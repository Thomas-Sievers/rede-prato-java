package br.com.fiap.redeprato.service;

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

import br.com.fiap.redeprato.repository.OfertanteRepository;
import br.com.fiap.redeprato.dto.OfertanteDTO;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class OfertanteServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private OfertanteRepository repository;
    @Mock
    private EnderecoService enderecoService;

    private OfertanteService service;
    private final Endereco endereco = Endereco.builder().idEndereco(1L).bairro("Paraíso").build();

    @BeforeEach
    void setUp() {
        service =new OfertanteService(repository, enderecoService, CLOCK);
        lenient().when(enderecoService.find(1L)).thenReturn(endereco);
        lenient().when(repository.save(any())).thenAnswer(inv -> {
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
        OfertanteDTO.Response r = service.criar(req(" Banca do Seu Antônio ", TipoOfertante.FEIRANTE,
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
        assertThatThrownBy(() -> service.criar(req("", TipoOfertante.FEIRANTE, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome é obrigatório.");
        verify(repository, never()).save(any());
    }

    @Test
    void br031NomeAte100() {
        assertThatThrownBy(() -> service.criar(req("n".repeat(101), TipoOfertante.FEIRANTE, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Nome deve ter no máximo 100 caracteres.");
    }

    @Test
    void br032TipoObrigatorio() {
        assertThatThrownBy(() -> service.criar(req("Banca", null, "11900000001", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Tipo do ofertante é obrigatório.");
    }

    @Test
    void br033TelefoneObrigatorio() {
        assertThatThrownBy(() -> service.criar(req("Banca", TipoOfertante.FEIRANTE, " ", null)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Telefone é obrigatório.");
    }

    @Test
    void br033TelefoneCurto() {
        assertThatThrownBy(() -> service.criar(req("Banca", TipoOfertante.FEIRANTE, "90000-0001", "")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Telefone deve ter 10 ou 11 dígitos (DDD + número).");
    }

    @Test
    void br033TelefoneLongo() {
        assertThatThrownBy(() -> service.criar(req("Banca", TipoOfertante.FEIRANTE, "5511900000001", null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br033TelefoneFixoCom10DigitosPassa() {
        assertThat(service.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", null)).telefone())
                .isEqualTo("1130000001");
    }

    @Test
    void br034EmailInvalido() {
        assertThatThrownBy(() -> service.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", "mercado@")))
                .isInstanceOf(BusinessRuleException.class).hasMessage("E-mail inválido.");
    }

    @Test
    void br034EmailValidoPassa() {
        assertThat(service.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", " contato@mercado.com ")).email())
                .isEqualTo("contato@mercado.com");
    }

    @Test
    void br034EmailAte100() {
        String email = "a".repeat(95) + "@x.com";
        assertThatThrownBy(() -> service.criar(req("Mercado", TipoOfertante.SUPERMERCADO, "1130000001", email)))
                .isInstanceOf(BusinessRuleException.class).hasMessage("E-mail deve ter no máximo 100 caracteres.");
    }

    @Test
    void br035EnderecoObrigatorio() {
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca", TipoOfertante.FEIRANTE, "11900000001", null, null, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Endereço é obrigatório.");
    }

    @Test
    void br035EnderecoInexistente() {
        when(enderecoService.find(9L)).thenThrow(NotFoundException.of("Endereço", 9L));
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca", TipoOfertante.FEIRANTE, "11900000001", null, 9L, null);
        assertThatThrownBy(() -> service.criar(r))
                .isInstanceOf(NotFoundException.class).hasMessage("Endereço 9 não encontrado.");
    }

    @Test
    void br036SeloInconsistenteRecusado() {
        Ofertante o = existente(true, false);
        o.setDataSelo(LocalDate.of(2026, 1, 1));
        assertThatThrownBy(() -> service.validate(o))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selo inconsistente: selo e data do selo devem andar juntos.");
    }

    @Test
    void br036SeloSemDataRecusado() {
        Ofertante o = existente(true, false);
        o.setPossuiSelo(true);
        assertThatThrownBy(() -> service.validate(o)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void br037ExclusaoLogica() {
        Ofertante o = existente(true, false);
        when(repository.findById(3L)).thenReturn(Optional.of(o));
        service.excluir(3L);
        assertThat(o.isAtivo()).isFalse();
        verify(repository).save(o);
        verify(repository, never()).delete(any());
    }

    @Test
    void br037ExclusaoLogicaIdempotente() {
        Ofertante o = existente(false, false);
        when(repository.findById(3L)).thenReturn(Optional.of(o));
        service.excluir(3L);
        assertThat(o.isAtivo()).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void br038ConcedeSeloComDataDeHoje() {
        when(repository.findById(3L)).thenReturn(Optional.of(existente(true, false)));
        OfertanteDTO.Response r = service.concederSelo(3L);
        assertThat(r.possuiSelo()).isTrue();
        assertThat(r.dataSelo()).isEqualTo(LocalDate.of(2026, 11, 17));
    }

    @Test
    void br038InativoNaoRecebeSelo() {
        when(repository.findById(3L)).thenReturn(Optional.of(existente(false, false)));
        assertThatThrownBy(() -> service.concederSelo(3L))
                .isInstanceOf(BusinessRuleException.class).hasMessage("Ofertante inativo não pode receber o selo.");
    }

    @Test
    void br038SeloDuplicadoDa409() {
        when(repository.findById(3L)).thenReturn(Optional.of(existente(true, true)));
        assertThatThrownBy(() -> service.concederSelo(3L))
                .isInstanceOf(ConflictException.class).hasMessage("Este ofertante já possui o selo.");
    }

    @Test
    void revogaSeloLimpaData() {
        Ofertante o = existente(true, true);
        when(repository.findById(3L)).thenReturn(Optional.of(o));
        service.revogarSelo(3L);
        assertThat(o.isPossuiSelo()).isFalse();
        assertThat(o.getDataSelo()).isNull();
    }

    @Test
    void br039PutEmInativoPermitidoEReativa() {
        Ofertante o = existente(false, false);
        when(repository.findById(3L)).thenReturn(Optional.of(o));
        OfertanteDTO.Request r = new OfertanteDTO.Request("Banca Nova", TipoOfertante.FEIRANTE, "11900000002", null, 1L, true);
        OfertanteDTO.Response resp = service.atualizar(3L, r);
        assertThat(resp.ativo()).isTrue();
        assertThat(resp.nome()).isEqualTo("Banca Nova");
    }

    @Test
    void br039PutSemAtivoMantemValorEPreservaSelo() {
        Ofertante o = existente(true, true);
        when(repository.findById(3L)).thenReturn(Optional.of(o));
        OfertanteDTO.Response resp = service.atualizar(3L, req("Banca", TipoOfertante.FEIRANTE, "11900000001", null));
        assertThat(resp.ativo()).isTrue();
        assertThat(resp.possuiSelo()).isTrue();
        assertThat(resp.dataSelo()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void listarPorPadraoSoAtivos() {
        when(repository.filtrar(null, true, null)).thenReturn(List.of(existente(true, false)));
        assertThat(service.listar(null, null, " ")).hasSize(1);
    }

    @Test
    void listarInativos() {
        when(repository.filtrar(TipoOfertante.FEIRANTE, false, "Paraíso")).thenReturn(List.of());
        assertThat(service.listar(TipoOfertante.FEIRANTE, false, "Paraíso")).isEmpty();
    }

    @Test
    void buscarInexistente() {
        when(repository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(77L))
                .isInstanceOf(NotFoundException.class).hasMessage("Ofertante 77 não encontrado.");
    }
}
