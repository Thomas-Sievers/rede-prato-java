package br.com.fiap.redeprato.dto;

import br.com.fiap.redeprato.entity.CategoriaAlimento;

/** Request/response records for {@link CategoriaAlimento}. */
public final class CategoriaDTO {

    private CategoriaDTO() {
    }

    public record Request(String nome, String descricao) {
    }

    public record Response(Long idCategoria, String nome, String descricao) {
        public static Response from(CategoriaAlimento c) {
            return new Response(c.getIdCategoria(), c.getNome(), c.getDescricao());
        }
    }
}
