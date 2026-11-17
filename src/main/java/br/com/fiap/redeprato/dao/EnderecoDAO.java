package br.com.fiap.redeprato.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.redeprato.entity.Endereco;

public interface EnderecoDAO extends JpaRepository<Endereco, Long> {

    List<Endereco> findAllByOrderByIdEnderecoAsc();

    List<Endereco> findByBairroIgnoreCaseOrderByIdEnderecoAsc(String bairro);
}
