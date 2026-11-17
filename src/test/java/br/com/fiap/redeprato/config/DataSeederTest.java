package br.com.fiap.redeprato.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.service.ImpactoService;
import br.com.fiap.redeprato.repository.AlertaRepository;
import br.com.fiap.redeprato.repository.BeneficiarioRepository;
import br.com.fiap.redeprato.repository.CategoriaAlimentoRepository;
import br.com.fiap.redeprato.repository.DestinacaoRepository;
import br.com.fiap.redeprato.repository.EnderecoRepository;
import br.com.fiap.redeprato.repository.LoteRepository;
import br.com.fiap.redeprato.repository.OfertanteRepository;
import br.com.fiap.redeprato.repository.PontoDistribuicaoRepository;
import br.com.fiap.redeprato.repository.ReservaRepository;
import br.com.fiap.redeprato.dto.ImpactoDTO;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;

@SpringBootTest
class DataSeederTest {

    @Autowired private DataSeeder seeder;
    @Autowired private CategoriaAlimentoRepository categoriaRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private OfertanteRepository ofertanteRepository;
    @Autowired private BeneficiarioRepository beneficiarioRepository;
    @Autowired private PontoDistribuicaoRepository pontoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private DestinacaoRepository destinacaoRepository;
    @Autowired private AlertaRepository alertaRepository;
    @Autowired private ImpactoService impactoService;
    @Autowired private Clock clock;

    private Ofertante ofertante(String nome) {
        return ofertanteRepository.filtrar(null, null, null).stream().filter(o -> o.getNome().equals(nome)).findFirst()
                .orElseThrow();
    }

    @Test
    void seedCriaOMinimoPorTabela() {
        assertThat(categoriaRepository.count()).isGreaterThanOrEqualTo(6);
        assertThat(enderecoRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(ofertanteRepository.count()).isGreaterThanOrEqualTo(5);
        assertThat(beneficiarioRepository.count()).isGreaterThanOrEqualTo(6);
        assertThat(pontoRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(loteRepository.count()).isGreaterThanOrEqualTo(19);
        assertThat(reservaRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(destinacaoRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(alertaRepository.count()).isGreaterThanOrEqualTo(21);
    }

    @Test
    @Transactional
    void seedCobreTodosOsStatus() {
        for (StatusLote s : StatusLote.values()) {
            assertThat(loteRepository.filtrar(s, null, null, null)).as("lotes " + s).isNotEmpty();
        }
        for (StatusReserva s : StatusReserva.values()) {
            assertThat(reservaRepository.filtrar(null, null, s, null)).as("reservas " + s).isNotEmpty();
        }
        assertThat(alertaRepository.filtrar(TipoAlerta.XEPA, null)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ofertante("Sacolão Vila Mariana").isAtivo()).isFalse();
        assertThat(ofertante("Banca do Seu Antônio").isPossuiSelo()).isTrue();
    }

    @Test
    @Transactional
    void lotesAbertosTemPrazoNoFuturo() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Lote> abertos = new java.util.ArrayList<>(loteRepository.filtrar(StatusLote.DISPONIVEL, null, null, null));
        abertos.addAll(loteRepository.filtrar(StatusLote.RESERVADO, null, null, null));
        assertThat(abertos).allSatisfy(l -> assertThat(l.getDataLimiteRetirada()).isAfter(agora));
    }

    @Test
    @Transactional
    void somaDosDestinosNaoPassaDoPeso() {
        for (Lote l : loteRepository.findAll()) {
            BigDecimal soma = destinacaoRepository.somaPorLote(l.getIdLote(), null);
            assertThat(soma).as("lote " + l.getIdLote()).isLessThanOrEqualTo(l.getPesoKg());
        }
    }

    @Test
    void painelDaBancaTemKgEmMaisDeUmaSemana() {
        ImpactoDTO.Ofertante r = impactoService.porOfertante(ofertante("Banca do Seu Antônio").getIdOfertante(), null);
        assertThat(r.porSemana().stream().filter(s -> s.kg().signum() > 0).count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void seedEIdempotente() throws Exception {
        long antes = loteRepository.count();
        seeder.run();
        assertThat(loteRepository.count()).isEqualTo(antes);
    }
}
