package br.com.fiap.redeprato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;

/** Request/response records for {@link Destinacao}. */
public final class DestinacaoDTO {

    private DestinacaoDTO() {
    }

    public record Request(Long idLote, TipoDestinacao tipo, BigDecimal pesoKg, LocalDateTime dataDestinacao) {
    }

    /** Batch body for {@code POST /api/lotes/{id}/destinacoes}. */
    public record LoteRequest(BigDecimal consumoKg, BigDecimal compostagemKg) {
    }

    public record Response(Long idDestinacao, Long idLote, String descricaoLote, BigDecimal pesoLoteKg,
                           StatusLote statusLote, Long idOfertante, String nomeOfertante,
                           TipoDestinacao tipo, BigDecimal pesoKg, LocalDateTime dataDestinacao) {
        public static Response from(Destinacao d) {
            Lote l = d.getLote();
            return new Response(d.getIdDestinacao(), l.getIdLote(), l.getDescricao(), l.getPesoKg(), l.getStatus(),
                    l.getOfertante() == null ? null : l.getOfertante().getIdOfertante(),
                    l.getOfertante() == null ? null : l.getOfertante().getNome(),
                    d.getTipo(), d.getPesoKg(), d.getDataDestinacao());
        }
    }
}
