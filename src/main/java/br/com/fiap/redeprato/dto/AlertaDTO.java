package br.com.fiap.redeprato.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.fiap.redeprato.entity.Alerta;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.CanalAlerta;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;

/** Request/response records for {@link Alerta}. */
public final class AlertaDTO {

    private AlertaDTO() {
    }

    public record Request(Long idLote, TipoAlerta tipo, CanalAlerta canal) {
    }

    /** UC04 body: lots to announce (empty = all available), optional new deadline and channel. */
    public record XepaRequest(Long idOfertante, List<Long> idsLotes, LocalDateTime novoPrazo, CanalAlerta canal) {
    }

    public record Response(Long idAlerta, Long idLote, String descricaoLote, Long idOfertante, String nomeOfertante,
                           TipoOfertante tipoOfertante, String bairro, LocalDateTime dataLimiteRetirada,
                           StatusLote statusLote, TipoAlerta tipo, CanalAlerta canal, LocalDateTime dataEnvio) {
        public static Response from(Alerta a) {
            Lote l = a.getLote();
            Ofertante o = l.getOfertante();
            return new Response(a.getIdAlerta(), l.getIdLote(), l.getDescricao(),
                    o == null ? null : o.getIdOfertante(),
                    o == null ? null : o.getNome(),
                    o == null ? null : o.getTipo(),
                    o == null || o.getEndereco() == null ? null : o.getEndereco().getBairro(),
                    l.getDataLimiteRetirada(), l.getStatus(), a.getTipo(), a.getCanal(), a.getDataEnvio());
        }
    }

    public record XepaResponse(int quantidade, String mensagem, List<Response> alertas) {
    }
}
