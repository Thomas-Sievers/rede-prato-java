package br.com.fiap.redeprato.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
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

/** Final destination of (part of) a lote: consumption or composting. Source of the impact indicator. Table T_RP_DESTINACAO. */
@Entity
@Table(name = "T_RP_DESTINACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destinacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_destinacao", columnDefinition = "NUMBER")
    private Long idDestinacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lote", columnDefinition = "NUMBER", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "VARCHAR2(15)", nullable = false)
    private TipoDestinacao tipo;

    @Column(name = "peso_kg", columnDefinition = "NUMBER(8,2)", nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "data_destinacao", columnDefinition = "DATE", nullable = false)
    private LocalDateTime dataDestinacao;
}
