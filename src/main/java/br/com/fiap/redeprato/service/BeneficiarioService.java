package br.com.fiap.redeprato.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.repository.BeneficiarioRepository;
import br.com.fiap.redeprato.dto.BeneficiarioDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** BO layer (business rules) for beneficiários (HU07). No document is ever asked for. */
@Service
public class BeneficiarioService {

    private final BeneficiarioRepository repository;
    private final EnderecoService enderecoService;

    public BeneficiarioService(BeneficiarioRepository repository, EnderecoService enderecoService) {
        this.repository = repository;
        this.enderecoService = enderecoService;
    }

    @Transactional(readOnly = true)
    public List<BeneficiarioDTO.Response> listar(TipoBeneficiario tipo, Boolean ativo) {
        return repository.filtrar(tipo, ativo == null ? Boolean.TRUE : ativo).stream()
                .map(BeneficiarioDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public BeneficiarioDTO.Response buscar(Long id) {
        return BeneficiarioDTO.Response.from(find(id));
    }

    @Transactional
    public BeneficiarioDTO.Response criar(BeneficiarioDTO.Request req) {
        Beneficiario b = new Beneficiario();
        apply(b, req);
        b.setAtivo(true);
        validate(b);
        return BeneficiarioDTO.Response.from(repository.save(b));
    }

    @Transactional
    public BeneficiarioDTO.Response atualizar(Long id, BeneficiarioDTO.Request req) {
        Beneficiario b = find(id);
        apply(b, req);
        if (req.ativo() != null) {
            b.setAtivo(req.ativo());
        }
        validate(b);
        return BeneficiarioDTO.Response.from(repository.save(b));
    }

    /** BR-05.7: logical delete, idempotent. */
    @Transactional
    public void excluir(Long id) {
        Beneficiario b = find(id);
        if (b.isAtivo()) {
            b.setAtivo(false);
            repository.save(b);
        }
    }

    /** Loads the entity or throws 404. Used by other services too. */
    public Beneficiario find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Beneficiário é obrigatório.");
        }
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Beneficiário", id));
    }

    private void apply(Beneficiario b, BeneficiarioDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        b.setNome(Checks.clean(req.nome()));
        b.setTipo(req.tipo());
        b.setQtdPessoas(req.qtdPessoas());
        b.setTelefone(Checks.digits(req.telefone()));
        // BR-05.5: optional, must exist when given
        b.setEndereco(req.idEndereco() == null ? null : enderecoService.find(req.idEndereco()));
    }

    void validate(Beneficiario b) {
        checkNome(b);
        checkTipo(b);
        checkQtdPessoas(b);
        checkTelefone(b);
    }

    // BR-05.1
    private void checkNome(Beneficiario b) {
        Checks.required(b.getNome(), "Nome", 100);
    }

    // BR-05.2
    private void checkTipo(Beneficiario b) {
        Checks.notNull(b.getTipo(), "Tipo do beneficiário é obrigatório.");
    }

    // BR-05.3
    private void checkQtdPessoas(Beneficiario b) {
        if (b.getTipo() == TipoBeneficiario.PESSOA) {
            if (b.getQtdPessoas() == null) {
                b.setQtdPessoas(1);
            } else if (b.getQtdPessoas() != 1) {
                throw new BusinessRuleException("Pessoa sozinha deve ter quantidade de pessoas igual a 1.");
            }
        }
        Integer q = b.getQtdPessoas();
        if (q != null && (q < 1 || q > 9999)) {
            throw new BusinessRuleException("Quantidade de pessoas deve ser entre 1 e 9999.");
        }
    }

    // BR-05.4
    private void checkTelefone(Beneficiario b) {
        if (b.getTelefone() != null) {
            Checks.phone(b.getTelefone(), "Telefone");
        }
    }
}
