// Rede de Prato — Oracle smoke test (feature 21).
//
// Calls every endpoint of the running API against the real Oracle database and checks the main flows
// and error cases. JDK only (no dependencies, not part of the Maven build).
//
//   java tools/smoke/OracleSmokeTest.java [baseUrl]      (default http://localhost:8080)
//
// Prints one line per check, a summary, and exits 1 if anything failed. Every record it creates is
// named "[TESTE] ...". It never reads ORACLE_* variables and never starts or stops the app.

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleSmokeTest {

    static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    static final String RUN = String.valueOf(System.currentTimeMillis() % 1_000_000_000L);
    static final String MES = "2026-11";

    // ids seeded by db/dml.sql (identity counters must start after them)
    static final long MAX_ENDERECO = 6, MAX_OFERTANTE = 5, MAX_CATEGORIA = 5, MAX_BENEFICIARIO = 6,
            MAX_PONTO = 5, MAX_LOTE = 6, MAX_RESERVA = 5, MAX_DESTINACAO = 5, MAX_ALERTA = 5;

    static String base;
    static HttpClient http;
    static int passed;
    static int failed;
    static final List<String> failures = new ArrayList<>();
    static final Set<String> covered = new LinkedHashSet<>();

    record Resp(int status, String body) {
    }

    public static void main(String[] args) throws Exception {
        base = args.length > 0 ? args[0].replaceAll("/+$", "") : "http://localhost:8080";
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        System.out.println("Rede de Prato — Oracle smoke test against " + base + " (run " + RUN + ")");
        System.out.println();

        // ---------- 0. health: only continue on the oracle profile ----------
        Resp h = expect("health", "GET", "/api/health", null, 200);
        String profile = str(h.body, "profile");
        if (!"oracle".equals(profile)) {
            check(false, "GET", "/api/health", "profile=oracle", String.valueOf(profile),
                    "app is not on the oracle profile; stopping (no tests against H2)");
            finish();
            return;
        }
        check(true, "GET", "/api/health", "profile=oracle", profile, "running on Oracle");

        section("Seeded data (db/dml.sql)");
        seeded();

        section("Endereços and categorias (identity + CRUD)");
        long end1 = createdId(expect("create endereço", "POST", "/api/enderecos",
                endereco("[TESTE] Rua Smoke " + RUN), 201), "idEndereco", MAX_ENDERECO, "POST", "/api/enderecos");
        expect("get endereço", "GET", "/api/enderecos/" + end1, null, 200);
        expect("list endereços by bairro", "GET", "/api/enderecos?bairro=Paraiso", null, 200);
        expect("update endereço", "PUT", "/api/enderecos/" + end1, endereco("[TESTE] Rua Smoke alterada " + RUN), 200);
        long cat1 = createdId(expect("create categoria", "POST", "/api/categorias",
                categoria("[TESTE] Cat " + RUN), 201), "idCategoria", MAX_CATEGORIA, "POST", "/api/categorias");
        expect("get categoria", "GET", "/api/categorias/" + cat1, null, 200);
        expect("update categoria", "PUT", "/api/categorias/" + cat1, categoria("[TESTE] Cat alt " + RUN), 200);
        expect("duplicate categoria name", "POST", "/api/categorias", categoria("[TESTE] Cat alt " + RUN), 409);

        section("Lotes");
        String prazo = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0).format(ISO);
        String passado = LocalDateTime.now().minusDays(1).withNano(0).format(ISO);
        Resp la = expect("create lote A (DOACAO)", "POST", "/api/lotes",
                lote(1, 1, "[TESTE] Lote A " + RUN, "10", "DOACAO", null, prazo), 201);
        long loteA = createdId(la, "idLote", MAX_LOTE, "POST", "/api/lotes");
        check("DISPONIVEL".equals(str(la.body, "status")), "POST", "/api/lotes", "status=DISPONIVEL",
                str(la.body, "status"), "new lote is available");
        Resp al = expect("NOVO_LOTE alert of lote A", "GET", "/api/alertas?tipo=NOVO_LOTE&idLote=" + loteA, null, 200);
        List<String> novos = objects(al.body);
        check(novos.size() == 1 && num(novos.get(0), "idAlerta") > MAX_ALERTA, "GET", "/api/alertas",
                "1 NOVO_LOTE alert with id>" + MAX_ALERTA, novos.size() + " alert(s)" +
                (novos.isEmpty() ? "" : ", id=" + num(novos.get(0), "idAlerta")), "alert created with the lote");
        Resp disp = expect("lotes disponíveis", "GET", "/api/lotes/disponiveis", null, 200);
        check(hasId(disp.body, "idLote", loteA), "GET", "/api/lotes/disponiveis", "contains lote " + loteA,
                hasId(disp.body, "idLote", loteA) ? "present" : "missing", "new lote is listed");
        expect("get lote", "GET", "/api/lotes/" + loteA, null, 200);
        expect("list lotes of ofertante 1", "GET", "/api/lotes?idOfertante=1&status=DISPONIVEL", null, 200);
        expect("update lote A (still DISPONIVEL)", "PUT", "/api/lotes/" + loteA,
                lote(1, 1, "[TESTE] Lote A " + RUN, "10", "DOACAO", null, prazo), 200);
        expect("DOACAO with preço", "POST", "/api/lotes", lote(1, 1, "[TESTE] invalido", "5", "DOACAO", "3.00", prazo), 400);
        expect("VENDA without preço", "POST", "/api/lotes", lote(2, 1, "[TESTE] invalido", "5", "VENDA", null, prazo), 400);
        expect("deadline in the past", "POST", "/api/lotes", lote(1, 1, "[TESTE] invalido", "5", "DOACAO", null, passado), 400);
        long loteB = createdId(expect("create lote B (for xepa)", "POST", "/api/lotes",
                lote(1, 2, "[TESTE] Lote B " + RUN, "4", "DOACAO", null, prazo), 201), "idLote", MAX_LOTE, "POST", "/api/lotes");
        long loteV = createdId(expect("create lote V (VENDA, to delete)", "POST", "/api/lotes",
                lote(2, 4, "[TESTE] Lote V " + RUN, "3", "VENDA", "2.50", prazo), 201), "idLote", MAX_LOTE, "POST", "/api/lotes");

        section("Reservas");
        Resp r1 = expect("reserve lote A", "POST", "/api/reservas", reserva(loteA, 1, null), 201);
        long res1 = createdId(r1, "idReserva", MAX_RESERVA, "POST", "/api/reservas");
        expectField("lote A after reserva", "/api/lotes/" + loteA, "status", "RESERVADO");
        expect("second reserva on the same lote", "POST", "/api/reservas", reserva(loteA, 2, null), 409);
        Resp rp = expect("change ponto of reserva", "PUT", "/api/reservas/" + res1, "{\"idPonto\":1}", 200);
        check(num(rp.body, "idPonto") == 1, "PUT", "/api/reservas/" + res1, "idPonto=1", raw(rp.body, "idPonto"), "ponto changed");
        expect("get reserva", "GET", "/api/reservas/" + res1, null, 200);
        expect("list reservas of lote A", "GET", "/api/reservas?idLote=" + loteA, null, 200);
        Resp c1 = expect("cancel reserva", "PUT", "/api/reservas/" + res1 + "/cancelar", null, 200);
        check("CANCELADA".equals(str(c1.body, "status")), "PUT", "/api/reservas/" + res1 + "/cancelar",
                "status=CANCELADA", str(c1.body, "status"), "reserva cancelled");
        expectField("lote A after cancel", "/api/lotes/" + loteA, "status", "DISPONIVEL");
        expect("delete cancelled reserva", "DELETE", "/api/reservas/" + res1, null, 204);
        Resp r2 = expect("reserve lote A again", "POST", "/api/reservas", reserva(loteA, 1, 2L), 201);
        long res2 = createdId(r2, "idReserva", MAX_RESERVA, "POST", "/api/reservas");
        expect("delete ATIVA reserva", "DELETE", "/api/reservas/" + res2, null, 409);
        Resp ret = expect("confirm retirada", "PUT", "/api/reservas/" + res2 + "/retirada", null, 200);
        check("RETIRADA".equals(str(ret.body, "status")) && str(ret.body, "dataRetirada") != null,
                "PUT", "/api/reservas/" + res2 + "/retirada", "status=RETIRADA and dataRetirada set",
                str(ret.body, "status") + ", dataRetirada=" + raw(ret.body, "dataRetirada"), "retirada recorded");
        expectField("lote A after retirada", "/api/lotes/" + loteA, "status", "RETIRADO");
        expect("cancel a RETIRADA reserva", "PUT", "/api/reservas/" + res2 + "/cancelar", null, 409);
        expect("PUT on a non-DISPONIVEL lote", "PUT", "/api/lotes/" + loteA,
                lote(1, 1, "[TESTE] Lote A " + RUN, "10", "DOACAO", null, prazo), 409);
        long ben = createdId(expect("create beneficiário", "POST", "/api/beneficiarios",
                beneficiario("[TESTE] Beneficiario " + RUN, null), 201), "idBeneficiario", MAX_BENEFICIARIO,
                "POST", "/api/beneficiarios");
        expect("deactivate beneficiário", "DELETE", "/api/beneficiarios/" + ben, null, 204);
        expect("inactive beneficiário reserves", "POST", "/api/reservas", reserva(loteB, ben, null), 400);

        section("Destinações");
        Resp dl = expect("consumo + compostagem of lote A", "POST", "/api/lotes/" + loteA + "/destinacoes",
                "{\"consumoKg\":7,\"compostagemKg\":2}", 201);
        List<String> ds = objects(dl.body);
        check(ds.size() == 2 && ds.stream().allMatch(d -> num(d, "idDestinacao") > MAX_DESTINACAO),
                "POST", "/api/lotes/" + loteA + "/destinacoes", "2 destinos with id>" + MAX_DESTINACAO,
                ds.size() + " destino(s)", "identity on T_RP_DESTINACAO");
        expect("destino over the lote weight", "POST", "/api/destinacoes", destinacao(loteA, "CONSUMO", "5"), 400);
        long dest = createdId(expect("create destino (1 kg compostagem)", "POST", "/api/destinacoes",
                destinacao(loteA, "COMPOSTAGEM", "1"), 201), "idDestinacao", MAX_DESTINACAO, "POST", "/api/destinacoes");
        expect("get destino", "GET", "/api/destinacoes/" + dest, null, 200);
        expect("list destinos of lote A", "GET", "/api/destinacoes?idLote=" + loteA, null, 200);
        expect("update destino", "PUT", "/api/destinacoes/" + dest, destinacao(loteA, "COMPOSTAGEM", "0.5"), 200);
        expect("delete destino", "DELETE", "/api/destinacoes/" + dest, null, 204);

        section("Alertas and xepa");
        Resp xp = expect("xepa by feirante 1", "POST", "/api/alertas/xepa",
                "{\"idOfertante\":1,\"idsLotes\":[" + loteB + "],\"novoPrazo\":null,\"canal\":\"APP\"}", 201);
        List<String> xa = objects(arrayField(xp.body, "alertas"));
        long xepaId = xa.isEmpty() ? -1 : num(xa.get(0), "idAlerta");
        check(xa.size() == 1 && xepaId > MAX_ALERTA, "POST", "/api/alertas/xepa", "1 XEPA alert with id>" + MAX_ALERTA,
                xa.size() + " alert(s), id=" + xepaId, "xepa alert created");
        expect("xepa by a SUPERMERCADO", "POST", "/api/alertas/xepa",
                "{\"idOfertante\":2,\"idsLotes\":[],\"novoPrazo\":null,\"canal\":\"APP\"}", 400);
        Resp rec = expect("recent alerts", "GET", "/api/alertas/recentes?horas=24", null, 200);
        check(hasId(rec.body, "idAlerta", xepaId), "GET", "/api/alertas/recentes", "contains alert " + xepaId,
                hasId(rec.body, "idAlerta", xepaId) ? "present" : "missing", "xepa is listed");
        long ale = createdId(expect("create alert", "POST", "/api/alertas",
                "{\"idLote\":" + loteB + ",\"tipo\":\"NOVO_LOTE\",\"canal\":\"WHATSAPP\"}", 201), "idAlerta", MAX_ALERTA,
                "POST", "/api/alertas");
        expect("get alert", "GET", "/api/alertas/" + ale, null, 200);
        expect("list XEPA alerts", "GET", "/api/alertas?tipo=XEPA", null, 200);
        expect("update alert", "PUT", "/api/alertas/" + ale, "{\"tipo\":\"XEPA\",\"canal\":\"APP\"}", 200);
        expect("delete alert", "DELETE", "/api/alertas/" + ale, null, 204);

        section("Selo (CK_RP_OFERTANTE_SELO_DATA)");
        Resp o3 = expect("get ofertante 3", "GET", "/api/ofertantes/3", null, 200);
        if ("true".equals(raw(o3.body, "possuiSelo"))) {
            expect("reset: ofertante 3 already had the selo, revoke first", "DELETE", "/api/ofertantes/3/selo", null, 204);
        }
        Resp s1 = expect("grant selo to ofertante 3", "PUT", "/api/ofertantes/3/selo", null, 200);
        check("true".equals(raw(s1.body, "possuiSelo")) && str(s1.body, "dataSelo") != null, "PUT",
                "/api/ofertantes/3/selo", "possuiSelo=true, dataSelo set",
                raw(s1.body, "possuiSelo") + ", " + raw(s1.body, "dataSelo"), "selo granted");
        expect("grant selo again", "PUT", "/api/ofertantes/3/selo", null, 409);
        expect("revoke selo", "DELETE", "/api/ofertantes/3/selo", null, 204);
        Resp s2 = expect("ofertante 3 after revoke", "GET", "/api/ofertantes/3", null, 200);
        check("false".equals(raw(s2.body, "possuiSelo")) && "null".equals(raw(s2.body, "dataSelo")), "GET",
                "/api/ofertantes/3", "possuiSelo=false, dataSelo=null",
                raw(s2.body, "possuiSelo") + ", " + raw(s2.body, "dataSelo"), "selo revoked");

        section("Logical delete and reactivation");
        long of = createdId(expect("create ofertante", "POST", "/api/ofertantes",
                ofertante("[TESTE] Ofertante " + RUN, end1, null), 201), "idOfertante", MAX_OFERTANTE,
                "POST", "/api/ofertantes");
        logical("ofertante", "/api/ofertantes/" + of, ofertante("[TESTE] Ofertante " + RUN, end1, true));
        logical("beneficiário", "/api/beneficiarios/" + ben, beneficiario("[TESTE] Beneficiario " + RUN, true));
        long po = createdId(expect("create ponto", "POST", "/api/pontos",
                ponto("[TESTE] Ponto " + RUN, end1, null), 201), "idPonto", MAX_PONTO, "POST", "/api/pontos");
        logical("ponto", "/api/pontos/" + po, ponto("[TESTE] Ponto " + RUN, end1, true));

        section("Physical delete");
        expect("delete unused test categoria", "DELETE", "/api/categorias/" + cat1, null, 204);
        long end2 = createdId(expect("create unused endereço", "POST", "/api/enderecos",
                endereco("[TESTE] Rua Apagar " + RUN), 201), "idEndereco", MAX_ENDERECO, "POST", "/api/enderecos");
        expect("delete unused test endereço", "DELETE", "/api/enderecos/" + end2, null, 204);
        expect("delete endereço in use (1)", "DELETE", "/api/enderecos/1", null, 409);
        expect("delete categoria with lotes (1)", "DELETE", "/api/categorias/1", null, 409);
        expect("delete lote with destinos (A)", "DELETE", "/api/lotes/" + loteA, null, 409);
        expect("delete DISPONIVEL test lote V", "DELETE", "/api/lotes/" + loteV, null, 204);

        section("Expiry, impacto and relatório");
        Resp ex = expect("run expiry", "POST", "/api/lotes/expirar", null, 200);
        check(num(ex.body, "expirados") >= 0, "POST", "/api/lotes/expirar", "expirados is a number",
                raw(ex.body, "expirados"), "count returned");
        Resp io = expect("impacto ofertante 1", "GET", "/api/impacto/ofertantes/1?mes=" + MES, null, 200);
        check(isNumber(raw(io.body, "totalKg")) && isNumber(raw(io.body, "percentualConsumo")), "GET",
                "/api/impacto/ofertantes/1", "numeric totalKg and percentualConsumo",
                raw(io.body, "totalKg") + ", " + raw(io.body, "percentualConsumo"), "panel numbers");
        Resp ir = expect("impacto rede", "GET", "/api/impacto/rede?mes=" + MES, null, 200);
        check(isNumber(raw(ir.body, "totalKg")) && num(ir.body, "totalKg") > 0, "GET", "/api/impacto/rede",
                "totalKg > 0", raw(ir.body, "totalKg"), "network totals include the seeded destinos");
        Resp rm = expect("relatório mensal ofertante 1", "GET", "/api/relatorios/ofertantes/1/mensal?mes=" + MES, null, 200);
        check(isNumber(raw(rm.body, "totalKg")), "GET", "/api/relatorios/ofertantes/1/mensal", "numeric totalKg",
                raw(rm.body, "totalKg"), "report numbers");

        section("404, malformed JSON, error body");
        for (String p : List.of("/api/categorias/", "/api/enderecos/", "/api/ofertantes/", "/api/lotes/",
                "/api/beneficiarios/", "/api/pontos/", "/api/reservas/", "/api/destinacoes/", "/api/alertas/",
                "/api/impacto/ofertantes/")) {
            expect("unknown id", "GET", p + "999999", null, 404);
        }
        expect("unknown id", "GET", "/api/relatorios/ofertantes/999999/mensal", null, 404);
        expect("malformed JSON", "POST", "/api/categorias", "{\"nome\": ", 400);
        expect("malformed JSON", "POST", "/api/lotes", "{\"idOfertante\":1,", 400);

        section("Cleanup (logical delete of test records, delete test lote B)");
        expect("cleanup ofertante", "DELETE", "/api/ofertantes/" + of, null, 204);
        expect("cleanup beneficiário", "DELETE", "/api/beneficiarios/" + ben, null, 204);
        expect("cleanup ponto", "DELETE", "/api/pontos/" + po, null, 204);
        expect("cleanup lote B (and its alerts)", "DELETE", "/api/lotes/" + loteB, null, 204);

        section("Coverage of docs/endpoints.md");
        coverage();
        finish();
    }

    // ---------- groups ----------

    static void seeded() {
        Resp o = expect("ofertantes", "GET", "/api/ofertantes?ativo=true", null, 200);
        countSeeded(o.body, "idOfertante", MAX_OFERTANTE, 5, "/api/ofertantes", "ofertantes");
        Resp c = expect("categorias", "GET", "/api/categorias", null, 200);
        countSeeded(c.body, "idCategoria", MAX_CATEGORIA, 5, "/api/categorias", "categorias");
        Resp b = expect("beneficiários", "GET", "/api/beneficiarios?ativo=true", null, 200);
        countSeeded(b.body, "idBeneficiario", MAX_BENEFICIARIO, 6, "/api/beneficiarios", "beneficiários");
        Resp p = expect("pontos", "GET", "/api/pontos?ativo=true", null, 200);
        countSeeded(p.body, "idPonto", MAX_PONTO, 5, "/api/pontos", "pontos");
        expect("endereços", "GET", "/api/enderecos", null, 200);
        expectField("lote 5", "/api/lotes/5", "status", "RESERVADO");
        Resp r = expect("ATIVA reserva of lote 5", "GET", "/api/reservas?idLote=5&status=ATIVA", null, 200);
        check(objects(r.body).size() == 1, "GET", "/api/reservas?idLote=5&status=ATIVA", "1 reserva",
                objects(r.body).size() + " reserva(s)", "seeded reserva 5 is ATIVA");
    }

    static void countSeeded(String body, String idField, long maxId, int expected, String path, String what) {
        long n = objects(body).stream().filter(o -> {
            long id = num(o, idField);
            return id >= 1 && id <= maxId;
        }).count();
        check(n == expected, "GET", path, expected + " seeded " + what, n + "", "data from Mariana's DML");
    }

    static void logical(String what, String path, String reactivateBody) {
        expect("delete " + what + " (logical)", "DELETE", path, null, 204);
        Resp g = expect("get inactive " + what, "GET", path, null, 200);
        check("false".equals(raw(g.body, "ativo")), "GET", path, "ativo=false", raw(g.body, "ativo"), what + " deactivated");
        Resp u = expect("reactivate " + what, "PUT", path, reactivateBody, 200);
        check("true".equals(raw(u.body, "ativo")), "PUT", path, "ativo=true", raw(u.body, "ativo"), what + " active again");
    }

    static void expectField(String label, String path, String field, String value) {
        Resp r = expect(label, "GET", path, null, 200);
        check(value.equals(str(r.body, field)), "GET", path, field + "=" + value, String.valueOf(str(r.body, field)), label);
    }

    static void coverage() throws Exception {
        Path doc = Path.of("docs/endpoints.md");
        if (!Files.exists(doc)) {
            check(false, "-", "docs/endpoints.md", "file found", "missing", "run from the repository root");
            return;
        }
        Pattern row = Pattern.compile("^\\| \\d+ \\| `(GET|POST|PUT|DELETE)` \\| `([^`]+)` \\|");
        List<String> all = new ArrayList<>();
        for (String line : Files.readAllLines(doc, StandardCharsets.UTF_8)) {
            Matcher m = row.matcher(line);
            if (m.find()) {
                all.add(m.group(1) + " " + m.group(2));
            }
        }
        List<String> missing = all.stream().filter(e -> !covered.contains(e)).toList();
        check(all.size() == 58 && missing.isEmpty(), "-", "docs/endpoints.md", "58 endpoints called",
                (all.size() - missing.size()) + " of " + all.size(), missing.isEmpty() ? "all covered" : "missing " + missing);
    }

    // ---------- request bodies ----------

    static String endereco(String logradouro) {
        return "{\"logradouro\":\"" + logradouro + "\",\"numero\":\"1\",\"complemento\":null,\"bairro\":\"Paraiso\","
                + "\"cidade\":\"Sao Paulo\",\"uf\":\"SP\",\"cep\":\"04007-000\"}";
    }

    static String categoria(String nome) {
        return "{\"nome\":\"" + nome + "\",\"descricao\":\"[TESTE] categoria do smoke test\"}";
    }

    static String lote(long of, long cat, String desc, String peso, String mod, String preco, String prazo) {
        return "{\"idOfertante\":" + of + ",\"idCategoria\":" + cat + ",\"descricao\":\"" + desc + "\",\"pesoKg\":" + peso
                + ",\"condicao\":\"BOA\",\"modalidade\":\"" + mod + "\",\"preco\":" + preco + ",\"valorEstimado\":20"
                + ",\"dataLimiteRetirada\":\"" + prazo + "\",\"urlFoto\":null,\"precisaTransporte\":false}";
    }

    static String reserva(long lote, long ben, Long ponto) {
        return "{\"idLote\":" + lote + ",\"idBeneficiario\":" + ben + ",\"idPonto\":" + ponto + "}";
    }

    static String destinacao(long lote, String tipo, String kg) {
        return "{\"idLote\":" + lote + ",\"tipo\":\"" + tipo + "\",\"pesoKg\":" + kg + ",\"dataDestinacao\":null}";
    }

    static String beneficiario(String nome, Boolean ativo) {
        return "{\"nome\":\"" + nome + "\",\"tipo\":\"FAMILIA\",\"qtdPessoas\":3,\"telefone\":null,\"idEndereco\":null"
                + ",\"ativo\":" + ativo + "}";
    }

    static String ofertante(String nome, long endereco, Boolean ativo) {
        return "{\"nome\":\"" + nome + "\",\"tipo\":\"FEIRANTE\",\"telefone\":\"11900000099\",\"email\":null"
                + ",\"idEndereco\":" + endereco + ",\"ativo\":" + ativo + "}";
    }

    static String ponto(String nome, long endereco, Boolean ativo) {
        return "{\"nome\":\"" + nome + "\",\"tipo\":\"ONG\",\"diasFuncionamento\":\"SAB\",\"horarioAbertura\":\"09:00\""
                + ",\"horarioFechamento\":\"12:00\",\"telefone\":null,\"idEndereco\":" + endereco + ",\"ativo\":" + ativo + "}";
    }

    // ---------- HTTP + checks ----------

    static Resp call(String method, String path, String body) {
        covered.add(method + " " + path.replaceAll("\\?.*$", "").replaceAll("/\\d+(?=/|$)", "/{id}"));
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path))
                    .timeout(Duration.ofSeconds(30)).header("Accept", "application/json");
            HttpRequest.BodyPublisher pub = body == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
            if (body != null) {
                b.header("Content-Type", "application/json");
            }
            HttpResponse<String> r = http.send(b.method(method, pub).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Resp(r.statusCode(), r.body());
        } catch (Exception e) {
            return new Resp(-1, "connection error: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    static Resp expect(String label, String method, String path, String body, int expected) {
        Resp r = call(method, path, body);
        boolean ok = r.status == expected;
        String reason = label;
        if (ok && expected >= 400) {
            boolean shape = raw(r.body, "status") != null && str(r.body, "message") != null && str(r.body, "path") != null;
            if (!shape) {
                ok = false;
                reason += " — error body lacks status/message/path";
            } else {
                reason += " — \"" + str(r.body, "message") + "\"";
            }
        } else if (!ok) {
            String msg = str(r.body, "message");
            reason += " — " + (msg != null ? "\"" + msg + "\"" : abbreviate(r.body));
        }
        line(ok, method, path, String.valueOf(expected), String.valueOf(r.status), reason);
        return r;
    }

    static void check(boolean ok, String method, String path, String expected, String actual, String reason) {
        line(ok, method, path, expected, actual, reason);
    }

    static void line(boolean ok, String method, String path, String expected, String actual, String reason) {
        String s = String.format("%s  %-6s %-48s expected %-10s got %-10s %s", ok ? "PASS" : "FAIL", method,
                abbreviate(path, 48), expected, actual, reason);
        System.out.println(s);
        if (ok) {
            passed++;
        } else {
            failed++;
            failures.add(s);
        }
    }

    static long createdId(Resp r, String field, long seededMax, String method, String path) {
        long id = num(r.body, field);
        if (r.status == 201) {
            check(id > seededMax, method, path, field + ">" + seededMax, String.valueOf(id), "identity after the seeded ids");
        }
        return id;
    }

    static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    static void finish() {
        System.out.println();
        System.out.println("==== Summary: " + (passed + failed) + " checks, " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.out.println("Failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }
        System.exit(failed > 0 ? 1 : 0);
    }

    // ---------- tiny JSON helpers (regex) ----------

    static String str(String json, String field) {
        if (json == null) {
            return null;
        }
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /** Raw value text: number, true/false/null or the string content. */
    static String raw(String json, String field) {
        if (json == null) {
            return null;
        }
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\"(?:[^\"\\\\]|\\\\.)*\"|[^,}\\]]+)").matcher(json);
        if (!m.find()) {
            return null;
        }
        String v = m.group(1).trim();
        return v.startsWith("\"") ? v.substring(1, v.length() - 1) : v;
    }

    static long num(String json, String field) {
        String v = raw(json, field);
        try {
            return v == null ? -1 : (long) Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static boolean isNumber(String v) {
        return v != null && v.matches("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
    }

    static boolean hasId(String json, String field, long id) {
        return objects(json).stream().anyMatch(o -> num(o, field) == id);
    }

    /** Text of the array value of a field ("[...]"), or "[]". */
    static String arrayField(String json, String field) {
        if (json == null) {
            return "[]";
        }
        int i = json.indexOf("\"" + field + "\"");
        int start = i < 0 ? -1 : json.indexOf('[', i);
        if (start < 0) {
            return "[]";
        }
        int depth = 0;
        boolean inStr = false;
        for (int k = start; k < json.length(); k++) {
            char ch = json.charAt(k);
            if (inStr) {
                if (ch == '\\') {
                    k++;
                } else if (ch == '"') {
                    inStr = false;
                }
            } else if (ch == '"') {
                inStr = true;
            } else if (ch == '[') {
                depth++;
            } else if (ch == ']' && --depth == 0) {
                return json.substring(start, k + 1);
            }
        }
        return "[]";
    }

    /** Splits a JSON array into its top-level objects. */
    static List<String> objects(String json) {
        List<String> out = new ArrayList<>();
        if (json == null || !json.trim().startsWith("[")) {
            return out;
        }
        int depth = 0;
        int start = -1;
        boolean inStr = false;
        for (int k = 0; k < json.length(); k++) {
            char ch = json.charAt(k);
            if (inStr) {
                if (ch == '\\') {
                    k++;
                } else if (ch == '"') {
                    inStr = false;
                }
            } else if (ch == '"') {
                inStr = true;
            } else if (ch == '{') {
                if (depth++ == 0) {
                    start = k;
                }
            } else if (ch == '}' && --depth == 0) {
                out.add(json.substring(start, k + 1));
            }
        }
        return out;
    }

    static String abbreviate(String s) {
        return abbreviate(s == null ? "" : s.replaceAll("\\s+", " "), 120);
    }

    static String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
