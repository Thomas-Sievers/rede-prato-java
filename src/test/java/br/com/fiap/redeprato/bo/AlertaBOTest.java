package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.AlertaDAO;
import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dto.AlertaDTO;
import br.com.fiap.redeprato.entity.Alerta;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.CanalAlerta;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class AlertaBOTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T15:30:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 12, 30);

    @Mock
    private AlertaDAO dao;
    @Mock
    private LoteDAO loteDAO;
    @Mock
    private LoteBO loteBO;
    @Mock
    private OfertanteBO ofertanteBO;

    private AlertaBO bo;
    private Ofertante banca;
    private Lote tomates;
    private Lote alface;

    @BeforeEach
    void setUp() {
        bo = new AlertaBO(dao, loteDAO, loteBO, ofertanteBO, CLOCK);
        banca = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio").tipo(TipoOfertante.FEIRANTE)
                .ativo(true).endereco(Endereco.builder().bairro("Paraíso").build()).build();
        tomates = lote(7L, banca, StatusLote.DISPONIVEL);
        alface = lote(8L, banca, StatusLote.DISPONIVEL);
        lenient().when(ofertanteBO.find(1L)).thenReturn(banca);
        lenient().when(loteBO.find(7L)).thenReturn(tomates);
        lenient().when(loteBO.find(8L)).thenReturn(alface);
        lenient().when(loteDAO.findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL))
                .thenReturn(List.of(tomates, alface));
        lenient().when(dao.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Lote lote(Long id, Ofertante o, StatusLote s) {
        return Lote.builder().idLote(id).ofertante(o).descricao("Lote " + id).pesoKg(BigDecimal.ONE)
                .dataCadastro(AGORA.minusHours(3)).dataLimiteRetirada(AGORA.plusHours(5)).status(s).build();
    }

    // ---------- UC04 ----------

    @Test
    void xepaSemSelecaoAvisaTodosOsLotesDisponiveis() {
        AlertaDTO.XepaResponse r = bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        assertThat(r.quantidade()).isEqualTo(2);
        assertThat(r.mensagem()).isEqualTo("Xepa avisada para 2 lotes.");
        assertThat(r.alertas()).allSatisfy(a -> {
            assertThat(a.tipo()).isEqualTo(TipoAlerta.XEPA);
            assertThat(a.canal()).isEqualTo(CanalAlerta.APP);
            assertThat(a.dataEnvio()).isEqualTo(AGORA);
            assertThat(a.statusLote()).isEqualTo(StatusLote.DISPONIVEL);
        });
        verify(dao, times(2)).save(any());
    }

    @Test
    void br092ListaVaziaEquivaleATodos() {
        assertThat(bo.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(), null, null)).quantidade()).isEqualTo(2);
    }

    @Test
    void xepaComSelecaoAvisaSoOsEscolhidos() {
        AlertaDTO.XepaResponse r = bo.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L), null, CanalAlerta.WHATSAPP));
        assertThat(r.quantidade()).isEqualTo(1);
        assertThat(r.mensagem()).isEqualTo("Xepa avisada para 1 lote.");
        assertThat(r.alertas().get(0).idLote()).isEqualTo(7L);
        assertThat(r.alertas().get(0).canal()).isEqualTo(CanalAlerta.WHATSAPP);
    }

    @Test
    void xepaIgnoraIdsRepetidos() {
        assertThat(bo.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L, 7L), null, null)).quantidade()).isEqualTo(1);
    }

    @Test
    void br091SoFeirante() {
        banca.setTipo(TipoOfertante.SUPERMERCADO);
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Só feirantes podem avisar xepa.");
        verify(dao, never()).save(any());
    }

    @Test
    void br091FeiranteInativo() {
        banca.setAtivo(false);
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode avisar xepa.");
    }

    @Test
    void br091OfertanteInexistente() {
        when(ofertanteBO.find(9L)).thenThrow(NotFoundException.of("Ofertante", 9L));
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(9L, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void br092SemLoteDisponivelRecusado() {
        when(loteDAO.findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL))
                .thenReturn(List.of());
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Selecione pelo menos um lote disponível.");
        verify(dao, never()).save(any());
    }

    @Test
    void br093LoteDeOutroOfertanteRecusado() {
        Ofertante outro = Ofertante.builder().idOfertante(2L).build();
        when(loteBO.find(5L)).thenReturn(lote(5L, outro, StatusLote.DISPONIVEL));
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(5L), null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O lote 5 não pertence a este ofertante.");
    }

    @Test
    void br093LoteNaoDisponivelRecusado() {
        when(loteBO.find(5L)).thenReturn(lote(5L, banca, StatusLote.RESERVADO));
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L, 5L), null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O lote 5 não está disponível.");
        verify(dao, never()).save(any());
    }

    @Test
    void br094NovoPrazoAtualizaOsLotes() {
        LocalDateTime fimDaFeira = AGORA.plusMinutes(90);
        bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, fimDaFeira, null));
        assertThat(tomates.getDataLimiteRetirada()).isEqualTo(fimDaFeira);
        assertThat(alface.getDataLimiteRetirada()).isEqualTo(fimDaFeira);
        verify(loteDAO).save(tomates);
        verify(loteDAO).save(alface);
    }

    @Test
    void br094NovoPrazoNoPassadoRecusado() {
        assertThatThrownBy(() -> bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, AGORA.minusMinutes(1), null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O novo prazo deve ser no futuro.");
        verify(dao, never()).save(any());
    }

    @Test
    void br094SemNovoPrazoNaoMexeNosLotes() {
        bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        verify(loteDAO, never()).save(any());
    }

    @Test
    void br096AlertaNaoGuardaDestinatarioNemStatus() {
        List<String> campos = Arrays.stream(Alerta.class.getDeclaredFields()).map(Field::getName).toList();
        assertThat(campos).containsExactlyInAnyOrder("idAlerta", "lote", "tipo", "canal", "dataEnvio");
    }

    @Test
    void br097ExpiracaoRodaAntesDeEscolherOsLotes() {
        bo.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        InOrder order = inOrder(loteBO, loteDAO);
        order.verify(loteBO).expirarVencidos();
        order.verify(loteDAO).findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL);
    }

    @Test
    void br097XepaETransacional() throws Exception {
        assertThat(AlertaBO.class.getMethod("avisarXepa", AlertaDTO.XepaRequest.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class)).isNotNull();
    }

    // ---------- recentes / CRUD ----------

    @Test
    void recentesPadrao24h() {
        when(dao.recentes(AGORA.minusHours(24))).thenReturn(List.of());
        assertThat(bo.recentes(null)).isEmpty();
    }

    @Test
    void recentesHorasForaDoLimite() {
        assertThatThrownBy(() -> bo.recentes(0)).isInstanceOf(BusinessRuleException.class)
                .hasMessage("Horas deve ser entre 1 e 168.");
        assertThatThrownBy(() -> bo.recentes(169)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br098CriarComCanalPadraoApp() {
        AlertaDTO.Response r = bo.criar(new AlertaDTO.Request(7L, TipoAlerta.NOVO_LOTE, null));
        assertThat(r.canal()).isEqualTo(CanalAlerta.APP);
        assertThat(r.dataEnvio()).isEqualTo(AGORA);
    }

    @Test
    void br098TipoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(new AlertaDTO.Request(7L, null, CanalAlerta.APP)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do alerta é obrigatório.");
    }

    @Test
    void br098LoteInexistente() {
        when(loteBO.find(99L)).thenThrow(NotFoundException.of("Lote", 99L));
        assertThatThrownBy(() -> bo.criar(new AlertaDTO.Request(99L, TipoAlerta.XEPA, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizarTipoECanal() {
        Alerta a = Alerta.builder().idAlerta(3L).lote(tomates).tipo(TipoAlerta.NOVO_LOTE).canal(CanalAlerta.APP)
                .dataEnvio(AGORA).build();
        when(dao.findById(3L)).thenReturn(Optional.of(a));
        AlertaDTO.Response r = bo.atualizar(3L, new AlertaDTO.Request(null, TipoAlerta.XEPA, CanalAlerta.WHATSAPP));
        assertThat(r.tipo()).isEqualTo(TipoAlerta.XEPA);
        assertThat(r.canal()).isEqualTo(CanalAlerta.WHATSAPP);
    }

    @Test
    void excluirSempre() {
        Alerta a = Alerta.builder().idAlerta(3L).lote(tomates).build();
        when(dao.findById(3L)).thenReturn(Optional.of(a));
        bo.excluir(3L);
        verify(dao).delete(a);
    }

    @Test
    void buscarInexistente() {
        when(dao.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(4L)).isInstanceOf(NotFoundException.class)
                .hasMessage("Alerta 4 não encontrado.");
    }
}
