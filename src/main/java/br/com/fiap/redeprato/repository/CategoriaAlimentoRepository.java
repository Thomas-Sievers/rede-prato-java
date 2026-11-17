package br.com.fiap.redeprato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.redeprato.entity.CategoriaAlimento;

/** DAO layer: data access for {@link CategoriaAlimento} (T_RP_CATEGORIA_ALIMENTO) via Spring Data JPA. */
public interface CategoriaAlimentoRepository extends JpaRepository<CategoriaAlimento, Long> {

    List<CategoriaAlimento> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdCategoriaNot(String nome, Long idCategoria);
}
