package br.com.fiap.redeprato.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;

public interface OfertanteDAO extends JpaRepository<Ofertante, Long> {

    @Query("""
            select o from Ofertante o join fetch o.endereco e
            where (:tipo is null or o.tipo = :tipo)
              and (:ativo is null or o.ativo = :ativo)
              and (:bairro is null or lower(e.bairro) = lower(:bairro))
            order by o.nome
            """)
    List<Ofertante> filtrar(@Param("tipo") TipoOfertante tipo,
                            @Param("ativo") Boolean ativo,
                            @Param("bairro") String bairro);

    boolean existsByEndereco_IdEndereco(Long idEndereco);
}
