package br.com.fiap.redeprato.dto;

import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;

/** Request/response records for {@link Beneficiario}. */
public final class BeneficiarioDTO {

    private BeneficiarioDTO() {
    }

    public record Request(String nome, TipoBeneficiario tipo, Integer qtdPessoas, String telefone,
                          Long idEndereco, Boolean ativo) {
    }

    public record Response(Long idBeneficiario, String nome, TipoBeneficiario tipo, Integer qtdPessoas,
                           String telefone, boolean ativo, Long idEndereco, String logradouro, String numero,
                           String complemento, String bairro, String cidade, String uf, String cep) {
        public static Response from(Beneficiario b) {
            Endereco e = b.getEndereco();
            return new Response(b.getIdBeneficiario(), b.getNome(), b.getTipo(), b.getQtdPessoas(),
                    b.getTelefone(), b.isAtivo(),
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
