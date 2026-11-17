package br.com.fiap.redeprato.dto;

import java.time.LocalTime;

import br.com.fiap.redeprato.converter.LocalTimeHHMMConverter;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.enums.TipoPonto;

/** Request/response records for {@link PontoDistribuicao}. Horários travel as "HH:MM". */
public final class PontoDTO {

    private PontoDTO() {
    }

    public record Request(String nome, TipoPonto tipo, String diasFuncionamento, String horarioAbertura,
                          String horarioFechamento, String telefone, Long idEndereco, Boolean ativo) {
    }

    public record Response(Long idPonto, String nome, TipoPonto tipo, String diasFuncionamento,
                           String horarioAbertura, String horarioFechamento, String telefone, boolean ativo,
                           Long idEndereco, String logradouro, String numero, String complemento,
                           String bairro, String cidade, String uf, String cep) {
        public static Response from(PontoDistribuicao p) {
            Endereco e = p.getEndereco();
            return new Response(p.getIdPonto(), p.getNome(), p.getTipo(), p.getDiasFuncionamento(),
                    hhmm(p.getHorarioAbertura()), hhmm(p.getHorarioFechamento()), p.getTelefone(), p.isAtivo(),
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

    public static String hhmm(LocalTime t) {
        return t == null ? null : t.format(LocalTimeHHMMConverter.HH_MM);
    }
}
