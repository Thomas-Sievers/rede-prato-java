package br.com.fiap.redeprato.bo;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.BeneficiarioDAO;
import br.com.fiap.redeprato.dao.EnderecoDAO;
import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dto.EnderecoDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** Business rules for addresses. */
@Service
public class EnderecoBO {

    static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
            "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private final EnderecoDAO dao;
    private final OfertanteDAO ofertanteDAO;
    private final BeneficiarioDAO beneficiarioDAO;

    public EnderecoBO(EnderecoDAO dao, OfertanteDAO ofertanteDAO, BeneficiarioDAO beneficiarioDAO) {
        this.dao = dao;
        this.ofertanteDAO = ofertanteDAO;
        this.beneficiarioDAO = beneficiarioDAO;
    }

    @Transactional(readOnly = true)
    public List<EnderecoDTO.Response> listar(String bairro) {
        String b = Checks.clean(bairro);
        List<Endereco> list = b == null
                ? dao.findAllByOrderByIdEnderecoAsc()
                : dao.findByBairroIgnoreCaseOrderByIdEnderecoAsc(b);
        return list.stream().map(EnderecoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public EnderecoDTO.Response buscar(Long id) {
        return EnderecoDTO.Response.from(find(id));
    }

    @Transactional
    public EnderecoDTO.Response criar(EnderecoDTO.Request req) {
        Endereco e = new Endereco();
        apply(e, req);
        validate(e);
        return EnderecoDTO.Response.from(dao.save(e));
    }

    @Transactional
    public EnderecoDTO.Response atualizar(Long id, EnderecoDTO.Request req) {
        Endereco e = find(id);
        apply(e, req);
        validate(e);
        return EnderecoDTO.Response.from(dao.save(e));
    }

    @Transactional
    public void excluir(Long id) {
        Endereco e = find(id);
        checkSemUso(e);
        dao.delete(e);
    }

    /** Loads the entity or throws 404. Used by other BOs too. */
    public Endereco find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Endereço é obrigatório.");
        }
        return dao.findById(id).orElseThrow(() -> NotFoundException.of("Endereço", id));
    }

    private void apply(Endereco e, EnderecoDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        e.setLogradouro(Checks.clean(req.logradouro()));
        e.setNumero(Checks.clean(req.numero()));
        e.setComplemento(Checks.clean(req.complemento()));
        e.setBairro(Checks.clean(req.bairro()));
        e.setCidade(Checks.clean(req.cidade()));
        String uf = Checks.clean(req.uf());
        e.setUf(uf == null ? null : uf.toUpperCase(Locale.ROOT));
        String cep = req.cep() == null ? null : req.cep().replaceAll("[\\s.\\-]", "");
        e.setCep(cep == null || cep.isEmpty() ? null : cep);
    }

    void validate(Endereco e) {
        checkObrigatorios(e);
        checkTamanhos(e);
        checkCep(e);
        checkUf(e);
    }

    // BR-02.5
    private void checkObrigatorios(Endereco e) {
        Checks.required(e.getLogradouro(), "Logradouro", 100);
        Checks.required(e.getBairro(), "Bairro", 60);
        Checks.requiredF(e.getCidade(), "Cidade", 60);
        Checks.requiredF(e.getUf(), "UF", 2);
        Checks.required(e.getCep(), "CEP", 8);
    }

    // BR-02.8
    private void checkTamanhos(Endereco e) {
        Checks.maxLength(e.getNumero(), "Número", 10);
        Checks.maxLength(e.getComplemento(), "Complemento", 50);
    }

    // BR-02.6
    private void checkCep(Endereco e) {
        if (!e.getCep().matches("\\d{8}")) {
            throw new BusinessRuleException("CEP deve ter 8 dígitos.");
        }
    }

    // BR-02.7
    private void checkUf(Endereco e) {
        if (!UFS.contains(e.getUf())) {
            throw new BusinessRuleException("UF inválida.");
        }
    }

    // BR-02.9 (ofertante: feature 03; beneficiário: 05; ponto: 06)
    private void checkSemUso(Endereco e) {
        Long id = e.getIdEndereco();
        if (ofertanteDAO.existsByEndereco_IdEndereco(id) || beneficiarioDAO.existsByEndereco_IdEndereco(id)) {
            throw new ConflictException(MSG_EM_USO);
        }
    }

    static final String MSG_EM_USO = "Não é possível excluir: este endereço está em uso.";
}
