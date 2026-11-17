package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dto.DestinacaoDTO;
import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class DestinacaoBOTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T21:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 18, 0);

    @Mock
    private DestinacaoDAO dao;
    @Mock
    private LoteBO loteBO;

    private DestinacaoBO bo;
    private Lote lote;

    @BeforeEach
    void setUp() {
        bo = new DestinacaoBO(dao, loteBO, CLOCK);
        lote = Lote.builder().idLote(7L).descricao("Tomates").pesoKg(new BigDecimal("8.5"))
                .status(StatusLote.RETIRADO).ofertante(Ofertante.builder().idOfertante(1L).nome("Banca").build()).build();
        lenient().when(loteBO.find(7L)).thenReturn(lote);
        lenient().when(dao.somaPorLote(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(dao.save(any())).thenAnswer(inv -> {
            Destinacao d = inv.getArgument(0);
            if (d.getIdDestinacao() == null) {
                d.setIdDestinacao(3L);
            }
            return d;
        });
    }

    private static DestinacaoDTO.Request req(TipoDestinacao tipo, String kg) {
        return new DestinacaoDTO.Request(7L, tipo, kg == null ? null : new BigDecimal(kg), null);
    }

    @Test
    void registraConsumoDeLoteRetiradoComDataAgora() {
        DestinacaoDTO.Response r = bo.criar(req(TipoDestinacao.CONSUMO, "7"));
        assertThat(r.idDestinacao()).isEqualTo(3L);
        assertThat(r.dataDestinacao()).isEqualTo(AGORA);
        assertThat(r.nomeOfertante()).isEqualTo("Banca");
    }

    @Test
    void br083LoteRetiradoAceitaCompostagem() {
        assertThat(bo.criar(req(TipoDestinacao.COMPOSTAGEM, "1.5")).tipo()).isEqualTo(TipoDestinacao.COMPOSTAGEM);
    }

    @Test
    void br084LoteExpiradoSoCompostagem() {
        lote.setStatus(StatusLote.EXPIRADO);
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.CONSUMO, "1")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Lote vencido só pode ir para compostagem.");
        assertThat(bo.criar(req(TipoDestinacao.COMPOSTAGEM, "1")).tipo()).isEqualTo(TipoDestinacao.COMPOSTAGEM);
    }

    @Test
    void br085LoteDisponivelRecusado() {
        lote.setStatus(StatusLote.DISPONIVEL);
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.CONSUMO, "1")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Registre a retirada antes do destino.");
        verify(dao, never()).save(any());
    }

    @Test
    void br085LoteReservadoRecusado() {
        lote.setStatus(StatusLote.RESERVADO);
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.COMPOSTAGEM, "1")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Registre a retirada antes do destino.");
    }

    @Test
    void br086TipoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req(null, "1")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do destino é obrigatório.");
    }

    @Test
    void br086PesoObrigatorioEPositivo() {
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.CONSUMO, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso é obrigatório.");
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.CONSUMO, "0")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser maior que zero.");
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.CONSUMO, "0.001")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ter no máximo 2 casas decimais.");
    }

    @Test
    void br087SomaPassaDoPesoDoLote() {
        when(dao.somaPorLote(7L, null)).thenReturn(new BigDecimal("6"));
        assertThatThrownBy(() -> bo.criar(req(TipoDestinacao.COMPOSTAGEM, "3")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A soma dos destinos passa do peso do lote (8,5 kg).");
    }

    @Test
    void br087SomaIgualAoPesoPassa() {
        when(dao.somaPorLote(7L, null)).thenReturn(new BigDecimal("6"));
        assertThat(bo.criar(req(TipoDestinacao.COMPOSTAGEM, "2.5")).pesoKg()).isEqualByComparingTo("2.5");
    }

    @Test
    void br087NaAtualizacaoExcluiAPropriaLinhaDaSoma() {
        Destinacao d = Destinacao.builder().idDestinacao(3L).lote(lote).tipo(TipoDestinacao.CONSUMO)
                .pesoKg(new BigDecimal("8")).dataDestinacao(AGORA).build();
        when(dao.findById(3L)).thenReturn(Optional.of(d));
        when(dao.somaPorLote(7L, 3L)).thenReturn(BigDecimal.ZERO);
        assertThat(bo.atualizar(3L, req(TipoDestinacao.CONSUMO, "8.5")).pesoKg()).isEqualByComparingTo("8.5");
        verify(dao).somaPorLote(7L, 3L);
    }

    @Test
    void br088DataNoFuturoRecusada() {
        DestinacaoDTO.Request r = new DestinacaoDTO.Request(7L, TipoDestinacao.CONSUMO, BigDecimal.ONE, AGORA.plusMinutes(1));
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("A data do destino não pode ser no futuro.");
    }

    @Test
    void br088DataNoPassadoAceita() {
        DestinacaoDTO.Request r = new DestinacaoDTO.Request(7L, TipoDestinacao.CONSUMO, BigDecimal.ONE, AGORA.minusDays(2));
        assertThat(bo.criar(r).dataDestinacao()).isEqualTo(AGORA.minusDays(2));
    }

    @Test
    void loteInexistente() {
        when(loteBO.find(99L)).thenThrow(NotFoundException.of("Lote", 99L));
        assertThatThrownBy(() -> bo.criar(new DestinacaoDTO.Request(99L, TipoDestinacao.CONSUMO, BigDecimal.ONE, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirSempre() {
        Destinacao d = Destinacao.builder().idDestinacao(3L).lote(lote).build();
        when(dao.findById(3L)).thenReturn(Optional.of(d));
        bo.excluir(3L);
        verify(dao).delete(d);
    }

    @Test
    void loteEmLoteCriaDuasLinhas() {
        List<DestinacaoDTO.Response> r = bo.registrarLote(7L,
                new DestinacaoDTO.LoteRequest(new BigDecimal("7"), new BigDecimal("1.5")));
        assertThat(r).extracting(DestinacaoDTO.Response::tipo)
                .containsExactly(TipoDestinacao.CONSUMO, TipoDestinacao.COMPOSTAGEM);
        verify(dao, times(2)).save(any());
    }

    @Test
    void loteEmLoteSoCompostagem() {
        assertThat(bo.registrarLote(7L, new DestinacaoDTO.LoteRequest(null, new BigDecimal("2")))).hasSize(1);
    }

    @Test
    void loteEmLoteSemPesoRecusado() {
        assertThatThrownBy(() -> bo.registrarLote(7L, new DestinacaoDTO.LoteRequest(BigDecimal.ZERO, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Informe o peso consumido ou compostado.");
    }

    @Test
    void listarDelegaFiltros() {
        when(dao.filtrar(isNull(), any())).thenReturn(List.of());
        assertThat(bo.listar(null, TipoDestinacao.CONSUMO)).isEmpty();
    }
}
