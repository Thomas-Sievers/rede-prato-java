package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dto.OfertanteDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class OfertanteBOTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private OfertanteDAO dao;
    @Mock
    private EnderecoBO enderecoBO;

    private OfertanteBO bo;
    private final Endereco endereco = Endereco.builder().idEndereco(1L).bairro("Paraíso").build();

    @BeforeEach
    void setUp() {
        bo = new OfertanteBO(dao, enderecoBO, CLOCK);
        lenient().when(enderecoBO.find(1L)).thenReturn(endereco);
        lenient().when(dao.save(any())).thenAnswer(inv -> {
            Ofertante o = inv.getArgument(0);
            if (o.getIdOfertante() == null) {
                o.setIdOfertante(10L);
            }
            return o;
        });
    }

    private static OfertanteDTO.Request req(String nome, TipoOfertante tipo, String tel, String email) {
        return new OfertanteDTO.Request(nome, tipo, tel, email, 1L, null);
    }

    private Ofertante existente(boolean ativo, boolean selo) {
        return Ofertante.builder().idOfertante(3L).nome("Banca").tipo(TipoOfertante.FEIRANTE)
                .telefone("11900000001").ativo(ativo).possuiSelo(selo)
                .dataSelo(selo ? LocalDate.of(2026, 11, 1) : null).endereco(endereco).build();
    }

    @Test
    void criaOfertanteAtivoSemSeloComTelefoneSoDigitos() {
        OfertanteDTO.Response r = bo.criar(req(" Banca do Seu Antônio ", TipoOfertante.FEIRANTE,
                "(11) 90000-0001", null));
        assertThat(r.idOfertante()).isEqualTo(10L);
        assertThat(r.nome()).isEqualTo("Banca do Seu Antônio");
        assertThat(r.telefone()).isEqualTo("11900000001");
        assertThat(r.ativo()).isTrue();
        assertThat(r.possuiSelo()).isFalse();
        assertThat(r.dataSelo()).isNull();
        assertThat(r.bairro()).isEqualTo("Paraíso");
    }

    @Test
    void br031NomeObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("", TipoOfertante.FEIRANTE, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome é obrigatório.");
        verify(dao, never()).save(any());
    }

    @Test
    void br031NomeAte100() {
        assertThatThrownBy(() -> bo.criar(req("n".repeat(101), TipoOfertante.FEIRANTE, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome deve ter no máximo 100 caracteres.");
    }

    @Test
    void br032TipoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("Banca", null, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do ofertante é obrigatório.");
    }

    @Test
    void br033TelefoneObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("Banca", TipoOfertante.FEIRANTE, " ", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Telefone é obrigatório.");
    }

    @Test
    void br033TelefoneCurto() {
        assertThatThrownBy(() -> bo.criar(req("Banca", TipoOfertante.FEIRANTE, "90000-0001", "")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Telefone deve ter 10 ou 11 dígitos (DDD + número).");
    }

    @Test
    void br033TelefoneLongo() {
        assertThatThrownBy(() -> bo.criar(req("Banca", TipoOfertante.FEIRANTE, "5511900000001", null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br033TelefoneFixoCom10DigitosPassa() {
        assertThat(bo.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", null)).telefone())
                .isEqualTo("1130000001");
    }

    @Test
    void br034EmailInvalido() {
        assertThatThrownBy(() -> bo.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", "mercado@")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("E-mail inválido.");
    }

    @Test
    void br034EmailValidoPassa() {
        assertThat(bo.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", " contato@mercado.com ")).email())
                .isEqualTo("contato@mercado.com");
    }

    @Test
    void br034EmailAte100() {
        String email = "a".repeat(95) + "@x.com";
        assertThatThrownBy(() -> bo.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", email)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("E-mail deve ter no máximo 100 caracteres.");
    }

    @Test
    void br035EnderecoObrigatorio() {
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca", TipoOfertante.FEIRANTE, "11900000001", null, null, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Endereço é obrigatório.");
    }

    @Test
    void br035EnderecoInexistente() {
        when(enderecoBO.find(9L)).thenThrow(NotFoundException.of("Endereço", 9L));
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca", TipoOfertante.FEIRANTE, "11900000001", null, 9L, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(NotFoundException.class).hasMessage("Endereço 9 não encontrado.");
    }

    @Test
    void br036SeloInconsistenteRecusado() {
        Ofertante o = existente(true, false);
        o.setDataSelo(LocalDate.of(2026, 1, 1));
        assertThatThrownBy(() -> bo.validate(o))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selo inconsistente: selo e data do selo devem andar juntos.");
    }

    @Test
    void br036SeloSemDataRecusado() {
        Ofertante o = existente(true, false);
        o.setPossuiSelo(true);
        assertThatThrownBy(() -> bo.validate(o)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br037ExclusaoLogica() {
        Ofertante o = existente(true, false);
        when(dao.findById(3L)).thenReturn(Optional.of(o));
        bo.excluir(3L);
        assertThat(o.isAtivo()).isFalse();
        verify(dao).save(o);
        verify(dao, never()).delete(any());
    }

    @Test
    void br037ExclusaoLogicaIdempotente() {
        Ofertante o = existente(false, false);
        when(dao.findById(3L)).thenReturn(Optional.of(o));
        bo.excluir(3L);
        assertThat(o.isAtivo()).isFalse();
        verify(dao, never()).save(any());
    }

    @Test
    void br038ConcedeSeloComDataDeHoje() {
        when(dao.findById(3L)).thenReturn(Optional.of(existente(true, false)));
        OfertanteDTO.Response r = bo.concederSelo(3L);
        assertThat(r.possuiSelo()).isTrue();
        assertThat(r.dataSelo()).isEqualTo(LocalDate.of(2026, 11, 17));
    }

    @Test
    void br038InativoNaoRecebeSelo() {
        when(dao.findById(3L)).thenReturn(Optional.of(existente(false, false)));
        assertThatThrownBy(() -> bo.concederSelo(3L))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode receber o selo.");
    }

    @Test
    void br038SeloDuplicadoDa409() {
        when(dao.findById(3L)).thenReturn(Optional.of(existente(true, true)));
        assertThatThrownBy(() -> bo.concederSelo(3L))
                .isInstanceOf(ConflictException.class).hasMessage("Este ofertante já possui o selo.");
    }

    @Test
    void revogaSeloLimpaData() {
        Ofertante o = existente(true, true);
        when(dao.findById(3L)).thenReturn(Optional.of(o));
        bo.revogarSelo(3L);
        assertThat(o.isPossuiSelo()).isFalse();
        assertThat(o.getDataSelo()).isNull();
    }

    @Test
    void br039PutEmInativoPermitidoEReativa() {
        Ofertante o = existente(false, false);
        when(dao.findById(3L)).thenReturn(Optional.of(o));
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca Nova", TipoOfertante.FEIRANTE, "11900000002", null, 1L, true);
        OfertanteDTO.Response resp = bo.atualizar(3L, r);
        assertThat(resp.ativo()).isTrue();
        assertThat(resp.nome()).isEqualTo("Banca Nova");
    }

    @Test
    void br039PutSemAtivoMantemValorEPreservaSelo() {
        Ofertante o = existente(true, true);
        when(dao.findById(3L)).thenReturn(Optional.of(o));
        OfertanteDTO.Response resp = bo.atualizar(3L, req("Banca", TipoOfertante.FEIRANTE, "11900000001", null));
        assertThat(resp.ativo()).isTrue();
        assertThat(resp.possuiSelo()).isTrue();
        assertThat(resp.dataSelo()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void listarPorPadraoSoAtivos() {
        when(dao.filtrar(null, true, null)).thenReturn(List.of(existente(true, false)));
        assertThat(bo.listar(null, null, " ")).hasSize(1);
    }

    @Test
    void listarInativos() {
        when(dao.filtrar(TipoOfertante.FEIRANTE, false, "Paraíso")).thenReturn(List.of());
        assertThat(bo.listar(TipoOfertante.FEIRANTE, false, "Paraíso")).isEmpty();
    }

    @Test
    void buscarInexistente() {
        when(dao.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(77L))
                .isInstanceOf(NotFoundException.class).hasMessage("Ofertante 77 não encontrado.");
    }
}
