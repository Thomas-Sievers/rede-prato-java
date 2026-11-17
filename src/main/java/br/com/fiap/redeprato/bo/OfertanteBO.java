package br.com.fiap.redeprato.bo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dto.OfertanteDTO;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** Business rules for ofertantes (HU01) and the Selo de Parceiro (HU20). */
@Service
public class OfertanteBO {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final OfertanteDAO dao;
    private final EnderecoBO enderecoBO;
    private final Clock clock;

    public OfertanteBO(OfertanteDAO dao, EnderecoBO enderecoBO, Clock clock) {
        this.dao = dao;
        this.enderecoBO = enderecoBO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OfertanteDTO.Response> listar(TipoOfertante tipo, Boolean ativo, String bairro) {
        Boolean filtroAtivo = ativo == null ? Boolean.TRUE : ativo;
        return dao.filtrar(tipo, filtroAtivo, Checks.clean(bairro)).stream()
                .map(OfertanteDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public OfertanteDTO.Response buscar(Long id) {
        return OfertanteDTO.Response.from(find(id));
    }

    @Transactional
    public OfertanteDTO.Response criar(OfertanteDTO.Request req) {
        Ofertante o = new Ofertante();
        apply(o, req);
        o.setAtivo(true);
        o.setPossuiSelo(false);
        o.setDataSelo(null);
        validate(o);
        return OfertanteDTO.Response.from(dao.save(o));
    }

    @Transactional
    public OfertanteDTO.Response atualizar(Long id, OfertanteDTO.Request req) {
        Ofertante o = find(id);
        apply(o, req);
        if (req.ativo() != null) {
            o.setAtivo(req.ativo()); // BR-03.9
        }
        validate(o);
        return OfertanteDTO.Response.from(dao.save(o));
    }

    /** BR-03.7: logical delete, idempotent. */
    @Transactional
    public void excluir(Long id) {
        Ofertante o = find(id);
        if (o.isAtivo()) {
            o.setAtivo(false);
            dao.save(o);
        }
    }

    @Transactional
    public OfertanteDTO.Response concederSelo(Long id) {
        Ofertante o = find(id);
        checkPodeReceberSelo(o);
        o.setPossuiSelo(true);
        o.setDataSelo(LocalDate.now(clock));
        validate(o);
        return OfertanteDTO.Response.from(dao.save(o));
    }

    @Transactional
    public void revogarSelo(Long id) {
        Ofertante o = find(id);
        o.setPossuiSelo(false);
        o.setDataSelo(null);
        validate(o);
        dao.save(o);
    }

    /** Loads the entity or throws 404. Used by other BOs too. */
    public Ofertante find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Ofertante é obrigatório.");
        }
        return dao.findById(id).orElseThrow(() -> NotFoundException.of("Ofertante", id));
    }

    private void apply(Ofertante o, OfertanteDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        o.setNome(Checks.clean(req.nome()));
        o.setTipo(req.tipo());
        o.setTelefone(Checks.digits(req.telefone()));
        o.setEmail(Checks.clean(req.email()));
        if (req.idEndereco() == null) {
            throw new BusinessRuleException("Endereço é obrigatório.");
        }
        o.setEndereco(enderecoBO.find(req.idEndereco())); // BR-03.5
    }

    void validate(Ofertante o) {
        checkNome(o);
        checkTipo(o);
        checkTelefone(o);
        checkEmail(o);
        checkSeloConsistente(o);
    }

    // BR-03.1
    private void checkNome(Ofertante o) {
        Checks.required(o.getNome(), "Nome", 100);
    }

    // BR-03.2
    private void checkTipo(Ofertante o) {
        Checks.notNull(o.getTipo(), "Tipo do ofertante é obrigatório.");
    }

    // BR-03.3
    private void checkTelefone(Ofertante o) {
        if (o.getTelefone() == null) {
            throw new BusinessRuleException("Telefone é obrigatório.");
        }
        Checks.phone(o.getTelefone(), "Telefone");
    }

    // BR-03.4
    private void checkEmail(Ofertante o) {
        if (o.getEmail() == null) {
            return;
        }
        Checks.maxLength(o.getEmail(), "E-mail", 100);
        if (!o.getEmail().matches(EMAIL_REGEX)) {
            throw new BusinessRuleException("E-mail inválido.");
        }
    }

    // BR-03.6
    private void checkSeloConsistente(Ofertante o) {
        if (o.isPossuiSelo() != (o.getDataSelo() != null)) {
            throw new BusinessRuleException("Selo inconsistente: selo e data do selo devem andar juntos.");
        }
    }

    // BR-03.8
    private void checkPodeReceberSelo(Ofertante o) {
        if (!o.isAtivo()) {
            throw new BusinessRuleException("Ofertante inativo não pode receber o selo.");
        }
        if (o.isPossuiSelo()) {
            throw new ConflictException("Este ofertante já possui o selo.");
        }
    }
}
