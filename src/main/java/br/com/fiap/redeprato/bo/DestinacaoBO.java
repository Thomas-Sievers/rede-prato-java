package br.com.fiap.redeprato.bo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dto.DestinacaoDTO;
import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** Business rules for destinações (HU14, decisão 4). Destinação kg feed the impact panel. */
@Service
public class DestinacaoBO {

    private final DestinacaoDAO dao;
    private final LoteBO loteBO;
    private final Clock clock;

    public DestinacaoBO(DestinacaoDAO dao, LoteBO loteBO, Clock clock) {
        this.dao = dao;
        this.loteBO = loteBO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DestinacaoDTO.Response> listar(Long idLote, TipoDestinacao tipo) {
        return dao.filtrar(idLote, tipo).stream().map(DestinacaoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public DestinacaoDTO.Response buscar(Long id) {
        return DestinacaoDTO.Response.from(find(id));
    }

    @Transactional
    public DestinacaoDTO.Response criar(DestinacaoDTO.Request req) {
        Destinacao d = new Destinacao();
        apply(d, req);
        validate(d);
        return DestinacaoDTO.Response.from(dao.save(d));
    }

    @Transactional
    public DestinacaoDTO.Response atualizar(Long id, DestinacaoDTO.Request req) {
        Destinacao d = find(id);
        apply(d, req);
        validate(d);
        return DestinacaoDTO.Response.from(dao.save(d));
    }

    /** Always allowed (corrects a wrong entry). */
    @Transactional
    public void excluir(Long id) {
        dao.delete(find(id));
    }

    /** Registers consumo and/or compostagem for one lote in a single transaction. */
    @Transactional
    public List<DestinacaoDTO.Response> registrarLote(Long idLote, DestinacaoDTO.LoteRequest req) {
        BigDecimal consumo = req == null ? null : req.consumoKg();
        BigDecimal compostagem = req == null ? null : req.compostagemKg();
        boolean temConsumo = consumo != null && consumo.signum() != 0;
        boolean temCompostagem = compostagem != null && compostagem.signum() != 0;
        if (!temConsumo && !temCompostagem) {
            throw new BusinessRuleException("Informe o peso consumido ou compostado.");
        }
        List<DestinacaoDTO.Response> out = new ArrayList<>();
        if (temConsumo) {
            out.add(criar(new DestinacaoDTO.Request(idLote, TipoDestinacao.CONSUMO, consumo, null)));
        }
        if (temCompostagem) {
            out.add(criar(new DestinacaoDTO.Request(idLote, TipoDestinacao.COMPOSTAGEM, compostagem, null)));
        }
        return out;
    }

    public Destinacao find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Destinação é obrigatória.");
        }
        return dao.findById(id).orElseThrow(() -> NotFoundException.ofFem("Destinação", id));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private void apply(Destinacao d, DestinacaoDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        d.setLote(loteBO.find(req.idLote()));
        d.setTipo(req.tipo());
        d.setPesoKg(req.pesoKg());
        d.setDataDestinacao(req.dataDestinacao() == null ? now().withNano(0) : req.dataDestinacao().withNano(0));
    }

    void validate(Destinacao d) {
        checkTipo(d);
        checkStatusLote(d);
        checkPeso(d);
        checkSoma(d);
        checkData(d);
    }

    // BR-08.6
    private void checkTipo(Destinacao d) {
        Checks.notNull(d.getTipo(), "Tipo do destino é obrigatório.");
    }

    // BR-08.3, BR-08.4, BR-08.5
    private void checkStatusLote(Destinacao d) {
        StatusLote s = d.getLote().getStatus();
        if (s == StatusLote.DISPONIVEL || s == StatusLote.RESERVADO) {
            throw new BusinessRuleException("Registre a retirada antes do destino.");
        }
        if (s == StatusLote.EXPIRADO && d.getTipo() != TipoDestinacao.COMPOSTAGEM) {
            throw new BusinessRuleException("Lote vencido só pode ir para compostagem.");
        }
    }

    // BR-08.6
    private void checkPeso(Destinacao d) {
        BigDecimal p = d.getPesoKg();
        if (p == null) {
            throw new BusinessRuleException("Peso é obrigatório.");
        }
        if (p.signum() <= 0) {
            throw new BusinessRuleException("Peso deve ser maior que zero.");
        }
        if (Checks.decimals(p) > 2) {
            throw new BusinessRuleException("Peso deve ter no máximo 2 casas decimais.");
        }
        if (p.compareTo(LoteBO.PESO_MAX) > 0) {
            throw new BusinessRuleException("Peso deve ser no máximo 999999,99 kg.");
        }
    }

    // BR-08.7
    private void checkSoma(Destinacao d) {
        Lote l = d.getLote();
        BigDecimal ja = dao.somaPorLote(l.getIdLote(), d.getIdDestinacao());
        BigDecimal total = (ja == null ? BigDecimal.ZERO : ja).add(d.getPesoKg());
        if (total.compareTo(l.getPesoKg()) > 0) {
            throw new BusinessRuleException("A soma dos destinos passa do peso do lote ("
                    + l.getPesoKg().stripTrailingZeros().toPlainString().replace('.', ',') + " kg).");
        }
    }

    // BR-08.8
    private void checkData(Destinacao d) {
        if (d.getDataDestinacao().isAfter(now())) {
            throw new BusinessRuleException("A data do destino não pode ser no futuro.");
        }
    }
}
