package br.com.fiap.redeprato.service;

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

import br.com.fiap.redeprato.repository.AlertaRepository;
import br.com.fiap.redeprato.repository.LoteRepository;
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
class AlertaServiceTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T15:30:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 12, 30);

    @Mock
    private AlertaRepository repository;
    @Mock
    private LoteRepository loteRepository;
    @Mock
    private LoteService loteService;
    @Mock
    private OfertanteService ofertanteService;

    private AlertaService service;
    private Ofertante banca;
    private Lote tomates;
    private Lote alface;

    @BeforeEach
    void setUp() {
        service =new AlertaService(repository, loteRepository, loteService, ofertanteService, CLOCK);
        banca = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio").tipo(TipoOfertante.FEIRANTE)
                .ativo(true).endereco(Endereco.builder().bairro("Paraíso").build()).build();
        tomates = lote(7L, banca, StatusLote.DISPONIVEL);
        alface = lote(8L, banca, StatusLote.DISPONIVEL);
        lenient().when(ofertanteService.find(1L)).thenReturn(banca);
        lenient().when(loteService.find(7L)).thenReturn(tomates);
        lenient().when(loteService.find(8L)).thenReturn(alface);
        lenient().when(loteRepository.findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL))
                .thenReturn(List.of(tomates, alface));
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Lote lote(Long id, Ofertante o, StatusLote s) {
        return Lote.builder().idLote(id).ofertante(o).descricao("Lote " + id).pesoKg(BigDecimal.ONE)
                .dataCadastro(AGORA.minusHours(3)).dataLimiteRetirada(AGORA.plusHours(5)).status(s).build();
    }

    // ---------- UC04 ----------

    @Test
    void xepaSemSelecaoAvisaTodosOsLotesDisponiveis() {
        AlertaDTO.XepaResponse r = service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        assertThat(r.quantidade()).isEqualTo(2);
        assertThat(r.mensagem()).isEqualTo("Xepa avisada para 2 lotes.");
        assertThat(r.alertas()).allSatisfy(a -> {
            assertThat(a.tipo()).isEqualTo(TipoAlerta.XEPA);
            assertThat(a.canal()).isEqualTo(CanalAlerta.APP);
            assertThat(a.dataEnvio()).isEqualTo(AGORA);
            assertThat(a.statusLote()).isEqualTo(StatusLote.DISPONIVEL);
        });
        verify(repository, times(2)).save(any());
    }

    @Test
    void br092ListaVaziaEquivaleATodos() {
        assertThat(service.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(), null, null)).quantidade()).isEqualTo(2);
    }

    @Test
    void xepaComSelecaoAvisaSoOsEscolhidos() {
        AlertaDTO.XepaResponse r = service.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L), null, CanalAlerta.WHATSAPP));
        assertThat(r.quantidade()).isEqualTo(1);
        assertThat(r.mensagem()).isEqualTo("Xepa avisada para 1 lote.");
        assertThat(r.alertas().get(0).idLote()).isEqualTo(7L);
        assertThat(r.alertas().get(0).canal()).isEqualTo(CanalAlerta.WHATSAPP);
    }

    @Test
    void xepaIgnoraIdsRepetidos() {
        assertThat(service.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L, 7L), null, null)).quantidade()).isEqualTo(1);
    }

    @Test
    void br091SoFeirante() {
        banca.setTipo(TipoOfertante.SUPERMERCADO);
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Só feirantes podem avisar xepa.");
        verify(repository, never()).save(any());
    }

    @Test
    void br091FeiranteInativo() {
        banca.setAtivo(false);
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode avisar xepa.");
    }

    @Test
    void br091OfertanteInexistente() {
        when(ofertanteService.find(9L)).thenThrow(NotFoundException.of("Ofertante", 9L));
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(9L, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void br092SemLoteDisponivelRecusado() {
        when(loteRepository.findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL))
                .thenReturn(List.of());
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Selecione pelo menos um lote disponível.");
        verify(repository, never()).save(any());
    }

    @Test
    void br093LoteDeOutroOfertanteRecusado() {
        Ofertante outro = Ofertante.builder().idOfertante(2L).build();
        when(loteService.find(5L)).thenReturn(lote(5L, outro, StatusLote.DISPONIVEL));
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(5L), null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O lote 5 não pertence a este ofertante.");
    }

    @Test
    void br093LoteNaoDisponivelRecusado() {
        when(loteService.find(5L)).thenReturn(lote(5L, banca, StatusLote.RESERVADO));
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, List.of(7L, 5L), null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O lote 5 não está disponível.");
        verify(repository, never()).save(any());
    }

    @Test
    void br094NovoPrazoAtualizaOsLotes() {
        LocalDateTime fimDaFeira = AGORA.plusMinutes(90);
        service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, fimDaFeira, null));
        assertThat(tomates.getDataLimiteRetirada()).isEqualTo(fimDaFeira);
        assertThat(alface.getDataLimiteRetirada()).isEqualTo(fimDaFeira);
        verify(loteRepository).save(tomates);
        verify(loteRepository).save(alface);
    }

    @Test
    void br094NovoPrazoNoPassadoRecusado() {
        assertThatThrownBy(() -> service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, AGORA.minusMinutes(1), null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O novo prazo deve ser no futuro.");
        verify(repository, never()).save(any());
    }

    @Test
    void br094SemNovoPrazoNaoMexeNosLotes() {
        service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void br096AlertaNaoGuardaDestinatarioNemStatus() {
        List<String> campos = Arrays.stream(Alerta.class.getDeclaredFields()).map(Field::getName).toList();
        assertThat(campos).containsExactlyInAnyOrder("idAlerta", "lote", "tipo", "canal", "dataEnvio");
    }

    @Test
    void br097ExpiracaoRodaAntesDeEscolherOsLotes() {
        service.avisarXepa(new AlertaDTO.XepaRequest(1L, null, null, null));
        InOrder order = inOrder(loteService, loteRepository);
        order.verify(loteService).expirarVencidos();
        order.verify(loteRepository).findByOfertante_IdOfertanteAndStatusOrderByDataLimiteRetiradaAsc(1L, StatusLote.DISPONIVEL);
    }

    @Test
    void br097XepaETransacional() throws Exception {
        assertThat(AlertaService.class.getMethod("avisarXepa", AlertaDTO.XepaRequest.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class)).isNotNull();
    }

    // ---------- recentes / CRUD ----------

    @Test
    void recentesPadrao24h() {
        when(repository.recentes(AGORA.minusHours(24))).thenReturn(List.of());
        assertThat(service.recentes(null)).isEmpty();
    }

    @Test
    void recentesHorasForaDoLimite() {
        assertThatThrownBy(() -> service.recentes(0)).isInstanceOf(BusinessRuleException.class)
                .hasMessage("Horas deve ser entre 1 e 168.");
        assertThatThrownBy(() -> service.recentes(169)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br098CriarComCanalPadraoApp() {
        AlertaDTO.Response r = service.criar(new AlertaDTO.Request(7L, TipoAlerta.NOVO_LOTE, null));
        assertThat(r.canal()).isEqualTo(CanalAlerta.APP);
        assertThat(r.dataEnvio()).isEqualTo(AGORA);
    }

    @Test
    void br098TipoObrigatorio() {
        assertThatThrownBy(() -> service.criar(new AlertaDTO.Request(7L, null, CanalAlerta.APP)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do alerta é obrigatório.");
    }

    @Test
    void br098LoteInexistente() {
        when(loteService.find(99L)).thenThrow(NotFoundException.of("Lote", 99L));
        assertThatThrownBy(() -> service.criar(new AlertaDTO.Request(99L, TipoAlerta.XEPA, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizarTipoECanal() {
        Alerta a = Alerta.builder().idAlerta(3L).lote(tomates).tipo(TipoAlerta.NOVO_LOTE).canal(CanalAlerta.APP)
                .dataEnvio(AGORA).build();
        when(repository.findById(3L)).thenReturn(Optional.of(a));
        AlertaDTO.Response r = service.atualizar(3L, new AlertaDTO.Request(null, TipoAlerta.XEPA, CanalAlerta.WHATSAPP));
        assertThat(r.tipo()).isEqualTo(TipoAlerta.XEPA);
        assertThat(r.canal()).isEqualTo(CanalAlerta.WHATSAPP);
    }

    @Test
    void excluirSempre() {
        Alerta a = Alerta.builder().idAlerta(3L).lote(tomates).build();
        when(repository.findById(3L)).thenReturn(Optional.of(a));
        service.excluir(3L);
        verify(repository).delete(a);
    }

    @Test
    void buscarInexistente() {
        when(repository.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(4L)).isInstanceOf(NotFoundException.class)
                .hasMessage("Alerta 4 não encontrado.");
    }
}
