package br.com.fiap.redeprato.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.repository.AlertaRepository;
import br.com.fiap.redeprato.repository.LoteRepository;
import br.com.fiap.redeprato.dto.AlertaDTO;
import br.com.fiap.redeprato.entity.Alerta;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.CanalAlerta;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** BO layer (business rules) for alertas: UC04 (xepa) and UC07 (notices shown in the app; WhatsApp simulated). */
@Service
public class AlertaService {

    private final AlertaRepository repository;
    private final LoteRepository loteRepository;
    private final LoteService loteService;
    private final OfertanteService ofertanteService;
    private final Clock clock;

    public AlertaService(AlertaRepository repository, LoteRepository loteRepository, LoteService loteService, OfertanteService ofertanteService, Clock clock) {
        this.repository = repository;
        this.loteRepository = loteRepository;
        this.loteService = loteService;
        this.ofertanteService = ofertanteService;
        this.clock = clock;
    }

    /** UC04: one XEPA alert per selected DISPONIVEL lot of the feirante, all in one transaction (BR-09.7). */
    @Transactional
    public AlertaDTO.XepaResponse avisarXepa(AlertaDTO.XepaRequest req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Ofertante ofertante = ofertanteService.find(req.idOfertante());
        checkFeiranteAtivo(ofertante);
        loteService.expirarVencidos();
        List<Lote> lotes = selecionarLotes(ofertante, req.idsLotes());
        LocalDateTime agora = now();
        checkNovoPrazo(req.novoPrazo(), agora);
        CanalAlerta canal = req.canal() == null ? CanalAlerta.APP : req.canal();

        List<AlertaDTO.Response> out = new ArrayList<>();
        for (Lote l : lotes) {
            if (req.novoPrazo() != null) {
                l.setDataLimiteRetirada(req.novoPrazo().withNano(0)); // BR-09.4
                loteRepository.save(l);
            }
            Alerta a = Alerta.builder().lote(l).tipo(TipoAlerta.XEPA).canal(canal).dataEnvio(agora).build();
            out.add(AlertaDTO.Response.from(repository.save(a))); // BR-09.5
        }
        int n = out.size();
        String msg = "Xepa avisada para " + n + (n == 1 ? " lote." : " lotes.");
        return new AlertaDTO.XepaResponse(n, msg, out);
    }

    /** HU05 banner: alerts of lots still DISPONIVEL sent in the last {@code horas} hours. */
    @Transactional(readOnly = true)
    public List<AlertaDTO.Response> recentes(Integer horas) {
        int h = horas == null ? 24 : horas;
        if (h < 1 || h > 168) {
            throw new BusinessRuleException("Horas deve ser entre 1 e 168.");
        }
        return repository.recentes(now().minusHours(h)).stream().map(AlertaDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO.Response> listar(TipoAlerta tipo, Long idLote) {
        return repository.filtrar(tipo, idLote).stream().map(AlertaDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public AlertaDTO.Response buscar(Long id) {
        return AlertaDTO.Response.from(find(id));
    }

    @Transactional
    public AlertaDTO.Response criar(AlertaDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Alerta a = new Alerta();
        a.setLote(loteService.find(req.idLote()));
        a.setTipo(req.tipo());
        a.setCanal(req.canal() == null ? CanalAlerta.APP : req.canal());
        a.setDataEnvio(now());
        validate(a);
        return AlertaDTO.Response.from(repository.save(a));
    }

    @Transactional
    public AlertaDTO.Response atualizar(Long id, AlertaDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Alerta a = find(id);
        a.setTipo(req.tipo());
        a.setCanal(req.canal() == null ? a.getCanal() : req.canal());
        validate(a);
        return AlertaDTO.Response.from(repository.save(a));
    }

    /** Always allowed ("Desfazer" in the xepa screen). */
    @Transactional
    public void excluir(Long id) {
        repository.delete(find(id));
    }

    public Alerta find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Alerta é obrigatório.");
        }
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Alerta", id));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).withNano(0);
    }

    // BR-09.8
    void validate(Alerta a) {
        Checks.notNull(a.getTipo(), "Tipo do alerta é obrigatório.");
    }

    // BR-09.1
    private void checkFeiranteAtivo(Ofertante o) {
        if (o.getTipo() != TipoOfertante.FEIRANTE) {
            throw new BusinessRuleException("Só feirantes podem avisar xepa.");
        }
        if (!o.isAtivo()) {
            throw new BusinessRuleException("Ofertante inativo não pode avisar xepa.");
        }
    }

    // BR-09.2 and BR-09.3
    private List<Lote> selecionarLotes(Ofertante o, List<Long> ids) {
        List<Lote> lotes = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            lotes.addAll(loteRepository.findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(
                    o.getIdOfertante(), StatusLote.DISPONIVEL));
        } else {
            for (Long id : new LinkedHashSet<>(ids)) {
                Lote l = loteService.find(id);
                if (!l.getOfertante().getIdOfertante().equals(o.getIdOfertante())) {
                    throw new BusinessRuleException("O lote " + id + " não pertence a este ofertante.");
                }
                if (l.getStatus() != StatusLote.DISPONIVEL) {
                    throw new BusinessRuleException("O lote " + id + " não está disponível.");
                }
                lotes.add(l);
            }
        }
        if (lotes.isEmpty()) {
            throw new BusinessRuleException("Selecione pelo menos um lote disponível.");
        }
        return lotes;
    }

    // BR-09.4
    private void checkNovoPrazo(LocalDateTime novoPrazo, LocalDateTime agora) {
        if (novoPrazo != null && !novoPrazo.isAfter(agora)) {
            throw new BusinessRuleException("O novo prazo deve ser no futuro.");
        }
    }
}
