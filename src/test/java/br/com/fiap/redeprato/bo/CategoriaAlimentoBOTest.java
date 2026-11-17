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

import br.com.fiap.redeprato.dao.CategoriaAlimentoDAO;
import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dto.CategoriaDTO;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoriaAlimentoBOTest {

    @Mock
    private CategoriaAlimentoDAO dao;

    @Mock
    private LoteDAO loteDAO;

    @Test
    void br024CategoriaUsadaPorLoteNaoPodeSerExcluida() {
        when(dao.findById(3L)).thenReturn(Optional.of(cat(3L, "Frutas")));
        when(loteDAO.existsByCategoria_IdCategoria(3L)).thenReturn(true);
        assertThatThrownBy(() -> bo.excluir(3L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível excluir: existem lotes nesta categoria.");
        verify(dao, never()).delete(any());
    }

    @InjectMocks
    private CategoriaAlimentoBO bo;

    private static CategoriaAlimento cat(Long id, String nome) {
        return CategoriaAlimento.builder().idCategoria(id).nome(nome).build();
    }

    @Test
    void criaCategoriaComNomeAparado() {
        when(dao.existsByNomeIgnoreCase("Frutas")).thenReturn(false);
        when(dao.save(any())).thenAnswer(inv -> {
            CategoriaAlimento c = inv.getArgument(0);
            c.setIdCategoria(1L);
            return c;
        });

        CategoriaDTO.Response r = bo.criar(new CategoriaDTO.Request("  Frutas ", "  "));

        assertThat(r.idCategoria()).isEqualTo(1L);
        assertThat(r.nome()).isEqualTo("Frutas");
        assertThat(r.descricao()).isNull();
    }

    @Test
    void br021NomeObrigatorio() {
        assertThatThrownBy(() -> bo.criar(new CategoriaDTO.Request("   ", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nome da categoria é obrigatório.");
        verify(dao, never()).save(any());
    }

    @Test
    void br021NomeAte50Caracteres() {
        assertThatThrownBy(() -> bo.criar(new CategoriaDTO.Request("x".repeat(51), null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nome da categoria deve ter no máximo 50 caracteres.");
    }

    @Test
    void br021NomeCom50CaracteresPassa() {
        when(dao.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(bo.criar(new CategoriaDTO.Request("x".repeat(50), null)).nome()).hasSize(50);
    }

    @Test
    void br022NomeDuplicadoIgnorandoMaiusculas() {
        when(dao.existsByNomeIgnoreCase("frutas")).thenReturn(true);
        assertThatThrownBy(() -> bo.criar(new CategoriaDTO.Request(" frutas ", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe uma categoria com esse nome.");
    }

    @Test
    void br022NomeDuplicadoNaAtualizacaoComparaOutrosIds() {
        when(dao.findById(2L)).thenReturn(Optional.of(cat(2L, "Verduras")));
        when(dao.existsByNomeIgnoreCaseAndIdCategoriaNot("Frutas", 2L)).thenReturn(true);
        assertThatThrownBy(() -> bo.atualizar(2L, new CategoriaDTO.Request("Frutas", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void br022AtualizarMantendoOProprioNomePassa() {
        when(dao.findById(2L)).thenReturn(Optional.of(cat(2L, "Verduras")));
        when(dao.existsByNomeIgnoreCaseAndIdCategoriaNot("Verduras", 2L)).thenReturn(false);
        when(dao.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(bo.atualizar(2L, new CategoriaDTO.Request("Verduras", "Folhas")).descricao())
                .isEqualTo("Folhas");
    }

    @Test
    void br023DescricaoAte200() {
        assertThatThrownBy(() -> bo.criar(new CategoriaDTO.Request("Frutas", "d".repeat(201))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Descrição deve ter no máximo 200 caracteres.");
    }

    @Test
    void buscarInexistenteDa404() {
        when(dao.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Categoria 99 não encontrada.");
    }

    @Test
    void listarOrdenadoPorNome() {
        when(dao.findAllByOrderByNomeAsc()).thenReturn(List.of(cat(2L, "Frutas"), cat(1L, "Verduras")));
        assertThat(bo.listar()).extracting(CategoriaDTO.Response::nome).containsExactly("Frutas", "Verduras");
    }

    @Test
    void corpoNuloRecusado() {
        assertThatThrownBy(() -> bo.criar(null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void excluirSemUsoApaga() {
        CategoriaAlimento c = cat(3L, "Outros");
        when(dao.findById(3L)).thenReturn(Optional.of(c));
        bo.excluir(3L);
        verify(dao).delete(c);
    }
}
