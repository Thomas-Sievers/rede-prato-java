package br.com.fiap.redeprato.entity;

import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.enums.CanalAlerta;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;
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

/** General notice about a lote (new lote or xepa), without recipients (decision 8). Table T_RP_ALERTA. */
@Entity
@Table(name = "T_RP_ALERTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta", columnDefinition = "NUMBER")
    private Long idAlerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lote", columnDefinition = "NUMBER", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "VARCHAR2(15)", nullable = false)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", columnDefinition = "VARCHAR2(15)", nullable = false)
    private CanalAlerta canal;

    @Column(name = "data_envio", columnDefinition = "DATE", nullable = false)
    private LocalDateTime dataEnvio;
}
