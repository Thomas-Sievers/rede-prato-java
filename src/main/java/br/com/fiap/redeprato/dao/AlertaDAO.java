package br.com.fiap.redeprato.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Alerta;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;

public interface AlertaDAO extends JpaRepository<Alerta, Long> {

    @Query("""
            select a from Alerta a join fetch a.lote l join fetch l.ofertante o join fetch o.endereco
            where (:tipo is null or a.tipo = :tipo)
              and (:idLote is null or l.idLote = :idLote)
            order by a.dataEnvio desc, a.idAlerta desc
            """)
    List<Alerta> filtrar(@Param("tipo") TipoAlerta tipo, @Param("idLote") Long idLote);

    /** Alerts sent since {@code desde} whose lote is still DISPONIVEL (xepa banner, HU05). */
    @Query("""
            select a from Alerta a join fetch a.lote l join fetch l.ofertante o join fetch o.endereco
            where a.dataEnvio >= :desde
              and l.status = br.com.fiap.redeprato.entity.enums.StatusLote.DISPONIVEL
            order by a.dataEnvio desc, a.idAlerta desc
            """)
    List<Alerta> recentes(@Param("desde") LocalDateTime desde);

    List<Alerta> findByLote_IdLote(Long idLote);
}
