package br.com.fiap.redeprato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.repository.DestinacaoRepository;
import br.com.fiap.redeprato.dto.RelatorioDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    static final LocalDateTime NOV = LocalDateTime.of(2026, 11, 1, 0, 0);
    static final LocalDateTime DEZ = LocalDateTime.of(2026, 12, 1, 0, 0);

    @Mock
    private DestinacaoRepository repository;
    @Mock
    private OfertanteService ofertanteService;
    @Mock
    private ImpactoService impactoService;

    private RelatorioService service;

    @BeforeEach
    void setUp() {
        service =new RelatorioService(repository, ofertanteService, impactoService);
    }

    private static Destinacao d(Lote l, TipoDestinacao t, String kg, int dia) {
        return Destinacao.builder().lote(l).tipo(t).pesoKg(new BigDecimal(kg))
                .dataDestinacao(LocalDateTime.of(2026, 11, dia, 19, 0)).build();
    }

    @Test
    void agrupaPorLoteESomaTotais() {
        Ofertante o = Ofertante.builder().idOfertante(3L).nome("Hortifrúti").build();
        CategoriaAlimento frutas = CategoriaAlimento.builder().nome("Frutas").build();
        Lote macas = Lote.builder().idLote(1L).descricao("Maçãs").categoria(frutas).pesoKg(new BigDecimal("7"))
                .valorEstimado(new BigDecimal("30")).build();
        Lote mamoes = Lote.builder().idLote(2L).descricao("Mamões").categoria(frutas).pesoKg(new BigDecimal("6")).build();
        when(ofertanteService.find(3L)).thenReturn(o);
        when(impactoService.parseMes("2026-11")).thenReturn(YearMonth.of(2026, 11));
        when(repository.doOfertanteNoPeriodo(3L, NOV, DEZ)).thenReturn(List.of(
                d(macas, TipoDestinacao.CONSUMO, "6", 3), d(macas, TipoDestinacao.COMPOSTAGEM, "1", 4),
                d(mamoes, TipoDestinacao.CONSUMO, "6", 10)));

        RelatorioDTO.Mensal r = service.mensal(3L, "2026-11");

        assertThat(r.mes()).isEqualTo("2026-11");
        assertThat(r.itens()).hasSize(2);
        assertThat(r.itens().get(0).consumoKg()).isEqualByComparingTo("6");
        assertThat(r.itens().get(0).compostagemKg()).isEqualByComparingTo("1");
        assertThat(r.itens().get(0).data()).isEqualTo(LocalDateTime.of(2026, 11, 4, 19, 0));
        assertThat(r.totalKg()).isEqualByComparingTo("13");
        assertThat(r.consumoKg()).isEqualByComparingTo("12");
        assertThat(r.compostagemKg()).isEqualByComparingTo("1");
        assertThat(r.valorEstimadoTotal()).isEqualByComparingTo("30"); // BR-18.2: null counts as 0
    }

    @Test
    void mesSemDestinosDaRelatorioVazio() {
        when(ofertanteService.find(3L)).thenReturn(Ofertante.builder().idOfertante(3L).nome("X").build());
        when(impactoService.parseMes(null)).thenReturn(YearMonth.of(2026, 11));
        when(repository.doOfertanteNoPeriodo(3L, NOV, DEZ)).thenReturn(List.of());
        RelatorioDTO.Mensal r = service.mensal(3L, null);
        assertThat(r.itens()).isEmpty();
        assertThat(r.totalKg()).isEqualByComparingTo("0");
    }

    @Test
    void ofertanteInexistente() {
        when(ofertanteService.find(9L)).thenThrow(NotFoundException.of("Ofertante", 9L));
        assertThatThrownBy(() -> service.mensal(9L, null)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void mesInvalido() {
        when(ofertanteService.find(3L)).thenReturn(Ofertante.builder().idOfertante(3L).build());
        when(impactoService.parseMes("x")).thenThrow(new BusinessRuleException("Mês deve estar no formato AAAA-MM."));
        assertThatThrownBy(() -> service.mensal(3L, "x")).isInstanceOf(BusinessRuleException.class);
    }
}
