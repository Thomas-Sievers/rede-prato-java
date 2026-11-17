package br.com.fiap.redeprato.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** End-to-end API checks on H2 (real BOs, DAOs and exception handler). */
@SpringBootTest
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void categoriaCrudFlow() throws Exception {
        String body = "{\"nome\":\"Teste Integracao\",\"descricao\":\"x\"}";
        String location = mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/categorias/")))
                .andExpect(jsonPath("$.nome").value("Teste Integracao"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"teste integracao\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe uma categoria com esse nome."));

        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void enderecoInvalidoDa400ComMensagem() throws Exception {
        mvc.perform(post("/api/enderecos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"logradouro\":\"Rua A\",\"bairro\":\"Paraíso\",\"cidade\":\"São Paulo\",\"uf\":\"SP\",\"cep\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CEP deve ter 8 dígitos."))
                .andExpect(jsonPath("$.path").value("/api/enderecos"));
    }

    private Long postId(String path, String json, String idField) throws Exception {
        String body = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return ((Number) com.jayway.jsonpath.JsonPath.read(body, "$." + idField)).longValue();
    }

    private Long novoEndereco() throws Exception {
        return postId("/api/enderecos", "{\"logradouro\":\"Rua Teste\",\"numero\":\"1\",\"bairro\":\"Paraíso\","
                + "\"cidade\":\"São Paulo\",\"uf\":\"SP\",\"cep\":\"04001-000\"}", "idEndereco");
    }

    @Test
    void ofertanteFlowComSeloEExclusaoLogica() throws Exception {
        Long idEnd = novoEndereco();
        Long id = postId("/api/ofertantes", "{\"nome\":\"Banca Integração\",\"tipo\":\"FEIRANTE\","
                + "\"telefone\":\"(11) 90000-0099\",\"idEndereco\":" + idEnd + "}", "idOfertante");

        mvc.perform(put("/api/ofertantes/" + id + "/selo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possuiSelo").value(true))
                .andExpect(jsonPath("$.dataSelo").isNotEmpty());
        mvc.perform(put("/api/ofertantes/" + id + "/selo")).andExpect(status().isConflict());

        mvc.perform(delete("/api/enderecos/" + idEnd))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível excluir: este endereço está em uso."));

        mvc.perform(delete("/api/ofertantes/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/ofertantes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/ofertantes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.bairro").value("Paraíso"));
        mvc.perform(get("/api/ofertantes").param("ativo", "false").param("tipo", "FEIRANTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idOfertante == " + id + ")]").exists());
        mvc.perform(get("/api/ofertantes").param("tipo", "PADARIA")).andExpect(status().isBadRequest());
    }

    @Test
    void loteFlowMantemHorarioNoH2EApareceEmDisponiveis() throws Exception {
        Long idEnd = novoEndereco();
        Long idOf = postId("/api/ofertantes", "{\"nome\":\"Banca Lote IT\",\"tipo\":\"FEIRANTE\","
                + "\"telefone\":\"11900000098\",\"idEndereco\":" + idEnd + "}", "idOfertante");
        Long idCat = postId("/api/categorias", "{\"nome\":\"Cat Lote IT\"}", "idCategoria");
        String prazo = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).plusDays(1) + "T18:30:00";

        Long idLote = postId("/api/lotes", "{\"idOfertante\":" + idOf + ",\"idCategoria\":" + idCat
                + ",\"descricao\":\"Tomates IT\",\"pesoKg\":8.5,\"condicao\":\"MADURA\",\"modalidade\":\"DOACAO\","
                + "\"dataLimiteRetirada\":\"" + prazo + "\"}", "idLote");

        mvc.perform(get("/api/lotes/" + idLote))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataLimiteRetirada").value(prazo))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.nomeCategoria").value("Cat Lote IT"))
                .andExpect(jsonPath("$.pesoKg").value(8.5));
        mvc.perform(get("/api/lotes/disponiveis").param("idCategoria", idCat.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idLote").value(idLote));
        mvc.perform(delete("/api/categorias/" + idCat)).andExpect(status().isConflict());
        mvc.perform(post("/api/lotes/expirar")).andExpect(status().isOk()).andExpect(jsonPath("$.expirados").isNumber());

        mvc.perform(post("/api/lotes").contentType(MediaType.APPLICATION_JSON).content("{\"idOfertante\":" + idOf
                        + ",\"idCategoria\":" + idCat + ",\"descricao\":\"X\",\"pesoKg\":1,\"condicao\":\"BOA\","
                        + "\"modalidade\":\"DOACAO\",\"preco\":3,\"dataLimiteRetirada\":\"" + prazo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Doação não tem preço."));
    }

    @Test
    void pontoHorarioIdaEVoltaPeloBanco() throws Exception {
        Long idEnd = novoEndereco();
        Long id = postId("/api/pontos", "{\"nome\":\"Ponto IT\",\"tipo\":\"ASSOCIACAO\",\"diasFuncionamento\":\"TER e QUI\","
                + "\"horarioAbertura\":\"16:00\",\"horarioFechamento\":\"18:30\",\"idEndereco\":" + idEnd + "}", "idPonto");
        mvc.perform(get("/api/pontos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarioAbertura").value("16:00"))
                .andExpect(jsonPath("$.horarioFechamento").value("18:30"));
        mvc.perform(delete("/api/enderecos/" + idEnd)).andExpect(status().isConflict());
        mvc.perform(delete("/api/pontos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pontos/" + id)).andExpect(jsonPath("$.ativo").value(false));
    }

    /** Creates ofertante + categoria + a DISPONIVEL lote; returns {idOfertante, idLote}. */
    private long[] novoLote(String tipoOfertante) throws Exception {
        Long idEnd = novoEndereco();
        Long idOf = postId("/api/ofertantes", "{\"nome\":\"Banca Fluxo\",\"tipo\":\"" + tipoOfertante + "\","
                + "\"telefone\":\"11900000097\",\"idEndereco\":" + idEnd + "}", "idOfertante");
        Long idCat = postId("/api/categorias", "{\"nome\":\"Cat " + System.nanoTime() + "\"}", "idCategoria");
        String prazo = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).plusDays(1) + "T12:00:00";
        Long idLote = postId("/api/lotes", "{\"idOfertante\":" + idOf + ",\"idCategoria\":" + idCat
                + ",\"descricao\":\"Bananas\",\"pesoKg\":6,\"condicao\":\"MADURA\",\"modalidade\":\"DOACAO\","
                + "\"dataLimiteRetirada\":\"" + prazo + "\"}", "idLote");
        return new long[] {idOf, idLote};
    }

    @Test
    void reservaFlowUc12() throws Exception {
        long[] ids = novoLote("FEIRANTE");
        long idLote = ids[1];
        Long idBen = postId("/api/beneficiarios", "{\"nome\":\"Rosana IT\",\"tipo\":\"FAMILIA\",\"qtdPessoas\":4}",
                "idBeneficiario");

        Long idRes = postId("/api/reservas", "{\"idLote\":" + idLote + ",\"idBeneficiario\":" + idBen + "}", "idReserva");
        mvc.perform(get("/api/reservas/" + idRes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroRetirada").value(idRes))
                .andExpect(jsonPath("$.localRetirada").value("OFERTANTE"))
                .andExpect(jsonPath("$.statusLote").value("RESERVADO"));
        mvc.perform(get("/api/lotes/disponiveis"))
                .andExpect(jsonPath("$[?(@.idLote == " + idLote + ")]").doesNotExist());

        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLote\":" + idLote + ",\"idBeneficiario\":" + idBen + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este lote já foi reservado por outra pessoa."));

        mvc.perform(delete("/api/lotes/" + idLote)).andExpect(status().isConflict());
        mvc.perform(delete("/api/reservas/" + idRes)).andExpect(status().isConflict());

        mvc.perform(put("/api/reservas/" + idRes + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.statusLote").value("DISPONIVEL"));
        mvc.perform(get("/api/lotes/disponiveis"))
                .andExpect(jsonPath("$[?(@.idLote == " + idLote + ")]").exists());
        mvc.perform(get("/api/reservas").param("idBeneficiario", idBen.toString()).param("status", "CANCELADA"))
                .andExpect(jsonPath("$[0].idReserva").value(idRes));

        // decisão 6: deleting the lote also deletes its CANCELADA reservas
        mvc.perform(delete("/api/lotes/" + idLote)).andExpect(status().isNoContent());
        mvc.perform(get("/api/reservas/" + idRes)).andExpect(status().isNotFound());
    }

    @Test
    void retiradaEDestinoFlow() throws Exception {
        long idLote = novoLote("SUPERMERCADO")[1];
        Long idBen = postId("/api/beneficiarios", "{\"nome\":\"Seu Jorge IT\",\"tipo\":\"PESSOA\"}", "idBeneficiario");

        mvc.perform(post("/api/destinacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLote\":" + idLote + ",\"tipo\":\"CONSUMO\",\"pesoKg\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registre a retirada antes do destino."));

        Long idRes = postId("/api/reservas", "{\"idLote\":" + idLote + ",\"idBeneficiario\":" + idBen + "}", "idReserva");
        mvc.perform(put("/api/reservas/" + idRes + "/retirada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRADA"))
                .andExpect(jsonPath("$.statusLote").value("RETIRADO"))
                .andExpect(jsonPath("$.dataRetirada").isNotEmpty());

        mvc.perform(post("/api/lotes/" + idLote + "/destinacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consumoKg\":5,\"compostagemKg\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(post("/api/destinacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLote\":" + idLote + ",\"tipo\":\"CONSUMO\",\"pesoKg\":0.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A soma dos destinos passa do peso do lote (6 kg)."));
        mvc.perform(get("/api/destinacoes").param("idLote", String.valueOf(idLote)))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(delete("/api/lotes/" + idLote)).andExpect(status().isConflict());

        // HU15: the painel computes kg from the destinações (JPQL aggregates on H2)
        String idOf = com.jayway.jsonpath.JsonPath.read(mvc.perform(get("/api/lotes/" + idLote)).andReturn()
                .getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8), "$.idOfertante").toString();
        mvc.perform(get("/api/impacto/ofertantes/" + idOf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalKg").value(6.0))
                .andExpect(jsonPath("$.consumoKg").value(5.0))
                .andExpect(jsonPath("$.compostagemKg").value(1.0))
                .andExpect(jsonPath("$.percentualConsumo").value(83.3))
                .andExpect(jsonPath("$.lotesRetirados").value(1))
                .andExpect(jsonPath("$.beneficiariosAtendidos").value(1))
                .andExpect(jsonPath("$.pessoasAtendidas").value(1))
                .andExpect(jsonPath("$.porSemana.length()").value(4))
                .andExpect(jsonPath("$.porSemana[3].kg").value(6.0));
        mvc.perform(get("/api/impacto/rede"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertantes[?(@.idOfertante == " + idOf + ")]").exists());
    }

    @Test
    void xepaFlowUc04() throws Exception {
        long[] ids = novoLote("FEIRANTE");
        long idOf = ids[0];
        long idLote = ids[1];
        mvc.perform(get("/api/alertas").param("idLote", String.valueOf(idLote)).param("tipo", "NOVO_LOTE"))
                .andExpect(jsonPath("$.length()").value(1));

        String body = mvc.perform(post("/api/alertas/xepa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idOfertante\":" + idOf + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade").value(1))
                .andExpect(jsonPath("$.mensagem").value("Xepa avisada para 1 lote."))
                .andExpect(jsonPath("$.alertas[0].tipo").value("XEPA"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        long idAlerta = ((Number) com.jayway.jsonpath.JsonPath.read(body, "$.alertas[0].idAlerta")).longValue();

        mvc.perform(get("/api/alertas/recentes").param("horas", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idAlerta == " + idAlerta + ")]").exists());
        mvc.perform(get("/api/lotes/disponiveis"))
                .andExpect(jsonPath("$[?(@.idLote == " + idLote + ")]").exists());
        mvc.perform(delete("/api/alertas/" + idAlerta)).andExpect(status().isNoContent());

        long idMercado = novoLote("SUPERMERCADO")[0];
        mvc.perform(post("/api/alertas/xepa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idOfertante\":" + idMercado + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Só feirantes podem avisar xepa."));

        // decisão 6: alerts go away with the lote
        mvc.perform(delete("/api/lotes/" + idLote)).andExpect(status().isNoContent());
        mvc.perform(get("/api/alertas").param("idLote", String.valueOf(idLote)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void enumInvalidoNoCorpoDa400() throws Exception {
        mvc.perform(post("/api/ofertantes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"tipo\":\"PADARIA\",\"telefone\":\"11900000001\",\"idEndereco\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void idNaoNumericoDa400() throws Exception {
        mvc.perform(get("/api/categorias/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rotaInexistenteDa404Json() throws Exception {
        mvc.perform(get("/api/naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
