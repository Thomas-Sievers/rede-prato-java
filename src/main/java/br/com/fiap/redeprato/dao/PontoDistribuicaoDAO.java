package br.com.fiap.redeprato.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.PontoDistribuicao;

public interface PontoDistribuicaoDAO extends JpaRepository<PontoDistribuicao, Long> {

    @Query("""
            select p from PontoDistribuicao p join fetch p.endereco e
            where (:ativo is null or p.ativo = :ativo)
              and (:bairro is null or lower(e.bairro) = lower(:bairro))
            order by p.nome
            """)
    List<PontoDistribuicao> filtrar(@Param("ativo") Boolean ativo, @Param("bairro") String bairro);

    boolean existsByEndereco_IdEndereco(Long idEndereco);
}
