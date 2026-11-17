package br.com.fiap.redeprato.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.StatusReserva;

/** DAO layer: data access for {@link Reserva} (T_RP_RESERVA) via Spring Data JPA. */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            select r from Reserva r
              join fetch r.lote l join fetch l.categoria join fetch l.ofertante o join fetch o.endereco
              join fetch r.beneficiario b
              left join fetch r.ponto p left join fetch p.endereco
            where (:idBeneficiario is null or b.idBeneficiario = :idBeneficiario)
              and (:idOfertante is null or o.idOfertante = :idOfertante)
              and (:status is null or r.status = :status)
              and (:idLote is null or l.idLote = :idLote)
            order by r.dataReserva desc, r.idReserva desc
            """)
    List<Reserva> filtrar(@Param("idBeneficiario") Long idBeneficiario,
                          @Param("idOfertante") Long idOfertante,
                          @Param("status") StatusReserva status,
                          @Param("idLote") Long idLote);

    boolean existsByLote_IdLoteAndStatus(Long idLote, StatusReserva status);

    boolean existsByLote_IdLoteAndStatusIn(Long idLote, Collection<StatusReserva> status);

    Optional<Reserva> findFirstByLote_IdLoteAndStatus(Long idLote, StatusReserva status);

    List<Reserva> findByLote_IdLoteAndStatus(Long idLote, StatusReserva status);

    boolean existsByPonto_IdPonto(Long idPonto);

    boolean existsByBeneficiario_IdBeneficiario(Long idBeneficiario);

    /** BR-10.2: distinct lotes picked up (reserva RETIRADA) in [inicio, fim). */
    @Query("""
            select count(distinct r.lote.idLote) from Reserva r
            where r.status = br.com.fiap.redeprato.entity.enums.StatusReserva.RETIRADA
              and (:idOfertante is null or r.lote.ofertante.idOfertante = :idOfertante)
              and r.dataRetirada >= :inicio and r.dataRetirada < :fim
            """)
    long contarLotesRetirados(@Param("idOfertante") Long idOfertante,
                              @Param("inicio") java.time.LocalDateTime inicio,
                              @Param("fim") java.time.LocalDateTime fim);

    /** BR-10.3: distinct beneficiários served by pickups in [inicio, fim). */
    @Query("""
            select distinct b from Reserva r join r.beneficiario b
            where r.status = br.com.fiap.redeprato.entity.enums.StatusReserva.RETIRADA
              and (:idOfertante is null or r.lote.ofertante.idOfertante = :idOfertante)
              and r.dataRetirada >= :inicio and r.dataRetirada < :fim
            """)
    List<br.com.fiap.redeprato.entity.Beneficiario> beneficiariosAtendidos(
            @Param("idOfertante") Long idOfertante,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fim") java.time.LocalDateTime fim);
}
