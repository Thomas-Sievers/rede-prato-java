package br.com.fiap.redeprato.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.repository.CategoriaAlimentoRepository;
import br.com.fiap.redeprato.repository.LoteRepository;
import br.com.fiap.redeprato.dto.CategoriaDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** BO layer (business rules) for food categories (HU19). */
@Service
public class CategoriaAlimentoService {

    private final CategoriaAlimentoRepository repository;
    private final LoteRepository loteRepository;

    public CategoriaAlimentoService(CategoriaAlimentoRepository repository, LoteRepository loteRepository) {
        this.repository = repository;
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO.Response> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(CategoriaDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO.Response buscar(Long id) {
        return CategoriaDTO.Response.from(find(id));
    }

    @Transactional
    public CategoriaDTO.Response criar(CategoriaDTO.Request req) {
        CategoriaAlimento c = new CategoriaAlimento();
        apply(c, req);
        validate(c);
        return CategoriaDTO.Response.from(repository.save(c));
    }

    @Transactional
    public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Request req) {
        CategoriaAlimento c = find(id);
        apply(c, req);
        validate(c);
        return CategoriaDTO.Response.from(repository.save(c));
    }

    @Transactional
    public void excluir(Long id) {
        CategoriaAlimento c = find(id);
        checkSemLotes(c);
        repository.delete(c);
    }

    /** Loads the entity or throws 404. Used by other services too. */
    public CategoriaAlimento find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Categoria é obrigatória.");
        }
        return repository.findById(id).orElseThrow(() -> NotFoundException.ofFem("Categoria", id));
    }

    private void apply(CategoriaAlimento c, CategoriaDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        c.setNome(Checks.clean(req.nome()));
        c.setDescricao(Checks.clean(req.descricao()));
    }

    void validate(CategoriaAlimento c) {
        checkNome(c);
        checkDescricao(c);
        checkNomeUnico(c);
    }

    // BR-02.1
    private void checkNome(CategoriaAlimento c) {
        Checks.required(c.getNome(), "Nome da categoria", 50);
    }

    // BR-02.3
    private void checkDescricao(CategoriaAlimento c) {
        Checks.maxLength(c.getDescricao(), "Descrição", 200);
    }

    // BR-02.2
    private void checkNomeUnico(CategoriaAlimento c) {
        boolean duplicado = c.getIdCategoria() == null
                ? repository.existsByNomeIgnoreCase(c.getNome())
                : repository.existsByNomeIgnoreCaseAndIdCategoriaNot(c.getNome(), c.getIdCategoria());
        if (duplicado) {
            throw new ConflictException("Já existe uma categoria com esse nome.");
        }
    }

    // BR-02.4
    private void checkSemLotes(CategoriaAlimento c) {
        if (loteRepository.existsByCategoria_IdCategoria(c.getIdCategoria())) {
            throw new ConflictException("Não é possível excluir: existem lotes nesta categoria.");
        }
    }
}
