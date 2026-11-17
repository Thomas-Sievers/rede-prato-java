package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.ReservaDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.entity.enums.TipoPonto;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservaBOTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 10, 0);

    @Mock
    private ReservaDAO dao;
    @Mock
    private LoteDAO loteDAO;
    @Mock
    private LoteBO loteBO;
    @Mock
    private BeneficiarioBO beneficiarioBO;
    @Mock
    private PontoDistribuicaoBO pontoBO;

    private ReservaBO bo;
    private Beneficiario rosana;
    private Lote lote;
    private PontoDistribuicao ponto;

    @BeforeEach
    void setUp() {
        bo = new ReservaBO(dao, loteDAO, loteBO, beneficiarioBO, pontoBO, CLOCK);
        rosana = Beneficiario.builder().idBeneficiario(1L).nome("Rosana").tipo(TipoBeneficiario.FAMILIA)
                .qtdPessoas(4).ativo(true).build();
        Ofertante banca = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio").tipo(TipoOfertante.FEIRANTE)
                .telefone("11900000001").ativo(true)
                .endereco(Endereco.builder().logradouro("Rua Manuel da Nóbrega").numero("s/n")
                        .complemento("Banca 12").bairro("Paraíso").build()).build();
        lote = Lote.builder().idLote(7L).ofertante(banca).categoria(CategoriaAlimento.builder().nome("Legumes").build())
                .descricao("Tomates").pesoKg(new BigDecimal("8.5")).condicao(CondicaoLote.MADURA)
                .modalidade(ModalidadeLote.DOACAO).dataCadastro(AGORA.minusHours(2))
                .dataLimiteRetirada(AGORA.plusHours(8)).status(StatusLote.DISPONIVEL).build();
        ponto = PontoDistribuicao.builder().idPonto(2L).nome("Ponto Paraíso").tipo(TipoPonto.ASSOCIACAO)
                .diasFuncionamento("TER e QUI").horarioAbertura(LocalTime.of(16, 0))
                .horarioFechamento(LocalTime.of(18, 0)).ativo(true)
                .endereco(Endereco.builder().logradouro("Rua Tutóia").numero("300").bairro("Paraíso").build()).build();

        lenient().when(beneficiarioBO.find(1L)).thenReturn(rosana);
        lenient().when(loteBO.find(7L)).thenReturn(lote);
        lenient().when(pontoBO.find(2L)).thenReturn(ponto);
        lenient().when(dao.save(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            if (r.getIdReserva() == null) {
                r.setIdReserva(12L);
            }
            return r;
        });
    }

    private Reserva reserva(StatusReserva status) {
        return Reserva.builder().idReserva(12L).lote(lote).beneficiario(rosana).status(status)
                .dataReserva(AGORA.minusHours(1)).build();
    }

    // ---------- UC12 happy paths ----------

    @Test
    void reservaNoOfertanteMudaLoteParaReservado() {
        ReservaDTO.Response r = bo.criar(new ReservaDTO.Request(7L, 1L, null));
        assertThat(r.idReserva()).isEqualTo(12L);
        assertThat(r.numeroRetirada()).isEqualTo(12L);
        assertThat(r.status()).isEqualTo(StatusReserva.ATIVA);
        assertThat(r.dataReserva()).isEqualTo(AGORA);
        assertThat(r.localRetirada()).isEqualTo("OFERTANTE");
        assertThat(r.enderecoOfertante()).isEqualTo("Rua Manuel da Nóbrega, s/n - Banca 12 - Paraíso");
        assertThat(r.dataLimiteRetirada()).isEqualTo(AGORA.plusHours(8));
        assertThat(lote.getStatus()).isEqualTo(StatusLote.RESERVADO);
        assertThat(r.statusLote()).isEqualTo(StatusLote.RESERVADO);
    }

    @Test
    void reservaNoPontoMostraLocalEHorarios() {
        ReservaDTO.Response r = bo.criar(new ReservaDTO.Request(7L, 1L, 2L));
        assertThat(r.localRetirada()).isEqualTo("PONTO");
        assertThat(r.nomePonto()).isEqualTo("Ponto Paraíso");
        assertThat(r.enderecoPonto()).isEqualTo("Rua Tutóia, 300 - Paraíso");
        assertThat(r.diasFuncionamento()).isEqualTo("TER e QUI");
        assertThat(r.horarioAbertura()).isEqualTo("16:00");
        assertThat(r.horarioFechamento()).isEqualTo("18:00");
    }

    @Test
    void br078ReservaELoteSalvosNaMesmaOperacaoNaOrdem() {
        bo.criar(new ReservaDTO.Request(7L, 1L, null));
        InOrder order = inOrder(loteBO, dao, loteDAO);
        order.verify(loteBO).expirarVencidos();
        order.verify(dao).save(any(Reserva.class));
        order.verify(loteDAO).save(lote);
    }

    @Test
    void br078CriarETransacional() throws Exception {
        Method m = ReservaBO.class.getMethod("criar", ReservaDTO.Request.class);
        Transactional tx = m.getAnnotation(Transactional.class);
        assertThat(tx).isNotNull();
        assertThat(tx.readOnly()).isFalse();
        // default rollback rules cover RuntimeException: a failing lote save rolls back the reserva insert
        assertThat(Arrays.asList(tx.noRollbackFor())).isEmpty();
    }

    @Test
    void br078FalhaAoSalvarLotePropagaParaRollback() {
        when(loteDAO.save(lote)).thenThrow(new IllegalStateException("falha"));
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- UC12 rules ----------

    @Test
    void br071BeneficiarioInexistente() {
        when(beneficiarioBO.find(9L)).thenThrow(NotFoundException.of("Beneficiário", 9L));
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 9L, null)))
                .isInstanceOf(NotFoundException.class).hasMessage("Beneficiário 9 não encontrado.");
        verify(dao, never()).save(any());
    }

    @Test
    void br071BeneficiarioInativo() {
        rosana.setAtivo(false);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Beneficiário inativo não pode reservar lotes.");
        verify(dao, never()).save(any());
    }

    @Test
    void br072LoteInexistente() {
        when(loteBO.find(99L)).thenThrow(NotFoundException.of("Lote", 99L));
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(99L, 1L, null)))
                .isInstanceOf(NotFoundException.class).hasMessage("Lote 99 não encontrado.");
    }

    @Test
    void br073LoteExpiradoNaoPodeSerReservado() {
        lote.setStatus(StatusLote.EXPIRADO);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deste lote já passou.");
        verify(dao, never()).save(any());
        verify(loteDAO, never()).save(any());
    }

    @Test
    void br073PrazoVencidoMesmoAindaDisponivel() {
        lote.setDataLimiteRetirada(AGORA.minusMinutes(1));
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deste lote já passou.");
    }

    @Test
    void br073PrazoExatamenteAgoraRecusado() {
        lote.setDataLimiteRetirada(AGORA);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br073ExpiracaoRodaAntesDeBuscarOLote() {
        bo.criar(new ReservaDTO.Request(7L, 1L, null));
        InOrder order = inOrder(loteBO);
        order.verify(loteBO).expirarVencidos();
        order.verify(loteBO).find(7L);
    }

    @Test
    void br074SegundaReservaDoMesmoLoteRecusada() {
        lote.setStatus(StatusLote.RESERVADO);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(ConflictException.class).hasMessage("Este lote já foi reservado por outra pessoa.");
        verify(dao, never()).save(any());
    }

    @Test
    void br074LoteRetiradoRecusado() {
        lote.setStatus(StatusLote.RETIRADO);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void br075ReservaAtivaExistenteRecusada() {
        when(dao.existsByLote_IdLoteAndStatus(7L, StatusReserva.ATIVA)).thenReturn(true);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, null)))
                .isInstanceOf(ConflictException.class).hasMessage("Este lote já tem uma reserva ativa.");
    }

    @Test
    void br076PontoInexistente() {
        when(pontoBO.find(5L)).thenThrow(NotFoundException.of("Ponto de distribuição", 5L));
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, 5L))).isInstanceOf(NotFoundException.class);
        verify(dao, never()).save(any());
    }

    @Test
    void br076PontoInativo() {
        ponto.setAtivo(false);
        assertThatThrownBy(() -> bo.criar(new ReservaDTO.Request(7L, 1L, 2L)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Este ponto de distribuição não está ativo.");
    }

    @Test
    void br077ReservaPegaOLoteInteiro() {
        ReservaDTO.Response r = bo.criar(new ReservaDTO.Request(7L, 1L, null));
        assertThat(r.pesoKg()).isEqualByComparingTo("8.5");
        assertThat(Arrays.stream(Reserva.class.getDeclaredFields()).map(f -> f.getName().toLowerCase()))
                .noneMatch(n -> n.contains("peso") || n.contains("kg"));
    }

    @Test
    void br0711VendaMostraPrecoEPagamentoNaRetirada() {
        lote.setModalidade(ModalidadeLote.VENDA);
        lote.setPreco(new BigDecimal("12.90"));
        ReservaDTO.Response r = bo.criar(new ReservaDTO.Request(7L, 1L, null));
        assertThat(r.preco()).isEqualByComparingTo("12.90");
        assertThat(r.pagamentoNaRetirada()).isTrue();
    }

    @Test
    void br0711DoacaoNaoTemPagamento() {
        assertThat(bo.criar(new ReservaDTO.Request(7L, 1L, null)).pagamentoNaRetirada()).isFalse();
    }

    // ---------- cancel (UC14) ----------

    @Test
    void br079CancelarDevolveLoteParaDisponivel() {
        lote.setStatus(StatusLote.RESERVADO);
        Reserva r = reserva(StatusReserva.ATIVA);
        when(dao.findById(12L)).thenReturn(Optional.of(r));
        ReservaDTO.Response resp = bo.cancelar(12L);
        assertThat(resp.status()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.DISPONIVEL);
        verify(loteDAO).save(lote);
    }

    @Test
    void br079CancelarDepoisDoPrazoDeixaLoteExpirado() {
        lote.setStatus(StatusLote.RESERVADO);
        lote.setDataLimiteRetirada(AGORA.minusMinutes(5));
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.ATIVA)));
        bo.cancelar(12L);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.EXPIRADO);
    }

    @Test
    void br079CancelarReservaNaoAtivaRecusado() {
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.RETIRADA)));
        assertThatThrownBy(() -> bo.cancelar(12L))
                .isInstanceOf(ConflictException.class).hasMessage("Só é possível cancelar uma reserva ativa.");
        when(dao.findById(13L)).thenReturn(Optional.of(reserva(StatusReserva.CANCELADA)));
        assertThatThrownBy(() -> bo.cancelar(13L)).isInstanceOf(ConflictException.class);
    }

    // ---------- retirada (UC09, feature 08) ----------

    @Test
    void retiradaTransicaoReservadoParaRetirado() {
        lote.setStatus(StatusLote.RESERVADO);
        Reserva r = reserva(StatusReserva.ATIVA);
        when(dao.findById(12L)).thenReturn(Optional.of(r));
        ReservaDTO.Response resp = bo.confirmarRetirada(12L);
        assertThat(resp.status()).isEqualTo(StatusReserva.RETIRADA);
        assertThat(resp.dataRetirada()).isEqualTo(AGORA);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.RETIRADO);
        verify(loteDAO).save(lote);
    }

    @Test
    void br081RetiradaSoDeReservaAtiva() {
        lote.setStatus(StatusLote.RESERVADO);
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.CANCELADA)));
        assertThatThrownBy(() -> bo.confirmarRetirada(12L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Só é possível confirmar a retirada de uma reserva ativa.");
        when(dao.findById(13L)).thenReturn(Optional.of(reserva(StatusReserva.RETIRADA)));
        assertThatThrownBy(() -> bo.confirmarRetirada(13L)).isInstanceOf(ConflictException.class);
        verify(loteDAO, never()).save(any());
    }

    @Test
    void br082LoteDaReservaPrecisaEstarReservado() {
        lote.setStatus(StatusLote.EXPIRADO);
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.ATIVA)));
        assertThatThrownBy(() -> bo.confirmarRetirada(12L))
                .isInstanceOf(ConflictException.class).hasMessage("O lote desta reserva não está reservado.");
    }

    // ---------- update / delete ----------

    @Test
    void br0712TrocarPontoDeReservaAtiva() {
        Reserva r = reserva(StatusReserva.ATIVA);
        when(dao.findById(12L)).thenReturn(Optional.of(r));
        assertThat(bo.atualizar(12L, new ReservaDTO.UpdateRequest(2L)).localRetirada()).isEqualTo("PONTO");
        assertThat(bo.atualizar(12L, new ReservaDTO.UpdateRequest(null)).localRetirada()).isEqualTo("OFERTANTE");
    }

    @Test
    void br0712NaoAlteraReservaNaoAtiva() {
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.CANCELADA)));
        assertThatThrownBy(() -> bo.atualizar(12L, new ReservaDTO.UpdateRequest(2L)))
                .isInstanceOf(ConflictException.class).hasMessage("Só é possível alterar uma reserva ativa.");
    }

    @Test
    void br0713ExcluiSoCancelada() {
        Reserva r = reserva(StatusReserva.CANCELADA);
        when(dao.findById(12L)).thenReturn(Optional.of(r));
        bo.excluir(12L);
        verify(dao).delete(r);
    }

    @Test
    void br0713NaoExcluiAtiva() {
        when(dao.findById(12L)).thenReturn(Optional.of(reserva(StatusReserva.ATIVA)));
        assertThatThrownBy(() -> bo.excluir(12L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Só é possível excluir reservas canceladas. Cancele a reserva primeiro.");
        verify(dao, never()).delete(any());
    }

    @Test
    void buscarInexistente() {
        when(dao.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(77L))
                .isInstanceOf(NotFoundException.class).hasMessage("Reserva 77 não encontrada.");
    }

    @Test
    void listarDelegaFiltros() {
        when(dao.filtrar(1L, null, StatusReserva.ATIVA, null)).thenReturn(List.of(reserva(StatusReserva.ATIVA)));
        assertThat(bo.listar(1L, null, StatusReserva.ATIVA, null)).hasSize(1);
    }
}
