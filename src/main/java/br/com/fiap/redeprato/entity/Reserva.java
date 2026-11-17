package br.com.fiap.redeprato.entity;

import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.enums.StatusReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Links a whole lote to the beneficiário who will pick it up and, optionally, a pickup point.
 * The id is the "número de retirada". Table T_RP_RESERVA.
 */
@Entity
@Table(name = "T_RP_RESERVA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva", columnDefinition = "NUMBER")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lote", columnDefinition = "NUMBER", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_beneficiario", columnDefinition = "NUMBER", nullable = false)
    private Beneficiario beneficiario;

    /** Null when the pickup is at the ofertante. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ponto", columnDefinition = "NUMBER")
    private PontoDistribuicao ponto;

    @Column(name = "data_reserva", columnDefinition = "DATE", nullable = false)
    private LocalDateTime dataReserva;

    @Column(name = "data_retirada", columnDefinition = "DATE")
    private LocalDateTime dataRetirada;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR2(15)", nullable = false)
    private StatusReserva status = StatusReserva.ATIVA;
}
