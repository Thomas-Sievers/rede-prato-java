package br.com.fiap.redeprato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.fiap.redeprato.entity.enums.TipoDestinacao;

/** Records for the impact panel (HU15) and the JPQL aggregate projections. */
public final class ImpactoDTO {

    private ImpactoDTO() {
    }

    /** JPQL projection: kg per destination type. */
    public record KgPorTipo(TipoDestinacao tipo, BigDecimal kg) {
    }

    /** JPQL projection: kg per ofertante. */
    public record KgPorOfertante(Long idOfertante, String nome, BigDecimal totalKg) {
    }

    public record Semana(LocalDate inicio, LocalDate fim, BigDecimal kg) {
    }

    public record Ofertante(Long idOfertante, String nomeOfertante, String mes,
                            BigDecimal totalKg, BigDecimal consumoKg, BigDecimal compostagemKg,
                            BigDecimal percentualConsumo, long lotesRetirados, long beneficiariosAtendidos,
                            long pessoasAtendidas, List<Semana> porSemana, boolean possuiSelo, LocalDate dataSelo) {
    }

    public record Rede(String mes, BigDecimal totalKg, BigDecimal consumoKg, BigDecimal compostagemKg,
                       BigDecimal percentualConsumo, long lotesRetirados, long beneficiariosAtendidos,
                       long pessoasAtendidas, List<Semana> porSemana, List<KgPorOfertante> ofertantes) {
    }
}
