package br.com.fiap.redeprato.dto;

import br.com.fiap.redeprato.entity.Endereco;

/** Request/response records for {@link Endereco}. */
public final class EnderecoDTO {

    private EnderecoDTO() {
    }

    public record Request(String logradouro, String numero, String complemento,
                          String bairro, String cidade, String uf, String cep) {
    }

    public record Response(Long idEndereco, String logradouro, String numero, String complemento,
                           String bairro, String cidade, String uf, String cep) {
        public static Response from(Endereco e) {
            return new Response(e.getIdEndereco(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                    e.getBairro(), e.getCidade(), e.getUf(), e.getCep());
        }
    }
}
