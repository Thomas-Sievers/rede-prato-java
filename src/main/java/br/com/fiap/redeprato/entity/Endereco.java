package br.com.fiap.redeprato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Address shared by ofertantes, beneficiários and pontos. Table T_RP_ENDERECO. */
@Entity
@Table(name = "T_RP_ENDERECO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco", columnDefinition = "NUMBER")
    private Long idEndereco;

    @Column(name = "logradouro", columnDefinition = "VARCHAR2(100)", nullable = false)
    private String logradouro;

    @Column(name = "numero", columnDefinition = "VARCHAR2(10)")
    private String numero;

    @Column(name = "complemento", columnDefinition = "VARCHAR2(50)")
    private String complemento;

    @Column(name = "bairro", columnDefinition = "VARCHAR2(60)", nullable = false)
    private String bairro;

    @Column(name = "cidade", columnDefinition = "VARCHAR2(60)", nullable = false)
    private String cidade;

    @Column(name = "uf", columnDefinition = "CHAR(2)", nullable = false)
    private String uf;

    @Column(name = "cep", columnDefinition = "CHAR(8)", nullable = false)
    private String cep;
}
