package br.com.fiap.redeprato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;

/** Request/response records for {@link Reserva}. */
public final class ReservaDTO {

    private ReservaDTO() {
    }

    public static final String LOCAL_OFERTANTE = "OFERTANTE";
    public static final String LOCAL_PONTO = "PONTO";

    public record Request(Long idLote, Long idBeneficiario, Long idPonto) {
    }

    /** PUT body: only the pickup place can change (null = at the ofertante). */
    public record UpdateRequest(Long idPonto) {
    }

    /** Confirmation view: where, when and until when to pick the lote up. */
    public record Response(Long idReserva, Long numeroRetirada, StatusReserva status,
                           LocalDateTime dataReserva, LocalDateTime dataRetirada,
                           Long idLote, String descricaoLote, BigDecimal pesoKg, CondicaoLote condicao,
                           ModalidadeLote modalidade, BigDecimal preco, BigDecimal valorEstimado,
                           boolean pagamentoNaRetirada, LocalDateTime dataLimiteRetirada, StatusLote statusLote,
                           String nomeCategoria,
                           Long idOfertante, String nomeOfertante, String telefoneOfertante, String enderecoOfertante,
                           String bairroOfertante,
                           String localRetirada, Long idPonto, String nomePonto, String enderecoPonto,
                           String diasFuncionamento, String horarioAbertura, String horarioFechamento,
                           Long idBeneficiario, String nomeBeneficiario, TipoBeneficiario tipoBeneficiario,
                           Integer qtdPessoas) {

        public static Response from(Reserva r) {
            Lote l = r.getLote();
            Ofertante o = l.getOfertante();
            PontoDistribuicao p = r.getPonto();
            Beneficiario b = r.getBeneficiario();
            return new Response(r.getIdReserva(), r.getIdReserva(), r.getStatus(),
                    r.getDataReserva(), r.getDataRetirada(),
                    l.getIdLote(), l.getDescricao(), l.getPesoKg(), l.getCondicao(),
                    l.getModalidade(), l.getPreco(), l.getValorEstimado(),
                    l.getModalidade() == ModalidadeLote.VENDA, l.getDataLimiteRetirada(), l.getStatus(),
                    l.getCategoria() == null ? null : l.getCategoria().getNome(),
                    o.getIdOfertante(), o.getNome(), o.getTelefone(), formatar(o.getEndereco()),
                    o.getEndereco() == null ? null : o.getEndereco().getBairro(),
                    p == null ? LOCAL_OFERTANTE : LOCAL_PONTO,
                    p == null ? null : p.getIdPonto(),
                    p == null ? null : p.getNome(),
                    p == null ? null : formatar(p.getEndereco()),
                    p == null ? null : p.getDiasFuncionamento(),
                    p == null ? null : PontoDTO.hhmm(p.getHorarioAbertura()),
                    p == null ? null : PontoDTO.hhmm(p.getHorarioFechamento()),
                    b.getIdBeneficiario(), b.getNome(), b.getTipo(), b.getQtdPessoas());
        }
    }

    /** "Rua X, 120 - Banca 12 - Paraíso". */
    public static String formatar(Endereco e) {
        if (e == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(e.getLogradouro() == null ? "" : e.getLogradouro());
        if (e.getNumero() != null) {
            sb.append(", ").append(e.getNumero());
        }
        if (e.getComplemento() != null) {
            sb.append(" - ").append(e.getComplemento());
        }
        if (e.getBairro() != null) {
            sb.append(" - ").append(e.getBairro());
        }
        return sb.toString();
    }
}
