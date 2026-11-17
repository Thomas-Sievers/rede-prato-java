package br.com.fiap.redeprato.entity;

import java.time.LocalDate;

import br.com.fiap.redeprato.converter.BooleanSNConverter;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
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

/** Supermarket or street-market stall that registers surplus food. Table T_RP_OFERTANTE. */
@Entity
@Table(name = "T_RP_OFERTANTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ofertante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ofertante", columnDefinition = "NUMBER")
    private Long idOfertante;

    @Column(name = "nome", columnDefinition = "VARCHAR2(100)", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "VARCHAR2(20)", nullable = false)
    private TipoOfertante tipo;

    @Column(name = "telefone", columnDefinition = "VARCHAR2(15)", nullable = false)
    private String telefone;

    @Column(name = "email", columnDefinition = "VARCHAR2(100)")
    private String email;

    @Builder.Default
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "possui_selo", columnDefinition = "CHAR(1)", nullable = false)
    private boolean possuiSelo = false;

    @Column(name = "data_selo", columnDefinition = "DATE")
    private LocalDate dataSelo;

    @Builder.Default
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "ativo", columnDefinition = "CHAR(1)", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_endereco", columnDefinition = "NUMBER", nullable = false)
    private Endereco endereco;
}
