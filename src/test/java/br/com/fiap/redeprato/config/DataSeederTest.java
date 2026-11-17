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

import br.com.fiap.redeprato.bo.ImpactoBO;
import br.com.fiap.redeprato.dao.AlertaDAO;
import br.com.fiap.redeprato.dao.BeneficiarioDAO;
import br.com.fiap.redeprato.dao.CategoriaAlimentoDAO;
import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dao.EnderecoDAO;
import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dao.PontoDistribuicaoDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.ImpactoDTO;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;

@SpringBootTest
class DataSeederTest {

    @Autowired private DataSeeder seeder;
    @Autowired private CategoriaAlimentoDAO categoriaDAO;
    @Autowired private EnderecoDAO enderecoDAO;
    @Autowired private OfertanteDAO ofertanteDAO;
    @Autowired private BeneficiarioDAO beneficiarioDAO;
    @Autowired private PontoDistribuicaoDAO pontoDAO;
    @Autowired private LoteDAO loteDAO;
    @Autowired private ReservaDAO reservaDAO;
    @Autowired private DestinacaoDAO destinacaoDAO;
    @Autowired private AlertaDAO alertaDAO;
    @Autowired private ImpactoBO impactoBO;
    @Autowired private Clock clock;

    private Ofertante ofertante(String nome) {
        return ofertanteDAO.filtrar(null, null, null).stream().filter(o -> o.getNome().equals(nome)).findFirst()
                .orElseThrow();
    }

    @Test
    void seedCriaOMinimoPorTabela() {
        assertThat(categoriaDAO.count()).isGreaterThanOrEqualTo(6);
        assertThat(enderecoDAO.count()).isGreaterThanOrEqualTo(10);
        assertThat(ofertanteDAO.count()).isGreaterThanOrEqualTo(5);
        assertThat(beneficiarioDAO.count()).isGreaterThanOrEqualTo(6);
        assertThat(pontoDAO.count()).isGreaterThanOrEqualTo(3);
        assertThat(loteDAO.count()).isGreaterThanOrEqualTo(19);
        assertThat(reservaDAO.count()).isGreaterThanOrEqualTo(10);
        assertThat(destinacaoDAO.count()).isGreaterThanOrEqualTo(10);
        assertThat(alertaDAO.count()).isGreaterThanOrEqualTo(21);
    }

    @Test
    @Transactional
    void seedCobreTodosOsStatus() {
        for (StatusLote s : StatusLote.values()) {
            assertThat(loteDAO.filtrar(s, null, null, null)).as("lotes " + s).isNotEmpty();
        }
        for (StatusReserva s : StatusReserva.values()) {
            assertThat(reservaDAO.filtrar(null, null, s, null)).as("reservas " + s).isNotEmpty();
        }
        assertThat(alertaDAO.filtrar(TipoAlerta.XEPA, null)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ofertante("Sacolão Vila Mariana").isAtivo()).isFalse();
        assertThat(ofertante("Banca do Seu Antônio").isPossuiSelo()).isTrue();
    }

    @Test
    @Transactional
    void lotesAbertosTemPrazoNoFuturo() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Lote> abertos = new java.util.ArrayList<>(loteDAO.filtrar(StatusLote.DISPONIVEL, null, null, null));
        abertos.addAll(loteDAO.filtrar(StatusLote.RESERVADO, null, null, null));
        assertThat(abertos).allSatisfy(l -> assertThat(l.getDataLimiteRetirada()).isAfter(agora));
    }

    @Test
    @Transactional
    void somaDosDestinosNaoPassaDoPeso() {
        for (Lote l : loteDAO.findAll()) {
            BigDecimal soma = destinacaoDAO.somaPorLote(l.getIdLote(), null);
            assertThat(soma).as("lote " + l.getIdLote()).isLessThanOrEqualTo(l.getPesoKg());
        }
    }

    @Test
    void painelDaBancaTemKgEmMaisDeUmaSemana() {
        ImpactoDTO.Ofertante r = impactoBO.porOfertante(ofertante("Banca do Seu Antônio").getIdOfertante(), null);
        assertThat(r.porSemana().stream().filter(s -> s.kg().signum() > 0).count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void seedEIdempotente() throws Exception {
        long antes = loteDAO.count();
        seeder.run();
        assertThat(loteDAO.count()).isEqualTo(antes);
    }
}
