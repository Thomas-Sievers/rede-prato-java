package br.com.fiap.redeprato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.redeprato.entity.Endereco;

/** DAO layer: data access for {@link Endereco} (T_RP_ENDERECO) via Spring Data JPA. */
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findAllByOrderByIdEnderecoAsc();

    List<Endereco> findByBairroIgnoreCaseOrderByIdEnderecoAsc(String bairro);
}
