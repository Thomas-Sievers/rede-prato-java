package br.com.fiap.redeprato.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.dao.BeneficiarioDAO;
import br.com.fiap.redeprato.dao.EnderecoDAO;
import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dao.PontoDistribuicaoDAO;
import br.com.fiap.redeprato.dto.EnderecoDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class EnderecoBOTest {

    @Mock
    private EnderecoDAO dao;

    @Mock
    private OfertanteDAO ofertanteDAO;

    @Mock
    private BeneficiarioDAO beneficiarioDAO;

    @Mock
    private PontoDistribuicaoDAO pontoDAO;

    @Test
    void br029EnderecoUsadoPorPontoNaoPodeSerExcluido() {
        when(dao.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(pontoDAO.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> bo.excluir(5L)).isInstanceOf(ConflictException.class);
        verify(dao, never()).delete(any());
    }

    @Test
    void br029EnderecoUsadoPorBeneficiarioNaoPodeSerExcluido() {
        when(dao.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(beneficiarioDAO.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> bo.excluir(5L)).isInstanceOf(ConflictException.class);
        verify(dao, never()).delete(any());
    }

    @InjectMocks
    private EnderecoBO bo;

    private static EnderecoDTO.Request req(String logradouro, String bairro, String cidade, String uf, String cep) {
        return new EnderecoDTO.Request(logradouro, "120", "Banca 12", bairro, cidade, uf, cep);
    }

    private static EnderecoDTO.Request valido() {
        return req("Rua Manuel da Nóbrega", "Paraíso", "São Paulo", "sp", "04001-000");
    }

    private void saveEcho() {
        when(dao.save(any())).thenAnswer(inv -> {
            Endereco e = inv.getArgument(0);
            e.setIdEndereco(5L);
            return e;
        });
    }

    @Test
    void criaEnderecoNormalizandoCepEUf() {
        saveEcho();
        EnderecoDTO.Response r = bo.criar(valido());
        assertThat(r.idEndereco()).isEqualTo(5L);
        assertThat(r.cep()).isEqualTo("04001000");
        assertThat(r.uf()).isEqualTo("SP");
        assertThat(r.complemento()).isEqualTo("Banca 12");
    }

    @Test
    void br025LogradouroObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req(" ", "Paraíso", "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Logradouro é obrigatório.");
        verify(dao, never()).save(any());
    }

    @Test
    void br025BairroObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", null, "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Bairro é obrigatório.");
    }

    @Test
    void br025CidadeObrigatoria() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Cidade é obrigatória.");
    }

    @Test
    void br025UfObrigatoria() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", null, "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF é obrigatória.");
    }

    @Test
    void br025CepObrigatorio() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", "SP", " - ")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP é obrigatório.");
    }

    @Test
    void br026CepComMenosDe8Digitos() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "0400-100")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP deve ter 8 dígitos.");
    }

    @Test
    void br026CepComLetras() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "0400A000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP deve ter 8 dígitos.");
    }

    @Test
    void br026CepComEspacosEPontoEhAceito() {
        saveEcho();
        assertThat(bo.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "04.001 000")).cep()).isEqualTo("04001000");
    }

    @Test
    void br027UfInexistente() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", "XX", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF inválida.");
    }

    @Test
    void br027UfComTresLetras() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "São Paulo", "SPX", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF deve ter no máximo 2 caracteres.");
    }

    @Test
    void br028LogradouroAte100() {
        assertThatThrownBy(() -> bo.criar(req("r".repeat(101), "Paraíso", "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Logradouro deve ter no máximo 100 caracteres.");
    }

    @Test
    void br028NumeroAte10() {
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "12345678901", null, "Paraíso", "São Paulo", "SP", "04001000");
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Número deve ter no máximo 10 caracteres.");
    }

    @Test
    void br028ComplementoAte50() {
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "1", "c".repeat(51), "Paraíso", "São Paulo", "SP", "04001000");
        assertThatThrownBy(() -> bo.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Complemento deve ter no máximo 50 caracteres.");
    }

    @Test
    void br028BairroECidadeAte60() {
        assertThatThrownBy(() -> bo.criar(req("Rua A", "b".repeat(61), "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Bairro deve ter no máximo 60 caracteres.");
        assertThatThrownBy(() -> bo.criar(req("Rua A", "Paraíso", "c".repeat(61), "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Cidade deve ter no máximo 60 caracteres.");
    }

    @Test
    void numeroSnAceito() {
        saveEcho();
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "s/n", null, "Paraíso", "São Paulo", "SP", "04001000");
        assertThat(bo.criar(r).numero()).isEqualTo("s/n");
    }

    @Test
    void listarPorBairro() {
        when(dao.findByBairroIgnoreCaseOrderByIdEnderecoAsc("paraíso"))
                .thenReturn(List.of(Endereco.builder().idEndereco(1L).bairro("Paraíso").build()));
        assertThat(bo.listar(" paraíso ")).hasSize(1);
    }

    @Test
    void listarSemFiltro() {
        when(dao.findAllByOrderByIdEnderecoAsc()).thenReturn(List.of());
        assertThat(bo.listar(null)).isEmpty();
    }

    @Test
    void atualizarInexistenteDa404() {
        when(dao.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.atualizar(9L, valido()))
                .isInstanceOf(NotFoundException.class).hasMessage("Endereço 9 não encontrado.");
    }

    @Test
    void br029EnderecoUsadoPorOfertanteNaoPodeSerExcluido() {
        when(dao.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(ofertanteDAO.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> bo.excluir(5L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível excluir: este endereço está em uso.");
        verify(dao, never()).delete(any());
    }

    @Test
    void excluirEnderecoSemUso() {
        Endereco e = Endereco.builder().idEndereco(6L).build();
        when(dao.findById(6L)).thenReturn(Optional.of(e));
        bo.excluir(6L);
        verify(dao).delete(e);
    }

    @Test
    void atualizarValido() {
        when(dao.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(dao.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(bo.atualizar(5L, valido()).logradouro()).isEqualTo("Rua Manuel da Nóbrega");
    }
}
