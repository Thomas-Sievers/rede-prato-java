package br.com.fiap.redeprato.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;

public interface DestinacaoDAO extends JpaRepository<Destinacao, Long> {

    @Query("""
            select d from Destinacao d join fetch d.lote l join fetch l.ofertante
            where (:idLote is null or l.idLote = :idLote)
              and (:tipo is null or d.tipo = :tipo)
            order by d.dataDestinacao desc, d.idDestinacao desc
            """)
    List<Destinacao> filtrar(@Param("idLote") Long idLote, @Param("tipo") TipoDestinacao tipo);

    /** Sum of kg already registered for a lote, optionally excluding one row (the one being updated). */
    @Query("""
            select coalesce(sum(d.pesoKg), 0) from Destinacao d
            where d.lote.idLote = :idLote
              and (:idExcluir is null or d.idDestinacao <> :idExcluir)
            """)
    BigDecimal somaPorLote(@Param("idLote") Long idLote, @Param("idExcluir") Long idExcluir);

    boolean existsByLote_IdLote(Long idLote);
}
