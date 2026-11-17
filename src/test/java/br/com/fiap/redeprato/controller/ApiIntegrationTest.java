package br.com.fiap.redeprato.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
