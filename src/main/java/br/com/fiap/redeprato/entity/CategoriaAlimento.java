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

/** Food category (frutas, verduras, legumes...). Table T_RP_CATEGORIA_ALIMENTO. */
@Entity
@Table(name = "T_RP_CATEGORIA_ALIMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", columnDefinition = "NUMBER")
    private Long idCategoria;

    @Column(name = "nome", columnDefinition = "VARCHAR2(50)", nullable = false, unique = true)
    private String nome;

    @Column(name = "descricao", columnDefinition = "VARCHAR2(200)")
    private String descricao;
}
