package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.ImpactoDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ImpactoBOTest {

    /** Tuesday 2026-11-17 10:00 in São Paulo. */
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime NOV = LocalDateTime.of(2026, 11, 1, 0, 0);
    static final LocalDateTime DEZ = LocalDateTime.of(2026, 12, 1, 0, 0);

    @Mock
    private DestinacaoDAO destinacaoDAO;
    @Mock
    private ReservaDAO reservaDAO;
    @Mock
    private OfertanteBO ofertanteBO;

    private ImpactoBO bo;
    private final Ofertante banca = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio")
            .possuiSelo(true).dataSelo(LocalDate.of(2026, 11, 7)).build();

    @BeforeEach
    void setUp() {
        bo = new ImpactoBO(destinacaoDAO, reservaDAO, ofertanteBO, CLOCK);
        lenient().when(ofertanteBO.find(1L)).thenReturn(banca);
        lenient().when(destinacaoDAO.somaPeriodo(any(), any(), any())).thenReturn(BigDecimal.ZERO);
    }

    private static Beneficiario ben(Integer qtd) {
        return Beneficiario.builder().qtdPessoas(qtd).build();
    }

    @Test
    void br101SomaKgPorTipoNoMes() {
        when(destinacaoDAO.somaPorTipo(1L, NOV, DEZ)).thenReturn(List.of(
                new ImpactoDTO.KgPorTipo(TipoDestinacao.CONSUMO, new BigDecimal("152")),
                new ImpactoDTO.KgPorTipo(TipoDestinacao.COMPOSTAGEM, new BigDecimal("34"))));
        ImpactoDTO.Ofertante r = bo.porOfertante(1L, "2026-11");
        assertThat(r.totalKg()).isEqualByComparingTo("186");
        assertThat(r.consumoKg()).isEqualByComparingTo("152");
        assertThat(r.compostagemKg()).isEqualByComparingTo("34");
        assertThat(r.mes()).isEqualTo("2026-11");
        assertThat(r.nomeOfertante()).isEqualTo("Banca do Seu Antônio");
        assertThat(r.possuiSelo()).isTrue();
        assertThat(r.dataSelo()).isEqualTo(LocalDate.of(2026, 11, 7));
    }

    @Test
    void br102LotesRetiradosVemDaConsulta() {
        when(reservaDAO.contarLotesRetirados(1L, NOV, DEZ)).thenReturn(41L);
        assertThat(bo.porOfertante(1L, "2026-11").lotesRetirados()).isEqualTo(41);
    }

    @Test
    void br103PessoasAtendidasContaNuloComoUm() {
        when(reservaDAO.beneficiariosAtendidos(1L, NOV, DEZ)).thenReturn(List.of(ben(4), ben(null), ben(60)));
        ImpactoDTO.Ofertante r = bo.porOfertante(1L, "2026-11");
        assertThat(r.beneficiariosAtendidos()).isEqualTo(3);
        assertThat(r.pessoasAtendidas()).isEqualTo(65);
    }

    @Test
    void br104PercentualComUmaCasa() {
        assertThat(ImpactoBO.percentual(new BigDecimal("152"), new BigDecimal("186"))).isEqualByComparingTo("81.7");
        assertThat(ImpactoBO.percentual(new BigDecimal("1"), new BigDecimal("3"))).isEqualByComparingTo("33.3");
        assertThat(ImpactoBO.percentual(new BigDecimal("2"), new BigDecimal("3"))).isEqualByComparingTo("66.7");
        assertThat(ImpactoBO.percentual(new BigDecimal("5"), new BigDecimal("5"))).isEqualByComparingTo("100.0");
    }

    @Test
    void br104PercentualZeroSemDestinos() {
        ImpactoDTO.Ofertante r = bo.porOfertante(1L, "2026-11");
        assertThat(r.totalKg()).isEqualByComparingTo("0");
        assertThat(r.percentualConsumo()).isEqualByComparingTo("0");
    }

    @Test
    void br105QuatroSemanasSegundaADomingoTerminandoNaAtual() {
        when(destinacaoDAO.somaPeriodo(eq(1L), eq(LocalDateTime.of(2026, 11, 16, 0, 0)),
                eq(LocalDateTime.of(2026, 11, 23, 0, 0)))).thenReturn(new BigDecimal("12.5"));
        List<ImpactoDTO.Semana> s = bo.porOfertante(1L, null).porSemana();
        assertThat(s).hasSize(4);
        assertThat(s.get(0).inicio()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(s.get(0).fim()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(s.get(3).inicio()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(s.get(3).fim()).isEqualTo(LocalDate.of(2026, 11, 22));
        assertThat(s.get(3).kg()).isEqualByComparingTo("12.5");
        assertThat(s.get(1).kg()).isEqualByComparingTo("0");
    }

    @Test
    void br105SemanaComNuloViraZero() {
        when(destinacaoDAO.somaPeriodo(any(), any(), any())).thenReturn(null);
        assertThat(bo.porOfertante(1L, null).porSemana()).allSatisfy(w -> assertThat(w.kg()).isEqualByComparingTo("0"));
    }

    @Test
    void br106OfertanteInexistente() {
        when(ofertanteBO.find(9L)).thenThrow(NotFoundException.of("Ofertante", 9L));
        assertThatThrownBy(() -> bo.porOfertante(9L, null)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void br107MesPadraoEhOAtual() {
        assertThat(bo.porOfertante(1L, null).mes()).isEqualTo("2026-11");
    }

    @Test
    void br107MesInvalido() {
        assertThatThrownBy(() -> bo.porOfertante(1L, "2026-13"))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Mês deve estar no formato AAAA-MM.");
        assertThatThrownBy(() -> bo.rede("nov"))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Mês deve estar no formato AAAA-MM.");
    }

    @Test
    void redeSomaTudoERanqueia() {
        when(destinacaoDAO.somaPorTipo(isNull(), eq(NOV), eq(DEZ))).thenReturn(List.of(
                new ImpactoDTO.KgPorTipo(TipoDestinacao.CONSUMO, new BigDecimal("10"))));
        when(destinacaoDAO.ranking(NOV, DEZ)).thenReturn(List.of(
                new ImpactoDTO.KgPorOfertante(2L, "Hortifrúti", new BigDecimal("7")),
                new ImpactoDTO.KgPorOfertante(1L, "Banca", new BigDecimal("3"))));
        ImpactoDTO.Rede r = bo.rede("2026-11");
        assertThat(r.totalKg()).isEqualByComparingTo("10");
        assertThat(r.percentualConsumo()).isEqualByComparingTo("100");
        assertThat(r.ofertantes()).extracting(ImpactoDTO.KgPorOfertante::idOfertante).containsExactly(2L, 1L);
        assertThat(r.porSemana()).hasSize(4);
    }
}
