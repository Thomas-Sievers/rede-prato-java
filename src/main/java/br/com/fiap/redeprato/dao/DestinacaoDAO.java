package br.com.fiap.redeprato.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.dto.ImpactoDTO;
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

    /** BR-10.1: kg per type in [inicio, fim) for one ofertante (or the whole network when null). */
    @Query("""
            select new br.com.fiap.redeprato.dto.ImpactoDTO$KgPorTipo(d.tipo, sum(d.pesoKg))
            from Destinacao d
            where (:idOfertante is null or d.lote.ofertante.idOfertante = :idOfertante)
              and d.dataDestinacao >= :inicio and d.dataDestinacao < :fim
            group by d.tipo
            """)
    List<ImpactoDTO.KgPorTipo> somaPorTipo(@Param("idOfertante") Long idOfertante,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);

    @Query("""
            select coalesce(sum(d.pesoKg), 0) from Destinacao d
            where (:idOfertante is null or d.lote.ofertante.idOfertante = :idOfertante)
              and d.dataDestinacao >= :inicio and d.dataDestinacao < :fim
            """)
    BigDecimal somaPeriodo(@Param("idOfertante") Long idOfertante,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim);

    @Query("""
            select new br.com.fiap.redeprato.dto.ImpactoDTO$KgPorOfertante(o.idOfertante, o.nome, sum(d.pesoKg))
            from Destinacao d join d.lote l join l.ofertante o
            where d.dataDestinacao >= :inicio and d.dataDestinacao < :fim
            group by o.idOfertante, o.nome
            order by sum(d.pesoKg) desc
            """)
    List<ImpactoDTO.KgPorOfertante> ranking(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);
}
