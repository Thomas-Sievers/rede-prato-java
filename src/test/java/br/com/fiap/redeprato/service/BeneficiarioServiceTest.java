package br.com.fiap.redeprato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.redeprato.repository.BeneficiarioRepository;
import br.com.fiap.redeprato.dto.BeneficiarioDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class BeneficiarioServiceTest {

    @Mock
    private BeneficiarioRepository repository;
    @Mock
    private EnderecoService enderecoService;

    private BeneficiarioService service;

    @BeforeEach
    void setUp() {
        service =new BeneficiarioService(repository, enderecoService);
        lenient().when(repository.save(any())).thenAnswer(inv -> {
            Beneficiario b = inv.getArgument(0);
            if (b.getIdBeneficiario() == null) {
                b.setIdBeneficiario(4L);
            }
            return b;
        });
    }

    private static BeneficiarioDTO.Request req(String nome, TipoBeneficiario tipo, Integer qtd, String tel, Long idEnd) {
        return new BeneficiarioDTO.Request(nome, tipo, qtd, tel, idEnd, null);
    }

    @Test
    void criaSoComNomeETipoSemTelefoneNemEndereco() {
        BeneficiarioDTO.Response r = service.criar(req("Maria", TipoBeneficiario.FAMILIA, null, null, null));
        assertThat(r.idBeneficiario()).isEqualTo(4L);
        assertThat(r.ativo()).isTrue();
        assertThat(r.telefone()).isNull();
        assertThat(r.idEndereco()).isNull();
        verifyNoInteractions(enderecoService);
    }

    @Test
    void criaComEndereco() {
        when(enderecoService.find(1L)).thenReturn(Endereco.builder().idEndereco(1L).bairro("Paraíso").build());
        BeneficiarioDTO.Response r = service.criar(req("Rosana", TipoBeneficiario.FAMILIA, 4, "(11) 90000-0011", 1L));
        assertThat(r.bairro()).isEqualTo("Paraíso");
        assertThat(r.telefone()).isEqualTo("11900000011");
        assertThat(r.qtdPessoas()).isEqualTo(4);
    }

    @Test
    void br051NomeObrigatorio() {
        assertThatThrownBy(() -> service.criar(req(null, TipoBeneficiario.FAMILIA, 4, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome é obrigatório.");
        verify(repository, never()).save(any());
    }

    @Test
    void br051NomeAte100() {
        assertThatThrownBy(() -> service.criar(req("n".repeat(101), TipoBeneficiario.FAMILIA, 4, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome deve ter no máximo 100 caracteres.");
    }

    @Test
    void br052TipoObrigatorio() {
        assertThatThrownBy(() -> service.criar(req("Maria", null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do beneficiário é obrigatório.");
    }

    @Test
    void br053PessoaSemQuantidadeViraUm() {
        assertThat(service.criar(req("Seu Jorge", TipoBeneficiario.PESSOA, null, null, null)).qtdPessoas()).isEqualTo(1);
    }

    @Test
    void br053PessoaComQuantidadeDiferenteDeUmRecusada() {
        assertThatThrownBy(() -> service.criar(req("Seu Jorge", TipoBeneficiario.PESSOA, 3, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Pessoa sozinha deve ter quantidade de pessoas igual a 1.");
    }

    @Test
    void br053QuantidadeZeroRecusada() {
        assertThatThrownBy(() -> service.criar(req("Família", TipoBeneficiario.FAMILIA, 0, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Quantidade de pessoas deve ser entre 1 e 9999.");
    }

    @Test
    void br053QuantidadeAcimaDoLimiteRecusada() {
        assertThatThrownBy(() -> service.criar(req("Cozinha", TipoBeneficiario.INSTITUICAO, 10000, null, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Quantidade de pessoas deve ser entre 1 e 9999.");
    }

    @Test
    void br053QuantidadeNoLimitePassa() {
        assertThat(service.criar(req("Cozinha", TipoBeneficiario.INSTITUICAO, 9999, null, null)).qtdPessoas()).isEqualTo(9999);
    }

    @Test
    void br054TelefoneInvalidoRecusado() {
        assertThatThrownBy(() -> service.criar(req("Maria", TipoBeneficiario.FAMILIA, 2, "900000001", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Telefone deve ter 10 ou 11 dígitos (DDD + número).");
    }

    @Test
    void br054TelefoneEmBrancoViraNulo() {
        assertThat(service.criar(req("Maria", TipoBeneficiario.FAMILIA, 2, "  ", null)).telefone()).isNull();
    }

    @Test
    void br055EnderecoInexistente() {
        when(enderecoService.find(9L)).thenThrow(NotFoundException.of("Endereço", 9L));
        assertThatThrownBy(() -> service.criar(req("Maria", TipoBeneficiario.FAMILIA, 2, null, 9L)))
                .isInstanceOf(NotFoundException.class).hasMessage("Endereço 9 não encontrado.");
    }

    @Test
    void br056EntidadeNaoTemCampoDeDocumento() {
        List<String> campos = Arrays.stream(Beneficiario.class.getDeclaredFields())
                .map(Field::getName).map(n -> n.toLowerCase(Locale.ROOT)).toList();
        assertThat(campos).noneMatch(n -> n.contains("cpf") || n.contains("rg") || n.contains("documento"));
    }

    @Test
    void br057ExclusaoLogicaIdempotente() {
        Beneficiario b = Beneficiario.builder().idBeneficiario(4L).nome("Maria").tipo(TipoBeneficiario.FAMILIA).ativo(true).build();
        when(repository.findById(4L)).thenReturn(Optional.of(b));
        service.excluir(4L);
        assertThat(b.isAtivo()).isFalse();
        service.excluir(4L);
        verify(repository, never()).delete(any());
        verify(repository).save(b);
    }

    @Test
    void reativaPorPut() {
        Beneficiario b = Beneficiario.builder().idBeneficiario(4L).nome("Maria").tipo(TipoBeneficiario.FAMILIA).ativo(false).build();
        when(repository.findById(4L)).thenReturn(Optional.of(b));
        BeneficiarioDTO.Response r = service.atualizar(4L,
                new BeneficiarioDTO.Request("Maria", TipoBeneficiario.FAMILIA, 3, null, null, true));
        assertThat(r.ativo()).isTrue();
        assertThat(r.qtdPessoas()).isEqualTo(3);
    }

    @Test
    void listarPadraoAtivos() {
        when(repository.filtrar(null, true)).thenReturn(List.of());
        assertThat(service.listar(null, null)).isEmpty();
    }

    @Test
    void buscarInexistente() {
        when(repository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(8L))
                .isInstanceOf(NotFoundException.class).hasMessage("Beneficiário 8 não encontrado.");
    }
}
