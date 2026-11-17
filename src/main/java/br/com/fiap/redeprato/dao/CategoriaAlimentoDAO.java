package br.com.fiap.redeprato.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.redeprato.entity.CategoriaAlimento;

public interface CategoriaAlimentoDAO extends JpaRepository<CategoriaAlimento, Long> {

    List<CategoriaAlimento> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdCategoriaNot(String nome, Long idCategoria);
}
