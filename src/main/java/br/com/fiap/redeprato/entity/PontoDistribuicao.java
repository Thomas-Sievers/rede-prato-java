package br.com.fiap.redeprato.entity;

import java.time.LocalTime;

import br.com.fiap.redeprato.converter.BooleanSNConverter;
import br.com.fiap.redeprato.converter.LocalTimeHHMMConverter;
import br.com.fiap.redeprato.entity.enums.TipoPonto;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

/** Pickup point with the Rede de Prato sign (cozinha solidária, ONG, associação). Table T_RP_PONTO_DISTRIBUICAO. */
@Entity
@Table(name = "T_RP_PONTO_DISTRIBUICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PontoDistribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ponto", columnDefinition = "NUMBER")
    private Long idPonto;

    @Column(name = "nome", columnDefinition = "VARCHAR2(100)", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "VARCHAR2(20)", nullable = false)
    private TipoPonto tipo;

    @Column(name = "dias_funcionamento", columnDefinition = "VARCHAR2(50)", nullable = false)
    private String diasFuncionamento;

    @Convert(converter = LocalTimeHHMMConverter.class)
    @Column(name = "horario_abertura", columnDefinition = "VARCHAR2(5)", nullable = false)
    private LocalTime horarioAbertura;

    @Convert(converter = LocalTimeHHMMConverter.class)
    @Column(name = "horario_fechamento", columnDefinition = "VARCHAR2(5)", nullable = false)
    private LocalTime horarioFechamento;

    @Column(name = "telefone", columnDefinition = "VARCHAR2(15)")
    private String telefone;

    @Builder.Default
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "ativo", columnDefinition = "CHAR(1)", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_endereco", columnDefinition = "NUMBER", nullable = false)
    private Endereco endereco;
}
