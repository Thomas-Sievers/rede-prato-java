package br.com.fiap.redeprato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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

import br.com.fiap.redeprato.repository.LoteRepository;
import br.com.fiap.redeprato.repository.ReservaRepository;
import br.com.fiap.redeprato.dto.LoteDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    /** 2026-11-17 10:00 in São Paulo. */
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 10, 0);

    @Mock
    private LoteRepository repository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private br.com.fiap.redeprato.repository.DestinacaoRepository destinacaoRepository;
    @Mock
    private br.com.fiap.redeprato.repository.AlertaRepository alertaRepository;

    @Test
    void hu05CriarLoteGeraAlertaNovoLoteNoApp() {
        service.criar(doacao());
        org.mockito.ArgumentCaptor<br.com.fiap.redeprato.entity.Alerta> cap =
                org.mockito.ArgumentCaptor.forClass(br.com.fiap.redeprato.entity.Alerta.class);
        verify(alertaRepository).save(cap.capture());
        assertThat(cap.getValue().getTipo()).isEqualTo(br.com.fiap.redeprato.entity.enums.TipoAlerta.NOVO_LOTE);
        assertThat(cap.getValue().getCanal()).isEqualTo(br.com.fiap.redeprato.entity.enums.CanalAlerta.APP);
        assertThat(cap.getValue().getDataEnvio()).isEqualTo(AGORA);
        assertThat(cap.getValue().getLote().getIdLote()).isEqualTo(7L);
    }

    @Test
    void loteInvalidoNaoGeraAlerta() {
        assertThatThrownBy(() -> service.criar(req(null, ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void excluirLoteApagaAlertasJunto() {
        Lote l = lote(StatusLote.DISPONIVEL);
        br.com.fiap.redeprato.entity.Alerta a = br.com.fiap.redeprato.entity.Alerta.builder().idAlerta(1L).lote(l).build();
        when(repository.findById(7L)).thenReturn(Optional.of(l));
        when(alertaRepository.findByLote_IdLote(7L)).thenReturn(List.of(a));
        service.excluir(7L);
        org.mockito.InOrder order = org.mockito.Mockito.inOrder(alertaRepository, repository);
        order.verify(alertaRepository).deleteAll(List.of(a));
        order.verify(repository).delete(l);
    }

    @Test
    void br0411NaoExcluiLoteComDestinacao() {
        when(repository.findById(7L)).thenReturn(Optional.of(lote(StatusLote.EXPIRADO)));
        when(destinacaoRepository.existsByLote_IdLote(7L)).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(7L)).isInstanceOf(ConflictException.class);
        verify(repository, never()).delete(any());
    }
    @Mock
    private OfertanteService ofertanteService;
    @Mock
    private CategoriaAlimentoService categoriaService;

    private LoteService service;
    private Ofertante ofertante;
    private final CategoriaAlimento categoria = CategoriaAlimento.builder().idCategoria(2L).nome("Legumes").build();

    @BeforeEach
    void setUp() {
        service =new LoteService(repository, reservaRepository, destinacaoRepository, alertaRepository, ofertanteService, categoriaService, CLOCK);
        ofertante = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio").tipo(TipoOfertante.FEIRANTE)
                .ativo(true).endereco(Endereco.builder().bairro("Paraíso").logradouro("Rua X").build()).build();
        lenient().when(ofertanteService.find(1L)).thenReturn(ofertante);
        lenient().when(categoriaService.find(2L)).thenReturn(categoria);
        lenient().when(repository.save(any())).thenAnswer(inv -> {
            Lote l = inv.getArgument(0);
            if (l.getIdLote() == null) {
                l.setIdLote(7L);
            }
            return l;
        });
    }

    static LoteDTO.Request req(BigDecimal peso, ModalidadeLote mod, BigDecimal preco, LocalDateTime prazo) {
        return new LoteDTO.Request(1L, 2L, "Caixa de tomates maduros", peso, CondicaoLote.MADURA, mod, preco,
                new BigDecimal("40.00"), prazo, null, null);
    }

    static LoteDTO.Request doacao() {
        return req(new BigDecimal("8.5"), ModalidadeLote.DOACAO, null, AGORA.plusHours(8));
    }

    private Lote lote(StatusLote status) {
        return Lote.builder().idLote(7L).ofertante(ofertante).categoria(categoria).descricao("Tomates")
                .pesoKg(new BigDecimal("8.5")).condicao(CondicaoLote.BOA).modalidade(ModalidadeLote.DOACAO)
                .dataCadastro(AGORA.minusHours(1)).dataLimiteRetirada(AGORA.plusHours(4)).status(status).build();
    }

    // ---------- create ----------

    @Test
    void criaLoteDisponivelComDataCadastroAgora() {
        LoteDTO.Response r = service.criar(doacao());
        assertThat(r.idLote()).isEqualTo(7L);
        assertThat(r.status()).isEqualTo(StatusLote.DISPONIVEL);
        assertThat(r.dataCadastro()).isEqualTo(AGORA);
        assertThat(r.nomeOfertante()).isEqualTo("Banca do Seu Antônio");
        assertThat(r.nomeCategoria()).isEqualTo("Legumes");
        assertThat(r.bairro()).isEqualTo("Paraíso");
        assertThat(r.precisaTransporte()).isFalse();
    }

    @Test
    void br041DescricaoObrigatoria() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, " ", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Descrição é obrigatória.");
        verify(repository, never()).save(any());
    }

    @Test
    void br041DescricaoAte200() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "d".repeat(201), BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Descrição deve ter no máximo 200 caracteres.");
    }

    @Test
    void br042PesoObrigatorio() {
        assertThatThrownBy(() -> service.criar(req(null, ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso é obrigatório.");
    }

    @Test
    void br042PesoZeroRecusado() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.ZERO, ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser maior que zero.");
    }

    @Test
    void br042PesoNegativoRecusado() {
        assertThatThrownBy(() -> service.criar(req(new BigDecimal("-1"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser maior que zero.");
    }

    @Test
    void br042PesoComTresCasasRecusado() {
        assertThatThrownBy(() -> service.criar(req(new BigDecimal("1.255"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ter no máximo 2 casas decimais.");
    }

    @Test
    void br042PesoComZerosAMaisPassa() {
        assertThat(service.criar(req(new BigDecimal("1.500"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))).pesoKg())
                .isEqualByComparingTo("1.5");
    }

    @Test
    void br042PesoMaximo() {
        assertThat(service.criar(req(new BigDecimal("999999.99"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1)))).isNotNull();
        assertThatThrownBy(() -> service.criar(req(new BigDecimal("1000000"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser no máximo 999999,99 kg.");
    }

    @Test
    void br043DoacaoNaoTemPreco() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.TEN, ModalidadeLote.DOACAO, new BigDecimal("5"), AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Doação não tem preço.");
    }

    @Test
    void br044VendaSemPrecoRecusada() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Lote de venda precisa de preço maior que zero.");
    }

    @Test
    void br044VendaComPrecoZeroRecusada() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, BigDecimal.ZERO, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Lote de venda precisa de preço maior que zero.");
    }

    @Test
    void br044VendaComPrecoPassa() {
        LoteDTO.Response r = service.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, new BigDecimal("12.90"), AGORA.plusHours(1)));
        assertThat(r.preco()).isEqualByComparingTo("12.90");
        assertThat(r.modalidade()).isEqualTo(ModalidadeLote.VENDA);
    }

    @Test
    void br044PrecoComTresCasasRecusado() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, new BigDecimal("1.999"), AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Preço deve ter no máximo 2 casas decimais.");
    }

    @Test
    void br045ValorEstimadoNegativoRecusado() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, new BigDecimal("-0.01"), AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Valor estimado não pode ser negativo.");
    }

    @Test
    void br045ValorEstimadoZeroEOpcional() {
        LoteDTO.Request zero = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, BigDecimal.ZERO, AGORA.plusHours(1), null, null);
        LoteDTO.Request nulo = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThat(service.criar(zero).valorEstimado()).isEqualByComparingTo("0");
        assertThat(service.criar(nulo).valorEstimado()).isNull();
    }

    @Test
    void br046PrazoObrigatorio() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Prazo de retirada é obrigatório.");
    }

    @Test
    void br046PrazoNoPassadoRecusado() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA.minusMinutes(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void br046PrazoIgualAAgoraRecusado() {
        assertThatThrownBy(() -> service.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void br047OfertanteInativoNaoRegistra() {
        ofertante.setAtivo(false);
        assertThatThrownBy(() -> service.criar(doacao()))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode registrar lotes.");
    }

    @Test
    void br047OfertanteInexistente() {
        when(ofertanteService.find(99L)).thenThrow(NotFoundException.of("Ofertante", 99L));
        LoteDTO.Request r = new LoteDTO.Request(99L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void br048CategoriaInexistente() {
        when(categoriaService.find(98L)).thenThrow(NotFoundException.ofFem("Categoria", 98L));
        LoteDTO.Request r = new LoteDTO.Request(1L, 98L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(NotFoundException.class).hasMessage("Categoria 98 não encontrada.");
    }

    @Test
    void br049StatusSempreDisponivelNaCriacao() {
        assertThat(service.criar(doacao()).status()).isEqualTo(StatusLote.DISPONIVEL);
    }

    @Test
    void br0412UrlFotoSemHttpRecusada() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "ftp://foto.jpg", null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("URL da foto deve começar com http:// ou https://.");
    }

    @Test
    void br0412UrlFotoAte255() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "https://" + "a".repeat(250), null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("URL da foto deve ter no máximo 255 caracteres.");
    }

    @Test
    void br0412UrlFotoHttpsPassa() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "https://img.exemplo.com/t.jpg", true);
        LoteDTO.Response resp = service.criar(r);
        assertThat(resp.urlFoto()).isEqualTo("https://img.exemplo.com/t.jpg");
        assertThat(resp.precisaTransporte()).isTrue();
    }

    @Test
    void br0414CondicaoObrigatoria() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, null,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Condição é obrigatória.");
    }

    @Test
    void br0414ModalidadeObrigatoria() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                null, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Modalidade é obrigatória.");
    }

    // ---------- update ----------

    @Test
    void br0410AtualizaLoteDisponivelSemTrocarOfertante() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(repository.findById(7L)).thenReturn(Optional.of(l));
        LoteDTO.Request r = new LoteDTO.Request(55L, 2L, "Tomates bem maduros", new BigDecimal("6"),
                CondicaoLote.CONSUMO_IMEDIATO, ModalidadeLote.DOACAO, null, null, AGORA.plusHours(2), null, null);
        LoteDTO.Response resp = service.atualizar(7L, r);
        assertThat(resp.descricao()).isEqualTo("Tomates bem maduros");
        assertThat(resp.idOfertante()).isEqualTo(1L);
        assertThat(resp.dataCadastro()).isEqualTo(AGORA.minusHours(1));
    }

    @Test
    void br0410NaoAtualizaLoteReservado() {
        when(repository.findById(7L)).thenReturn(Optional.of(lote(StatusLote.RESERVADO)));
        assertThatThrownBy(() -> service.atualizar(7L, doacao()))
                .isInstanceOf(ConflictException.class).hasMessage("Só é possível alterar um lote disponível.");
    }

    @Test
    void br0410NaoAtualizaLoteExpiradoOuRetirado() {
        when(repository.findById(7L)).thenReturn(Optional.of(lote(StatusLote.EXPIRADO)));
        assertThatThrownBy(() -> service.atualizar(7L, doacao())).isInstanceOf(ConflictException.class);
        when(repository.findById(8L)).thenReturn(Optional.of(lote(StatusLote.RETIRADO)));
        assertThatThrownBy(() -> service.atualizar(8L, doacao())).isInstanceOf(ConflictException.class);
    }

    @Test
    void br046AtualizacaoComPrazoNoPassadoRecusada() {
        when(repository.findById(7L)).thenReturn(Optional.of(lote(StatusLote.DISPONIVEL)));
        assertThatThrownBy(() -> service.atualizar(7L, req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA.minusHours(2))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void buscarInexistente() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(42L))
                .isInstanceOf(NotFoundException.class).hasMessage("Lote 42 não encontrado.");
    }

    // ---------- expiry (BR-04.13) ----------

    @Test
    void br0413TransicaoDisponivelParaExpirado() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        assertThat(service.expirarVencidos()).isEqualTo(1);
        assertThat(l.getStatus()).isEqualTo(StatusLote.EXPIRADO);
    }

    @Test
    void br0413TransicaoReservadoParaExpirado() {
        Lote l = lote(StatusLote.RESERVADO);
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        assertThat(service.expirarVencidos()).isEqualTo(1);
        assertThat(l.getStatus()).isEqualTo(StatusLote.EXPIRADO);
    }

    @Test
    void br0413SemVencidosNaoGrava() {
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of());
        assertThat(service.expirarVencidos()).isZero();
        verify(repository, never()).saveAll(any());
    }

    @Test
    void listarDisponiveisRodaExpiracaoAntes() {
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of());
        when(repository.disponiveis(AGORA, 2L, ModalidadeLote.DOACAO, "Paraíso")).thenReturn(List.of(lote(StatusLote.DISPONIVEL)));
        assertThat(service.listarDisponiveis(2L, ModalidadeLote.DOACAO, " Paraíso ")).hasSize(1);
        verify(repository).findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA));
    }

    @Test
    void br183FiltroPrecisaTransporte() {
        Lote l = lote(StatusLote.DISPONIVEL);
        l.setPrecisaTransporte(true);
        when(repository.filtrar(null, null, null, null, true)).thenReturn(List.of(l));
        assertThat(service.listar(null, null, null, null, true)).singleElement()
                .satisfies(r -> assertThat(r.precisaTransporte()).isTrue());
    }

    // ---------- delete ----------

    @Test
    void excluiLoteSemDependencias() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(repository.findById(7L)).thenReturn(Optional.of(l));
        service.excluir(7L);
        verify(repository).delete(l);
    }

    @Test
    void br0411NaoExcluiLoteComReservaAtivaOuRetirada() {
        when(repository.findById(7L)).thenReturn(Optional.of(lote(StatusLote.RESERVADO)));
        when(reservaRepository.existsByLote_IdLoteAndStatusIn(eq(7L), anyCollection())).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(7L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível excluir: o lote tem reserva ativa ou retirada, ou destino registrado.");
        verify(repository, never()).delete(any());
    }

    @Test
    void br0411ExcluiReservasCanceladasJunto() {
        Lote l = lote(StatusLote.DISPONIVEL);
        Reserva cancelada = Reserva.builder().idReserva(3L).lote(l).status(StatusReserva.CANCELADA).build();
        when(repository.findById(7L)).thenReturn(Optional.of(l));
        when(reservaRepository.findByLote_IdLoteAndStatus(7L, StatusReserva.CANCELADA)).thenReturn(List.of(cancelada));
        service.excluir(7L);
        org.mockito.InOrder order = org.mockito.Mockito.inOrder(reservaRepository, repository);
        order.verify(reservaRepository).deleteAll(List.of(cancelada));
        order.verify(repository).delete(l);
    }

    @Test
    void br0413ExpirarReservadoCancelaReservaAtiva() {
        Lote l = lote(StatusLote.RESERVADO);
        Reserva ativa = Reserva.builder().idReserva(3L).lote(l).status(StatusReserva.ATIVA).build();
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        when(reservaRepository.findFirstByLote_IdLoteAndStatus(7L, StatusReserva.ATIVA)).thenReturn(Optional.of(ativa));
        service.expirarVencidos();
        assertThat(l.getStatus()).isEqualTo(StatusLote.EXPIRADO);
        assertThat(ativa.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        verify(reservaRepository).save(ativa);
    }

    @Test
    void br0413ExpirarDisponivelNaoMexeEmReservas() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(repository.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        service.expirarVencidos();
        verify(reservaRepository, never()).findFirstByLote_IdLoteAndStatus(any(), any());
    }
}
