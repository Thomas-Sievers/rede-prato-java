package br.com.fiap.redeprato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Monthly impact report of an ofertante (HU22): base for the donation documentation. */
public final class RelatorioDTO {

    private RelatorioDTO() {
    }

    public record Item(Long idLote, String descricao, String categoria, LocalDateTime data, BigDecimal pesoKg,
                       BigDecimal valorEstimado, BigDecimal consumoKg, BigDecimal compostagemKg) {
    }

    public record Mensal(Long idOfertante, String nomeOfertante, String mes, List<Item> itens,
                         BigDecimal totalKg, BigDecimal consumoKg, BigDecimal compostagemKg,
                         BigDecimal valorEstimadoTotal) {
    }
}
