package br.com.fiap.redeprato.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** The static frontend is served by the same Spring Boot app. */
@SpringBootTest
class FrontendPagesTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void raizEncaminhaParaALanding() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
    }

    @Test
    void landingServida() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rede de Prato")))
                .andExpect(content().string(containsString("Procure esta placa")));
    }

    @Test
    void telaDeLotesServida() throws Exception {
        mvc.perform(get("/lotes.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escolha sua comida")))
                .andExpect(content().string(containsString("/js/lotes.js")));
    }

    @Test
    void telaDeRegistrarServida() throws Exception {
        mvc.perform(get("/registrar.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar sobra")))
                .andExpect(content().string(containsString("Fim de feira?")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/css/app.css", "/js/api.js", "/js/persona.js", "/js/ui.js", "/js/lotes.js",
        "/js/registrar.js", "/img/logo.png"})
    void assetsServidos(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }
}
