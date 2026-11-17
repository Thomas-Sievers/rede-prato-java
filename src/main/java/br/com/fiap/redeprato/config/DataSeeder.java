package br.com.fiap.redeprato.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.AlertaDAO;
import br.com.fiap.redeprato.dao.BeneficiarioDAO;
import br.com.fiap.redeprato.dao.CategoriaAlimentoDAO;
import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dao.EnderecoDAO;
import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dao.OfertanteDAO;
import br.com.fiap.redeprato.dao.PontoDistribuicaoDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.entity.Alerta;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.CanalAlerta;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;
import br.com.fiap.redeprato.entity.enums.TipoPonto;

/**
 * Simulated demo data (fictional names, phones and generic addresses) for the H2 profile.
 * Dates are relative to the Clock so the demo always looks like "today". Never runs on Oracle.
 */
@Component
@Profile("!oracle")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoriaAlimentoDAO categoriaDAO;
    private final EnderecoDAO enderecoDAO;
    private final OfertanteDAO ofertanteDAO;
    private final BeneficiarioDAO beneficiarioDAO;
    private final PontoDistribuicaoDAO pontoDAO;
    private final LoteDAO loteDAO;
    private final ReservaDAO reservaDAO;
    private final DestinacaoDAO destinacaoDAO;
    private final AlertaDAO alertaDAO;
    private final Clock clock;

    private LocalDateTime now;

    public DataSeeder(CategoriaAlimentoDAO categoriaDAO, EnderecoDAO enderecoDAO, OfertanteDAO ofertanteDAO,
                      BeneficiarioDAO beneficiarioDAO, PontoDistribuicaoDAO pontoDAO, LoteDAO loteDAO,
                      ReservaDAO reservaDAO, DestinacaoDAO destinacaoDAO, AlertaDAO alertaDAO, Clock clock) {
        this.categoriaDAO = categoriaDAO;
        this.enderecoDAO = enderecoDAO;
        this.ofertanteDAO = ofertanteDAO;
        this.beneficiarioDAO = beneficiarioDAO;
        this.pontoDAO = pontoDAO;
        this.loteDAO = loteDAO;
        this.reservaDAO = reservaDAO;
        this.destinacaoDAO = destinacaoDAO;
        this.alertaDAO = alertaDAO;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaDAO.count() > 0) {
            log.info("Seed skipped: data already present");
            return;
        }
        now = LocalDateTime.now(clock).withSecond(0).withNano(0);
        LocalDate hoje = now.toLocalDate();

        // Categorias
        CategoriaAlimento frutas = cat("Frutas", "Frutas frescas ou maduras");
        CategoriaAlimento verduras = cat("Verduras", "Folhas e verduras");
        CategoriaAlimento legumes = cat("Legumes", "Legumes e tubérculos");
        CategoriaAlimento padaria = cat("Padaria", "Pães e massas do dia");
        CategoriaAlimento laticinios = cat("Laticínios", "Leite, iogurte e queijos perto do vencimento");
        cat("Outros", "Outros alimentos");

        // Endereços (genéricos, Paraíso / Vila Mariana)
        Endereco eFeira12 = end("Rua Manuel da Nóbrega", "s/n", "Feira - Banca 12", "Paraíso", "04001000");
        Endereco eFeira7 = end("Rua Manuel da Nóbrega", "s/n", "Feira - Banca 7", "Paraíso", "04001000");
        Endereco eHorti = end("Rua Tutóia", "210", null, "Paraíso", "04007000");
        Endereco eMercado = end("Rua Abílio Soares", "455", null, "Paraíso", "04005001");
        Endereco eSacolao = end("Rua Domingos de Morais", "1200", null, "Vila Mariana", "04010100");
        Endereco ePonto = end("Rua Desembargador Eliseu Guilherme", "88", "Salão da associação", "Paraíso", "04004030");
        Endereco eCozinha = end("Rua Coronel Oscar Porto", "300", null, "Paraíso", "04003000");
        Endereco eOng = end("Rua Cubatão", "640", null, "Vila Mariana", "04013001");
        Endereco eRosana = end("Rua Tomás Carvalhal", "150", "Apto 12", "Paraíso", "04006001");
        Endereco eSouza = end("Rua Afonso de Freitas", "75", null, "Paraíso", "04006050");

        // Ofertantes
        Ofertante antonio = of("Banca do Seu Antônio", TipoOfertante.FEIRANTE, "11900000001", null, eFeira12,
                hoje.minusDays(10), true);
        Ofertante lucia = of("Banca da Dona Lúcia", TipoOfertante.FEIRANTE, "11900000002", null, eFeira7, null, true);
        Ofertante horti = of("Hortifrúti do Paraíso", TipoOfertante.SUPERMERCADO, "1130000003",
                "contato@hortifruti.example", eHorti, hoje.minusDays(30), true);
        Ofertante mercado = of("Mercado Bom Preço", TipoOfertante.SUPERMERCADO, "1130000004",
                "doacoes@bompreco.example", eMercado, null, true);
        of("Sacolão Vila Mariana", TipoOfertante.SUPERMERCADO, "1130000005", null, eSacolao, null, false);

        // Beneficiários (sem documento; telefone e endereço opcionais)
        Beneficiario rosana = ben("Rosana", TipoBeneficiario.FAMILIA, 4, "11900000011", eRosana);
        Beneficiario jorge = ben("Seu Jorge", TipoBeneficiario.PESSOA, 1, "11900000012", null);
        Beneficiario souza = ben("Família Souza", TipoBeneficiario.FAMILIA, 5, "11900000013", eSouza);
        Beneficiario cozinhaBen = ben("Cozinha Solidária do Bairro", TipoBeneficiario.INSTITUICAO, 60,
                "11900000014", eCozinha);
        Beneficiario lanchonete = ben("Lanchonete da Esquina", TipoBeneficiario.PEQUENO_NEGOCIO, 3, "11900000015", null);
        Beneficiario maria = ben("Maria", TipoBeneficiario.PESSOA, 1, null, null);

        // Pontos de distribuição (com a placa)
        PontoDistribuicao pontoParaiso = ponto("Ponto Paraíso", TipoPonto.ASSOCIACAO, "TER e QUI", 16, 18,
                "11900000021", ePonto);
        ponto("Cozinha Solidária do Bairro", TipoPonto.COZINHA_SOLIDARIA, "SEG a SEX", 11, 14, "11900000014", eCozinha);
        PontoDistribuicao ong = ponto("ONG Prato Cheio", TipoPonto.ONG, "SAB", 9, 12, null, eOng);

        // Prazos relativos a "agora"
        LocalDateTime hoje14 = futuroOu(hoje.atTime(14, 0), 2);
        LocalDateTime hoje18 = futuroOu(hoje.atTime(18, 0), 3);
        LocalDateTime amanha10 = hoje.plusDays(1).atTime(10, 0);
        LocalDateTime amanha12 = hoje.plusDays(1).atTime(12, 0);

        // 8 DISPONIVEL
        Lote d1 = lote(antonio, legumes, "Tomates maduros", "8.5", CondicaoLote.MADURA, ModalidadeLote.DOACAO,
                null, null, hoje14, StatusLote.DISPONIVEL, 3);
        lote(antonio, frutas, "Bananas", "6", CondicaoLote.MADURA, ModalidadeLote.DOACAO, null, null,
                hoje18, StatusLote.DISPONIVEL, 3);
        Lote d3 = lote(antonio, verduras, "Alface crespa", "3", CondicaoLote.CONSUMO_IMEDIATO, ModalidadeLote.DOACAO,
                null, null, hoje14, StatusLote.DISPONIVEL, 2);
        Lote d4 = lote(lucia, frutas, "Laranjas", "10", CondicaoLote.BOA, ModalidadeLote.DOACAO, null, null,
                hoje18, StatusLote.DISPONIVEL, 5);
        lote(horti, frutas, "Maçãs", "5", CondicaoLote.BOA, ModalidadeLote.VENDA, "9.90", "30.00",
                amanha10, StatusLote.DISPONIVEL, 4);
        lote(horti, legumes, "Cenouras", "4", CondicaoLote.BOA, ModalidadeLote.DOACAO, null, "16.00",
                amanha12, StatusLote.DISPONIVEL, 4);
        lote(mercado, padaria, "Pão francês do dia", "2.5", CondicaoLote.CONSUMO_IMEDIATO, ModalidadeLote.DOACAO,
                null, "35.00", hoje18, StatusLote.DISPONIVEL, 2);
        lote(mercado, laticinios, "Iogurte perto do vencimento", "3", CondicaoLote.BOA, ModalidadeLote.VENDA,
                "12.00", "36.00", amanha10, StatusLote.DISPONIVEL, 2);

        // 3 RESERVADO with ATIVA reservas
        Lote r1 = lote(antonio, legumes, "Abobrinhas", "4", CondicaoLote.MADURA, ModalidadeLote.DOACAO, null, null,
                hoje18, StatusLote.RESERVADO, 4);
        reserva(r1, rosana, pontoParaiso, now.minusHours(1), null, StatusReserva.ATIVA);
        Lote r2 = lote(horti, frutas, "Mamões", "6", CondicaoLote.MADURA, ModalidadeLote.DOACAO, null, "24.00",
                amanha12, StatusLote.RESERVADO, 5);
        reserva(r2, cozinhaBen, null, now.minusHours(2), null, StatusReserva.ATIVA);
        Lote r3 = lote(mercado, padaria, "Pão de forma", "2", CondicaoLote.BOA, ModalidadeLote.VENDA, "5.00", "14.00",
                amanha10, StatusLote.RESERVADO, 3);
        reserva(r3, lanchonete, null, now.minusMinutes(40), null, StatusReserva.ATIVA);

        // 6 RETIRADO over the last 4 weeks, with destinações
        retirado(antonio, legumes, "Tomates", "12", 2, rosana, pontoParaiso, "10", "2");
        retirado(antonio, frutas, "Bananas", "9", 8, souza, null, "9", null);
        retirado(antonio, verduras, "Folhas variadas", "5", 15, cozinhaBen, null, "4", "1");
        retirado(horti, frutas, "Maçãs", "7", 20, cozinhaBen, ong, "7", null);
        retirado(lucia, frutas, "Laranjas", "15", 4, cozinhaBen, null, "13", "2");
        retirado(mercado, padaria, "Pão francês", "3", 1, jorge, null, "3", null);

        // 2 EXPIRADO (one already composted)
        LocalDate d3dias = hoje.minusDays(3);
        Lote e1 = lote(lucia, verduras, "Couve", "2", CondicaoLote.CONSUMO_IMEDIATO, ModalidadeLote.DOACAO, null, null,
                d3dias.atTime(13, 0), StatusLote.EXPIRADO, d3dias.atTime(7, 0));
        destino(e1, TipoDestinacao.COMPOSTAGEM, "2", hoje.minusDays(2).atTime(9, 0));
        LocalDate ontem = hoje.minusDays(1);
        lote(mercado, laticinios, "Queijo minas", "1.5", CondicaoLote.BOA, ModalidadeLote.DOACAO, null, "25.00",
                ontem.atTime(12, 0), StatusLote.EXPIRADO, ontem.atTime(8, 0));

        // CANCELADA history on an available lote
        reserva(d4, maria, null, now.minusHours(3), null, StatusReserva.CANCELADA);

        // 2 XEPA alerts from Banca do Seu Antônio today
        alerta(d1, TipoAlerta.XEPA, CanalAlerta.APP, now.minusMinutes(30));
        alerta(d3, TipoAlerta.XEPA, CanalAlerta.WHATSAPP, now.minusMinutes(30));

        log.info("Seed done: {} lotes, {} reservas, {} destinações, {} alertas",
                loteDAO.count(), reservaDAO.count(), destinacaoDAO.count(), alertaDAO.count());
    }

    /** Today's slot, or now + {@code horas} if that hour already passed. */
    private LocalDateTime futuroOu(LocalDateTime alvo, int horas) {
        return alvo.isAfter(now.plusMinutes(30)) ? alvo : now.plusHours(horas);
    }

    private CategoriaAlimento cat(String nome, String descricao) {
        return categoriaDAO.save(CategoriaAlimento.builder().nome(nome).descricao(descricao).build());
    }

    private Endereco end(String logradouro, String numero, String complemento, String bairro, String cep) {
        return enderecoDAO.save(Endereco.builder().logradouro(logradouro).numero(numero).complemento(complemento)
                .bairro(bairro).cidade("São Paulo").uf("SP").cep(cep).build());
    }

    private Ofertante of(String nome, TipoOfertante tipo, String tel, String email, Endereco e,
                         LocalDate dataSelo, boolean ativo) {
        return ofertanteDAO.save(Ofertante.builder().nome(nome).tipo(tipo).telefone(tel).email(email)
                .possuiSelo(dataSelo != null).dataSelo(dataSelo).ativo(ativo).endereco(e).build());
    }

    private Beneficiario ben(String nome, TipoBeneficiario tipo, Integer qtd, String tel, Endereco e) {
        return beneficiarioDAO.save(Beneficiario.builder().nome(nome).tipo(tipo).qtdPessoas(qtd).telefone(tel)
                .ativo(true).endereco(e).build());
    }

    private PontoDistribuicao ponto(String nome, TipoPonto tipo, String dias, int abre, int fecha, String tel,
                                    Endereco e) {
        return pontoDAO.save(PontoDistribuicao.builder().nome(nome).tipo(tipo).diasFuncionamento(dias)
                .horarioAbertura(LocalTime.of(abre, 0)).horarioFechamento(LocalTime.of(fecha, 0)).telefone(tel)
                .ativo(true).endereco(e).build());
    }

    /** Lote registered {@code horasAtras} hours ago. */
    private Lote lote(Ofertante o, CategoriaAlimento c, String desc, String kg, CondicaoLote cond,
                      ModalidadeLote mod, String preco, String valor, LocalDateTime prazo, StatusLote status,
                      int horasAtras) {
        return lote(o, c, desc, kg, cond, mod, preco, valor, prazo, status, now.minusHours(horasAtras));
    }

    private Lote lote(Ofertante o, CategoriaAlimento c, String desc, String kg, CondicaoLote cond,
                      ModalidadeLote mod, String preco, String valor, LocalDateTime prazo, StatusLote status,
                      LocalDateTime cadastro) {
        Lote l = loteDAO.save(Lote.builder().ofertante(o).categoria(c).descricao(desc).pesoKg(new BigDecimal(kg))
                .condicao(cond).modalidade(mod).preco(preco == null ? null : new BigDecimal(preco))
                .valorEstimado(valor == null ? null : new BigDecimal(valor)).dataCadastro(cadastro)
                .dataLimiteRetirada(prazo).precisaTransporte(false).status(status).build());
        alerta(l, TipoAlerta.NOVO_LOTE, CanalAlerta.APP, cadastro);
        return l;
    }

    private void retirado(Ofertante o, CategoriaAlimento c, String desc, String kg, int diasAtras, Beneficiario b,
                          PontoDistribuicao p, String consumo, String compostagem) {
        LocalDate dia = now.toLocalDate().minusDays(diasAtras);
        Lote l = lote(o, c, desc, kg, CondicaoLote.MADURA, ModalidadeLote.DOACAO, null, null,
                dia.atTime(18, 0), StatusLote.RETIRADO, dia.atTime(8, 0));
        reserva(l, b, p, dia.atTime(10, 0), dia.atTime(16, 0), StatusReserva.RETIRADA);
        if (consumo != null) {
            destino(l, TipoDestinacao.CONSUMO, consumo, dia.atTime(19, 0));
        }
        if (compostagem != null) {
            destino(l, TipoDestinacao.COMPOSTAGEM, compostagem, dia.atTime(19, 30));
        }
    }

    private void reserva(Lote l, Beneficiario b, PontoDistribuicao p, LocalDateTime data, LocalDateTime retirada,
                         StatusReserva status) {
        reservaDAO.save(Reserva.builder().lote(l).beneficiario(b).ponto(p).dataReserva(data)
                .dataRetirada(retirada).status(status).build());
    }

    private void destino(Lote l, TipoDestinacao tipo, String kg, LocalDateTime data) {
        destinacaoDAO.save(Destinacao.builder().lote(l).tipo(tipo).pesoKg(new BigDecimal(kg)).dataDestinacao(data).build());
    }

    private void alerta(Lote l, TipoAlerta tipo, CanalAlerta canal, LocalDateTime data) {
        alertaDAO.save(Alerta.builder().lote(l).tipo(tipo).canal(canal).dataEnvio(data).build());
    }
}
