package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.PontoDistribuicaoDAO;
import br.com.fiap.redeprato.dto.PontoDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.enums.TipoPonto;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class PontoDistribuicaoBOTest {

    @Mock
    private PontoDistribuicaoDAO dao;
    @Mock
    private EnderecoBO enderecoBO;

    private PontoDistribuicaoBO bo;

    @BeforeEach
    void setUp() {
        bo = new PontoDistribuicaoBO(dao, enderecoBO);
        lenient().when(enderecoBO.find(3L)).thenReturn(Endereco.builder().idEndereco(3L).bairro("Paraíso").build());
        lenient().when(dao.save(any())).thenAnswer(inv -> {
            PontoDistribuicao p = inv.getArgument(0);
            if (p.getIdPonto() == null) {
                p.setIdPonto(1L);
            }
            return p;
        });
    }

    private static PontoDTO.Request req(String abre, String fecha) {
        return new PontoDTO.Request("Ponto Paraíso", TipoPonto.ASSOCIACAO, "TER e QUI", abre, fecha, null, 3L, null);
    }

    @Test
    void criaPontoComHorariosHHMM() {
        PontoDTO.Response r = bo.criar(req("16:00", "18:00"));
        assertThat(r.idPonto()).isEqualTo(1L);
        assertThat(r.horarioAbertura()).isEqualTo("16:00");
        assertThat(r.horarioFechamento()).isEqualTo("18:00");
        assertThat(r.ativo()).isTrue();
        assertThat(r.bairro()).isEqualTo("Paraíso");
    }

    @Test
    void br061NomeObrigatorio() {
        PontoDTO.Request r = new PontoDTO.Request(" ", TipoPonto.ONG, "SAB", "09:00", "12:00", null, 3L, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(BusinessRuleException.class).hasMessage("Nome é obrigatório.");
        verify(dao, never()).save(any());
    }

    @Test
    void br062TipoObrigatorio() {
        PontoDTO.Request r = new PontoDTO.Request("ONG", null, "SAB", "09:00", "12:00", null, 3L, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do ponto é obrigatório.");
    }

    @Test
    void br063DiasObrigatorio() {
        PontoDTO.Request r = new PontoDTO.Request("ONG", TipoPonto.ONG, null, "09:00", "12:00", null, 3L, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Dias de funcionamento é obrigatório.");
    }

    @Test
    void br063DiasAte50() {
        PontoDTO.Request r = new PontoDTO.Request("ONG", TipoPonto.ONG, "d".repeat(51), "09:00", "12:00", null, 3L, null);
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Dias de funcionamento deve ter no máximo 50 caracteres.");
    }

    @Test
    void br064AberturaObrigatoria() {
        assertThatThrownBy(() -> bo.criar(req(null, "18:00")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Horário de abertura é obrigatório.");
    }

    @Test
    void br064FechamentoObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("16:00", "")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Horário de fechamento é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"9h", "9:00", "24:00", "16:60", "16-00", "16:00:00", "ab:cd"})
    void br064FormatoInvalido(String horario) {
        assertThatThrownBy(() -> bo.criar(req(horario, "23:00")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Horário deve estar no formato HH:MM.");
    }

    @Test
    void br064LimitesValidos() {
        assertThat(bo.criar(req("00:00", "23:59")).horarioFechamento()).isEqualTo("23:59");
    }

    @Test
    void br065AberturaIgualFechamentoRecusada() {
        assertThatThrownBy(() -> bo.criar(req("16:00", "16:00")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Horário de abertura deve ser antes do fechamento.");
    }

    @Test
    void br065AberturaDepoisDoFechamentoRecusada() {
        assertThatThrownBy(() -> bo.criar(req("18:00", "16:00"))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br066EnderecoObrigatorio() {
        PontoDTO.Request r = new PontoDTO.Request("ONG", TipoPonto.ONG, "SAB", "09:00", "12:00", null, null, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(BusinessRuleException.class).hasMessage("Endereço é obrigatório.");
    }

    @Test
    void br066EnderecoInexistente() {
        when(enderecoBO.find(9L)).thenThrow(NotFoundException.of("Endereço", 9L));
        PontoDTO.Request r = new PontoDTO.Request("ONG", TipoPonto.ONG, "SAB", "09:00", "12:00", null, 9L, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void br067TelefoneOpcionalValidado() {
        PontoDTO.Request r = new PontoDTO.Request("ONG", TipoPonto.ONG, "SAB", "09:00", "12:00", "123", 3L, null);
        assertThatThrownBy(() -> bo.criar(r)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br067ExclusaoLogica() {
        PontoDistribuicao p = PontoDistribuicao.builder().idPonto(1L).nome("P").tipo(TipoPonto.ONG)
                .diasFuncionamento("SAB").horarioAbertura(LocalTime.of(9, 0)).horarioFechamento(LocalTime.of(12, 0))
                .ativo(true).build();
        when(dao.findById(1L)).thenReturn(Optional.of(p));
        bo.excluir(1L);
        assertThat(p.isAtivo()).isFalse();
        verify(dao, never()).delete(any());
    }

    @Test
    void reativaPorPut() {
        PontoDistribuicao p = PontoDistribuicao.builder().idPonto(1L).ativo(false).build();
        when(dao.findById(1L)).thenReturn(Optional.of(p));
        PontoDTO.Request r = new PontoDTO.Request("Ponto", TipoPonto.ONG, "SAB", "09:00", "12:00", null, 3L, true);
        assertThat(bo.atualizar(1L, r).ativo()).isTrue();
    }

    @Test
    void listarPadraoAtivos() {
        when(dao.filtrar(true, "Paraíso")).thenReturn(List.of());
        assertThat(bo.listar(null, "Paraíso")).isEmpty();
    }

    @Test
    void buscarInexistente() {
        when(dao.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(5L))
                .isInstanceOf(NotFoundException.class).hasMessage("Ponto de distribuição 5 não encontrado.");
    }
}
