package br.com.fiap.redeprato.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.redeprato.converter.BooleanSNConverter;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
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

/** Surplus food batch registered by an ofertante. Table T_RP_LOTE. */
@Entity
@Table(name = "T_RP_LOTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote", columnDefinition = "NUMBER")
    private Long idLote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ofertante", columnDefinition = "NUMBER", nullable = false)
    private Ofertante ofertante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", columnDefinition = "NUMBER", nullable = false)
    private CategoriaAlimento categoria;

    @Column(name = "descricao", columnDefinition = "VARCHAR2(200)", nullable = false)
    private String descricao;

    @Column(name = "peso_kg", columnDefinition = "NUMBER(8,2)", nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao", columnDefinition = "VARCHAR2(20)", nullable = false)
    private CondicaoLote condicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", columnDefinition = "VARCHAR2(15)", nullable = false)
    private ModalidadeLote modalidade;

    @Column(name = "preco", columnDefinition = "NUMBER(10,2)", precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "valor_estimado", columnDefinition = "NUMBER(10,2)", precision = 10, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "data_cadastro", columnDefinition = "DATE", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_limite_retirada", columnDefinition = "DATE", nullable = false)
    private LocalDateTime dataLimiteRetirada;

    @Column(name = "url_foto", columnDefinition = "VARCHAR2(255)")
    private String urlFoto;

    @Builder.Default
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "precisa_transporte", columnDefinition = "CHAR(1)", nullable = false)
    private boolean precisaTransporte = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR2(15)", nullable = false)
    private StatusLote status = StatusLote.DISPONIVEL;
}
