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

import br.com.fiap.redeprato.repository.CategoriaAlimentoRepository;
import br.com.fiap.redeprato.repository.LoteRepository;
import br.com.fiap.redeprato.dto.CategoriaDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoriaAlimentoServiceTest {

    @Mock
    private CategoriaAlimentoRepository repository;

    @Mock
    private LoteRepository loteRepository;

    @Test
    void br024CategoriaUsadaPorLoteNaoPodeSerExcluida() {
        when(repository.findById(3L)).thenReturn(Optional.of(cat(3L, "Frutas")));
        when(loteRepository.existsByCategoria_IdCategoria(3L)).thenReturn(true);
        assertThatThrownBy(() -> service.excluir(3L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível excluir: existem lotes nesta categoria.");
        verify(repository, never()).delete(any());
    }

    @InjectMocks
    private CategoriaAlimentoService service;

    private static CategoriaAlimento cat(Long id, String nome) {
        return CategoriaAlimento.builder().idCategoria(id).nome(nome).build();
    }

    @Test
    void criaCategoriaComNomeAparado() {
        when(repository.existsByNomeIgnoreCase("Frutas")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            CategoriaAlimento c = inv.getArgument(0);
            c.setIdCategoria(1L);
            return c;
        });

        CategoriaDTO.Response r = service.criar(new CategoriaDTO.Request("  Frutas ", "  "));

        assertThat(r.idCategoria()).isEqualTo(1L);
        assertThat(r.nome()).isEqualTo("Frutas");
        assertThat(r.descricao()).isNull();
    }

    @Test
    void br021NomeObrigatorio() {
        assertThatThrownBy(() -> service.criar(new CategoriaDTO.Request("   ", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nome da categoria é obrigatório.");
        verify(repository, never()).save(any());
    }

    @Test
    void br021NomeAte50Caracteres() {
        assertThatThrownBy(() -> service.criar(new CategoriaDTO.Request("x".repeat(51), null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nome da categoria deve ter no máximo 50 caracteres.");
    }

    @Test
    void br021NomeCom50CaracteresPassa() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.criar(new CategoriaDTO.Request("x".repeat(50), null)).nome()).hasSize(50);
    }

    @Test
    void br022NomeDuplicadoIgnorandoMaiusculas() {
        when(repository.existsByNomeIgnoreCase("frutas")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(new CategoriaDTO.Request(" frutas ", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe uma categoria com esse nome.");
    }

    @Test
    void br022NomeDuplicadoNaAtualizacaoComparaOutrosIds() {
        when(repository.findById(2L)).thenReturn(Optional.of(cat(2L, "Verduras")));
        when(repository.existsByNomeIgnoreCaseAndIdCategoriaNot("Frutas", 2L)).thenReturn(true);
        assertThatThrownBy(() -> service.atualizar(2L, new CategoriaDTO.Request("Frutas", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void br022AtualizarMantendoOProprioNomePassa() {
        when(repository.findById(2L)).thenReturn(Optional.of(cat(2L, "Verduras")));
        when(repository.existsByNomeIgnoreCaseAndIdCategoriaNot("Verduras", 2L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.atualizar(2L, new CategoriaDTO.Request("Verduras", "Folhas")).descricao())
                .isEqualTo("Folhas");
    }

    @Test
    void br023DescricaoAte200() {
        assertThatThrownBy(() -> service.criar(new CategoriaDTO.Request("Frutas", "d".repeat(201))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Descrição deve ter no máximo 200 caracteres.");
    }

    @Test
    void buscarInexistenteDa404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Categoria 99 não encontrada.");
    }

    @Test
    void listarOrdenadoPorNome() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(cat(2L, "Frutas"), cat(1L, "Verduras")));
        assertThat(service.listar()).extracting(CategoriaDTO.Response::nome).containsExactly("Frutas", "Verduras");
    }

    @Test
    void corpoNuloRecusado() {
        assertThatThrownBy(() -> service.criar(null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void excluirSemUsoApaga() {
        CategoriaAlimento c = cat(3L, "Outros");
        when(repository.findById(3L)).thenReturn(Optional.of(c));
        service.excluir(3L);
        verify(repository).delete(c);
    }
}
