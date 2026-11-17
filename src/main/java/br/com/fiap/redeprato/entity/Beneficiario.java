package br.com.fiap.redeprato.entity;

import br.com.fiap.redeprato.converter.BooleanSNConverter;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;
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

/**
 * Person, family, institution or small business that receives or buys food. Table T_RP_BENEFICIARIO.
 * Deliberately has no document field (no CPF/RG) and optional phone/address (decision 12).
 */
@Entity
@Table(name = "T_RP_BENEFICIARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario", columnDefinition = "NUMBER")
    private Long idBeneficiario;

    @Column(name = "nome", columnDefinition = "VARCHAR2(100)", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "VARCHAR2(20)", nullable = false)
    private TipoBeneficiario tipo;

    @Column(name = "qtd_pessoas", columnDefinition = "NUMBER(4)")
    private Integer qtdPessoas;

    @Column(name = "telefone", columnDefinition = "VARCHAR2(15)")
    private String telefone;

    @Builder.Default
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "ativo", columnDefinition = "CHAR(1)", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", columnDefinition = "NUMBER")
    private Endereco endereco;
}
