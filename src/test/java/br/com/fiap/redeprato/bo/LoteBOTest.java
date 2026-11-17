package br.com.fiap.redeprato.bo;

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

import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dto.LoteDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class LoteBOTest {

    /** 2026-11-17 10:00 in São Paulo. */
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 17, 10, 0);

    @Mock
    private LoteDAO dao;
    @Mock
    private OfertanteBO ofertanteBO;
    @Mock
    private CategoriaAlimentoBO categoriaBO;

    private LoteBO bo;
    private Ofertante ofertante;
    private final CategoriaAlimento categoria = CategoriaAlimento.builder().idCategoria(2L).nome("Legumes").build();

    @BeforeEach
    void setUp() {
        bo = new LoteBO(dao, ofertanteBO, categoriaBO, CLOCK);
        ofertante = Ofertante.builder().idOfertante(1L).nome("Banca do Seu Antônio").tipo(TipoOfertante.FEIRANTE)
                .ativo(true).endereco(Endereco.builder().bairro("Paraíso").logradouro("Rua X").build()).build();
        lenient().when(ofertanteBO.find(1L)).thenReturn(ofertante);
        lenient().when(categoriaBO.find(2L)).thenReturn(categoria);
        lenient().when(dao.save(any())).thenAnswer(inv -> {
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
        LoteDTO.Response r = bo.criar(doacao());
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
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Descrição é obrigatória.");
        verify(dao, never()).save(any());
    }

    @Test
    void br041DescricaoAte200() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "d".repeat(201), BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Descrição deve ter no máximo 200 caracteres.");
    }

    @Test
    void br042PesoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req(null, ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso é obrigatório.");
    }

    @Test
    void br042PesoZeroRecusado() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.ZERO, ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser maior que zero.");
    }

    @Test
    void br042PesoNegativoRecusado() {
        assertThatThrownBy(() -> bo.criar(req(new BigDecimal("-1"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser maior que zero.");
    }

    @Test
    void br042PesoComTresCasasRecusado() {
        assertThatThrownBy(() -> bo.criar(req(new BigDecimal("1.255"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ter no máximo 2 casas decimais.");
    }

    @Test
    void br042PesoComZerosAMaisPassa() {
        assertThat(bo.criar(req(new BigDecimal("1.500"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))).pesoKg())
                .isEqualByComparingTo("1.5");
    }

    @Test
    void br042PesoMaximo() {
        assertThat(bo.criar(req(new BigDecimal("999999.99"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1)))).isNotNull();
        assertThatThrownBy(() -> bo.criar(req(new BigDecimal("1000000"), ModalidadeLote.DOACAO, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Peso deve ser no máximo 999999,99 kg.");
    }

    @Test
    void br043DoacaoNaoTemPreco() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.TEN, ModalidadeLote.DOACAO, new BigDecimal("5"), AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Doação não tem preço.");
    }

    @Test
    void br044VendaSemPrecoRecusada() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, null, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Lote de venda precisa de preço maior que zero.");
    }

    @Test
    void br044VendaComPrecoZeroRecusada() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, BigDecimal.ZERO, AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Lote de venda precisa de preço maior que zero.");
    }

    @Test
    void br044VendaComPrecoPassa() {
        LoteDTO.Response r = bo.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, new BigDecimal("12.90"), AGORA.plusHours(1)));
        assertThat(r.preco()).isEqualByComparingTo("12.90");
        assertThat(r.modalidade()).isEqualTo(ModalidadeLote.VENDA);
    }

    @Test
    void br044PrecoComTresCasasRecusado() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.TEN, ModalidadeLote.VENDA, new BigDecimal("1.999"), AGORA.plusHours(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Preço deve ter no máximo 2 casas decimais.");
    }

    @Test
    void br045ValorEstimadoNegativoRecusado() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, new BigDecimal("-0.01"), AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Valor estimado não pode ser negativo.");
    }

    @Test
    void br045ValorEstimadoZeroEOpcional() {
        LoteDTO.Request zero = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, BigDecimal.ZERO, AGORA.plusHours(1), null, null);
        LoteDTO.Request nulo = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThat(bo.criar(zero).valorEstimado()).isEqualByComparingTo("0");
        assertThat(bo.criar(nulo).valorEstimado()).isNull();
    }

    @Test
    void br046PrazoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Prazo de retirada é obrigatório.");
    }

    @Test
    void br046PrazoNoPassadoRecusado() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA.minusMinutes(1))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void br046PrazoIgualAAgoraRecusado() {
        assertThatThrownBy(() -> bo.criar(req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void br047OfertanteInativoNaoRegistra() {
        ofertante.setAtivo(false);
        assertThatThrownBy(() -> bo.criar(doacao()))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode registrar lotes.");
    }

    @Test
    void br047OfertanteInexistente() {
        when(ofertanteBO.find(99L)).thenThrow(NotFoundException.of("Ofertante", 99L));
        LoteDTO.Request r = new LoteDTO.Request(99L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void br048CategoriaInexistente() {
        when(categoriaBO.find(98L)).thenThrow(NotFoundException.ofFem("Categoria", 98L));
        LoteDTO.Request r = new LoteDTO.Request(1L, 98L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(NotFoundException.class).hasMessage("Categoria 98 não encontrada.");
    }

    @Test
    void br049StatusSempreDisponivelNaCriacao() {
        assertThat(bo.criar(doacao()).status()).isEqualTo(StatusLote.DISPONIVEL);
    }

    @Test
    void br0412UrlFotoSemHttpRecusada() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "ftp://foto.jpg", null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("URL da foto deve começar com http:// ou https://.");
    }

    @Test
    void br0412UrlFotoAte255() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "https://" + "a".repeat(250), null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("URL da foto deve ter no máximo 255 caracteres.");
    }

    @Test
    void br0412UrlFotoHttpsPassa() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), "https://img.exemplo.com/t.jpg", true);
        LoteDTO.Response resp = bo.criar(r);
        assertThat(resp.urlFoto()).isEqualTo("https://img.exemplo.com/t.jpg");
        assertThat(resp.precisaTransporte()).isTrue();
    }

    @Test
    void br0414CondicaoObrigatoria() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, null,
                ModalidadeLote.DOACAO, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Condição é obrigatória.");
    }

    @Test
    void br0414ModalidadeObrigatoria() {
        LoteDTO.Request r = new LoteDTO.Request(1L, 2L, "Tomates", BigDecimal.ONE, CondicaoLote.BOA,
                null, null, null, AGORA.plusHours(1), null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Modalidade é obrigatória.");
    }

    // ---------- update ----------

    @Test
    void br0410AtualizaLoteDisponivelSemTrocarOfertante() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(dao.findById(7L)).thenReturn(Optional.of(l));
        LoteDTO.Request r = new LoteDTO.Request(55L, 2L, "Tomates bem maduros", new BigDecimal("6"),
                CondicaoLote.CONSUMO_IMEDIATO, ModalidadeLote.DOACAO, null, null, AGORA.plusHours(2), null, null);
        LoteDTO.Response resp = bo.atualizar(7L, r);
        assertThat(resp.descricao()).isEqualTo("Tomates bem maduros");
        assertThat(resp.idOfertante()).isEqualTo(1L);
        assertThat(resp.dataCadastro()).isEqualTo(AGORA.minusHours(1));
    }

    @Test
    void br0410NaoAtualizaLoteReservado() {
        when(dao.findById(7L)).thenReturn(Optional.of(lote(StatusLote.RESERVADO)));
        assertThatThrownBy(() -> bo.atualizar(7L, doacao()))
                .isInstanceOf(ConflictException.class).hasMessage("Só é possível alterar um lote disponível.");
    }

    @Test
    void br0410NaoAtualizaLoteExpiradoOuRetirado() {
        when(dao.findById(7L)).thenReturn(Optional.of(lote(StatusLote.EXPIRADO)));
        assertThatThrownBy(() -> bo.atualizar(7L, doacao())).isInstanceOf(ConflictException.class);
        when(dao.findById(8L)).thenReturn(Optional.of(lote(StatusLote.RETIRADO)));
        assertThatThrownBy(() -> bo.atualizar(8L, doacao())).isInstanceOf(ConflictException.class);
    }

    @Test
    void br046AtualizacaoComPrazoNoPassadoRecusada() {
        when(dao.findById(7L)).thenReturn(Optional.of(lote(StatusLote.DISPONIVEL)));
        assertThatThrownBy(() -> bo.atualizar(7L, req(BigDecimal.ONE, ModalidadeLote.DOACAO, null, AGORA.minusHours(2))))
                .isInstanceOf(BusinessRuleException.class).hasMessage("O prazo de retirada deve ser no futuro.");
    }

    @Test
    void buscarInexistente() {
        when(dao.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(42L))
                .isInstanceOf(NotFoundException.class).hasMessage("Lote 42 não encontrado.");
    }

    // ---------- expiry (BR-04.13) ----------

    @Test
    void br0413TransicaoDisponivelParaExpirado() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(dao.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        assertThat(bo.expirarVencidos()).isEqualTo(1);
        assertThat(l.getStatus()).isEqualTo(StatusLote.EXPIRADO);
    }

    @Test
    void br0413TransicaoReservadoParaExpirado() {
        Lote l = lote(StatusLote.RESERVADO);
        when(dao.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of(l));
        assertThat(bo.expirarVencidos()).isEqualTo(1);
        assertThat(l.getStatus()).isEqualTo(StatusLote.EXPIRADO);
    }

    @Test
    void br0413SemVencidosNaoGrava() {
        when(dao.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of());
        assertThat(bo.expirarVencidos()).isZero();
        verify(dao, never()).saveAll(any());
    }

    @Test
    void listarDisponiveisRodaExpiracaoAntes() {
        when(dao.findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA))).thenReturn(List.of());
        when(dao.disponiveis(AGORA, 2L, ModalidadeLote.DOACAO, "Paraíso")).thenReturn(List.of(lote(StatusLote.DISPONIVEL)));
        assertThat(bo.listarDisponiveis(2L, ModalidadeLote.DOACAO, " Paraíso ")).hasSize(1);
        verify(dao).findByStatusInAndDataLimiteRetiradaBefore(anyCollection(), eq(AGORA));
    }

    // ---------- delete ----------

    @Test
    void excluiLoteSemDependencias() {
        Lote l = lote(StatusLote.DISPONIVEL);
        when(dao.findById(7L)).thenReturn(Optional.of(l));
        bo.excluir(7L);
        verify(dao).delete(l);
    }
}
