package br.com.fiap.redeprato.service;

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

import br.com.fiap.redeprato.repository.BeneficiarioRepository;
import br.com.fiap.redeprato.repository.EnderecoRepository;
import br.com.fiap.redeprato.repository.OfertanteRepository;
import br.com.fiap.redeprato.repository.PontoDistribuicaoRepository;
import br.com.fiap.redeprato.dto.EnderecoDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository repository;

    @Mock
    private OfertanteRepository ofertanteRepository;

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Mock
    private PontoDistribuicaoRepository pontoRepository;

    @Test
    void br029EnderecoUsadoPorPontoNaoPodeSerExcluido() {
        when(repository.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(pontoRepository.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(5L)).isInstanceOf(ConflictException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void br029EnderecoUsadoPorBeneficiarioNaoPodeSerExcluido() {
        when(repository.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(beneficiarioRepository.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(5L)).isInstanceOf(ConflictException.class);
        verify(repository, never()).delete(any());
    }

    @InjectMocks
    private EnderecoService service;

    private static EnderecoDTO.Request req(String logradouro, String bairro, String cidade, String uf, String cep) {
        return new EnderecoDTO.Request(logradouro, "120", "Banca 12", bairro, cidade, uf, cep);
    }

    private static EnderecoDTO.Request valido() {
        return req("Rua Manuel da Nóbrega", "Paraíso", "São Paulo", "sp", "04001-000");
    }

    private void saveEcho() {
        when(repository.save(any())).thenAnswer(inv -> {
            Endereco e = inv.getArgument(0);
            e.setIdEndereco(5L);
            return e;
        });
    }

    @Test
    void criaEnderecoNormalizandoCepEUf() {
        saveEcho();
        EnderecoDTO.Response r = service.criar(valido());
        assertThat(r.idEndereco()).isEqualTo(5L);
        assertThat(r.cep()).isEqualTo("04001000");
        assertThat(r.uf()).isEqualTo("SP");
        assertThat(r.complemento()).isEqualTo("Banca 12");
    }

    @Test
    void br025LogradouroObrigatorio() {
        assertThatThrownBy(() -> service.criar(req(" ", "Paraíso", "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Logradouro é obrigatório.");
        verify(repository, never()).save(any());
    }

    @Test
    void br025BairroObrigatorio() {
        assertThatThrownBy(() -> service.criar(req("Rua A", null, "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Bairro é obrigatório.");
    }

    @Test
    void br025CidadeObrigatoria() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Cidade é obrigatória.");
    }

    @Test
    void br025UfObrigatoria() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", null, "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF é obrigatória.");
    }

    @Test
    void br025CepObrigatorio() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", "SP", " - ")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP é obrigatório.");
    }

    @Test
    void br026CepComMenosDe8Digitos() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "0400-100")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP deve ter 8 dígitos.");
    }

    @Test
    void br026CepComLetras() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "0400A000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("CEP deve ter 8 dígitos.");
    }

    @Test
    void br026CepComEspacosEPontoEhAceito() {
        saveEcho();
        assertThat(service.criar(req("Rua A", "Paraíso", "São Paulo", "SP", "04.001 000")).cep()).isEqualTo("04001000");
    }

    @Test
    void br027UfInexistente() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", "XX", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF inválida.");
    }

    @Test
    void br027UfComTresLetras() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "São Paulo", "SPX", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("UF deve ter no máximo 2 caracteres.");
    }

    @Test
    void br028LogradouroAte100() {
        assertThatThrownBy(() -> service.criar(req("r".repeat(101), "Paraíso", "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Logradouro deve ter no máximo 100 caracteres.");
    }

    @Test
    void br028NumeroAte10() {
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "12345678901", null, "Paraíso", "São Paulo", "SP", "04001000");
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Número deve ter no máximo 10 caracteres.");
    }

    @Test
    void br028ComplementoAte50() {
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "1", "c".repeat(51), "Paraíso", "São Paulo", "SP", "04001000");
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Complemento deve ter no máximo 50 caracteres.");
    }

    @Test
    void br028BairroECidadeAte60() {
        assertThatThrownBy(() -> service.criar(req("Rua A", "b".repeat(61), "São Paulo", "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Bairro deve ter no máximo 60 caracteres.");
        assertThatThrownBy(() -> service.criar(req("Rua A", "Paraíso", "c".repeat(61), "SP", "04001000")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Cidade deve ter no máximo 60 caracteres.");
    }

    @Test
    void numeroSnAceito() {
        saveEcho();
        EnderecoDTO.Request r = new EnderecoDTO.Request("Rua A", "s/n", null, "Paraíso", "São Paulo", "SP", "04001000");
        assertThat(service.criar(r).numero()).isEqualTo("s/n");
    }

    @Test
    void listarPorBairro() {
        when(repository.findByBairroIgnoreCaseOrderByIdEnderecoAsc("paraíso"))
                .thenReturn(List.of(Endereco.builder().idEndereco(1L).bairro("Paraíso").build()));
        assertThat(service.listar(" paraíso ")).hasSize(1);
    }

    @Test
    void listarSemFiltro() {
        when(repository.findAllByOrderByIdEnderecoAsc()).thenReturn(List.of());
        assertThat(service.listar(null)).isEmpty();
    }

    @Test
    void atualizarInexistenteDa404() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar(9L, valido()))
                .isInstanceOf(NotFoundException.class).hasMessage("Endereço 9 não encontrado.");
    }

    @Test
    void br029EnderecoUsadoPorOfertanteNaoPodeSerExcluido() {
        when(repository.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(ofertanteRepository.existsByEndereco_IdEndereco(5L)).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(5L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível excluir: este endereço está em uso.");
        verify(repository, never()).delete(any());
    }

    @Test
    void excluirEnderecoSemUso() {
        Endereco e = Endereco.builder().idEndereco(6L).build();
        when(repository.findById(6L)).thenReturn(Optional.of(e));
        service.excluir(6L);
        verify(repository).delete(e);
    }

    @Test
    void atualizarValido() {
        when(repository.findById(5L)).thenReturn(Optional.of(Endereco.builder().idEndereco(5L).build()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.atualizar(5L, valido()).logradouro()).isEqualTo("Rua Manuel da Nóbrega");
    }
}
