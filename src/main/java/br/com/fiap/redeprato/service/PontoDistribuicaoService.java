package br.com.fiap.redeprato.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.converter.LocalTimeHHMMConverter;
import br.com.fiap.redeprato.repository.PontoDistribuicaoRepository;
import br.com.fiap.redeprato.dto.PontoDTO;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** BO layer (business rules) for pontos de distribuição (HU17). */
@Service
public class PontoDistribuicaoService {

    static final String MSG_HHMM = "Horário deve estar no formato HH:MM.";

    private final PontoDistribuicaoRepository repository;
    private final EnderecoService enderecoService;

    public PontoDistribuicaoService(PontoDistribuicaoRepository repository, EnderecoService enderecoService) {
        this.repository = repository;
        this.enderecoService = enderecoService;
    }

    @Transactional(readOnly = true)
    public List<PontoDTO.Response> listar(Boolean ativo, String bairro) {
        return repository.filtrar(ativo == null ? Boolean.TRUE : ativo, Checks.clean(bairro)).stream()
                .map(PontoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public PontoDTO.Response buscar(Long id) {
        return PontoDTO.Response.from(find(id));
    }

    @Transactional
    public PontoDTO.Response criar(PontoDTO.Request req) {
        PontoDistribuicao p = new PontoDistribuicao();
        apply(p, req);
        p.setAtivo(true);
        validate(p);
        return PontoDTO.Response.from(repository.save(p));
    }

    @Transactional
    public PontoDTO.Response atualizar(Long id, PontoDTO.Request req) {
        PontoDistribuicao p = find(id);
        apply(p, req);
        if (req.ativo() != null) {
            p.setAtivo(req.ativo());
        }
        validate(p);
        return PontoDTO.Response.from(repository.save(p));
    }

    /** BR-06.7: logical delete, idempotent. */
    @Transactional
    public void excluir(Long id) {
        PontoDistribuicao p = find(id);
        if (p.isAtivo()) {
            p.setAtivo(false);
            repository.save(p);
        }
    }

    /** Loads the entity or throws 404. Used by other services too. */
    public PontoDistribuicao find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Ponto de distribuição é obrigatório.");
        }
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Ponto de distribuição", id));
    }

    private void apply(PontoDistribuicao p, PontoDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        p.setNome(Checks.clean(req.nome()));
        p.setTipo(req.tipo());
        p.setDiasFuncionamento(Checks.clean(req.diasFuncionamento()));
        p.setHorarioAbertura(parseHorario(req.horarioAbertura(), "Horário de abertura é obrigatório."));
        p.setHorarioFechamento(parseHorario(req.horarioFechamento(), "Horário de fechamento é obrigatório."));
        p.setTelefone(Checks.digits(req.telefone()));
        if (req.idEndereco() == null) {
            throw new BusinessRuleException("Endereço é obrigatório."); // BR-06.6
        }
        p.setEndereco(enderecoService.find(req.idEndereco()));
    }

    // BR-06.4
    static LocalTime parseHorario(String text, String msgObrigatorio) {
        String t = Checks.clean(text);
        if (t == null) {
            throw new BusinessRuleException(msgObrigatorio);
        }
        if (!t.matches("\\d{2}:\\d{2}")) {
            throw new BusinessRuleException(MSG_HHMM);
        }
        try {
            return LocalTime.parse(t, LocalTimeHHMMConverter.HH_MM);
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException(MSG_HHMM);
        }
    }

    void validate(PontoDistribuicao p) {
        checkNome(p);
        checkTipo(p);
        checkDias(p);
        checkHorarios(p);
        checkTelefone(p);
    }

    // BR-06.1
    private void checkNome(PontoDistribuicao p) {
        Checks.required(p.getNome(), "Nome", 100);
    }

    // BR-06.2
    private void checkTipo(PontoDistribuicao p) {
        Checks.notNull(p.getTipo(), "Tipo do ponto é obrigatório.");
    }

    // BR-06.3
    private void checkDias(PontoDistribuicao p) {
        Checks.required(p.getDiasFuncionamento(), "Dias de funcionamento", 50);
    }

    // BR-06.5
    private void checkHorarios(PontoDistribuicao p) {
        if (!p.getHorarioAbertura().isBefore(p.getHorarioFechamento())) {
            throw new BusinessRuleException("Horário de abertura deve ser antes do fechamento.");
        }
    }

    // BR-06.7
    private void checkTelefone(PontoDistribuicao p) {
        if (p.getTelefone() != null) {
            Checks.phone(p.getTelefone(), "Telefone");
        }
    }
}
