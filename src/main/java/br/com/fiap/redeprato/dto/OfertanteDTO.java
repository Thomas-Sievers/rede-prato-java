package br.com.fiap.redeprato.dto;

import java.time.LocalDate;

import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;

/** Request/response records for {@link Ofertante}. */
public final class OfertanteDTO {

    private OfertanteDTO() {
    }

    /** {@code ativo} is only used by PUT (null keeps the current value). */
    public record Request(String nome, TipoOfertante tipo, String telefone, String email,
                          Long idEndereco, Boolean ativo) {
    }

    public record Response(Long idOfertante, String nome, TipoOfertante tipo, String telefone, String email,
                           boolean possuiSelo, LocalDate dataSelo, boolean ativo,
                           Long idEndereco, String logradouro, String numero, String complemento,
                           String bairro, String cidade, String uf, String cep) {
        public static Response from(Ofertante o) {
            Endereco e = o.getEndereco();
            return new Response(o.getIdOfertante(), o.getNome(), o.getTipo(), o.getTelefone(), o.getEmail(),
                    o.isPossuiSelo(), o.getDataSelo(), o.isAtivo(),
                    e == null ? null : e.getIdEndereco(),
                    e == null ? null : e.getLogradouro(),
                    e == null ? null : e.getNumero(),
                    e == null ? null : e.getComplemento(),
                    e == null ? null : e.getBairro(),
                    e == null ? null : e.getCidade(),
                    e == null ? null : e.getUf(),
                    e == null ? null : e.getCep());
        }
    }
}
