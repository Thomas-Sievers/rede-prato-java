package br.com.fiap.redeprato.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new org.springframework.web.filter.CharacterEncodingFilter("UTF-8", true))
                .build();
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

    @Test
    void telaDeRetiradasServida() throws Exception {
        mvc.perform(get("/retiradas.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Retiradas")))
                .andExpect(content().string(containsString("Conferir")))
                .andExpect(content().string(containsString("Número da pessoa")));
    }

    @Test
    void telaDePainelServida() throws Exception {
        mvc.perform(get("/painel.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Meu impacto")))
                .andExpect(content().string(containsString("Quilos salvos por semana")));
    }

    @Test
    void openApiDisponivel() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/reservas")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/css/app.css", "/js/api.js", "/js/persona.js", "/js/ui.js", "/js/lotes.js",
        "/js/registrar.js", "/js/retiradas.js", "/js/painel.js", "/img/logo.png", "/img/logo_light.png",
        "/img/caixa-laranjas.png"})
    void assetsServidos(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }

    // ---------- feature 20: WCAG 2.2 AA (docs/redesign/README.md §3) ----------

    private String body(String path) throws Exception {
        return mvc.perform(get(path)).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/index.html", "/lotes.html", "/registrar.html", "/retiradas.html", "/painel.html"})
    void paginaAcessivel(String path) throws Exception {
        String html = body(path);
        assertThat(html).contains("<html lang=\"pt-BR\">");
        // A5: skip link is the first element in <body> and points to <main id="conteudo">
        Matcher first = Pattern.compile("<body[^>]*>\\s*(<[^>]+>)").matcher(html);
        assertThat(first.find()).isTrue();
        assertThat(first.group(1)).isEqualTo("<a class=\"skip\" href=\"#conteudo\">");
        assertThat(html).contains("<a class=\"skip\" href=\"#conteudo\">Pular para o conteúdo</a>");
        assertThat(html).contains("<main id=\"conteudo\" tabindex=\"-1\"");
        // A7: exactly one <h1> per page
        assertThat(Pattern.compile("<h1[\\s>]").matcher(html).results().count()).isEqualTo(1);
    }

    @Test
    void landingTemBotaoDePausaEImagemComAlt() throws Exception {
        String html = body("/index.html");
        // A1: real button to stop the marquee
        assertThat(html).containsPattern("<button type=\"button\" class=\"marquee-toggle\" id=\"marquee-toggle\" "
                + "aria-pressed=\"false\">.*Pausar animação</span></button>");
        // A4: hero image has alt text
        assertThat(html).contains("src=\"/img/caixa-laranjas.png\" alt=\"Caixa de feira de madeira cheia de laranjas\"");
        assertThat(html).contains("<linearGradient id=\"g-dark\"");
    }

    @Test
    void registrarTemH1EscondidoEH2DeSecao() throws Exception {
        String html = body("/registrar.html");
        assertThat(html).contains("<h1 class=\"sr-only\">Registrar sobra e avisar xepa</h1>");
        assertThat(html).contains("<h2 id=\"form-title\">Registrar sobra</h2>");
    }

    @Test
    void retiradasTemH2AntesDosCards() throws Exception {
        assertThat(body("/retiradas.html")).contains(">Reservas da sua banca</h2>");
    }

    @Test
    void painelGraficosComNomeAcessivel() throws Exception {
        String html = body("/painel.html");
        assertThat(html).contains("id=\"bars\" role=\"img\" aria-label=");
        assertThat(html).contains("id=\"split\" role=\"img\" aria-label=");
    }

    @Test
    void cssTemAnelDeFocoDeDuasCores() throws Exception {
        String css = body("/css/app.css");
        // A2: two-tone focus ring
        assertThat(css).containsPattern(":focus-visible\\s*\\{[^}]*outline:\\s*3px solid #1A3A2A[^}]*box-shadow:\\s*0 0 0 3px #FFF8EA");
        assertThat(css).contains(".marquee.paused .track { animation-play-state: paused; }");
        assertThat(css).containsPattern("prefers-reduced-motion: reduce\\)[^@]*\\.marquee \\.track \\{ animation: none; \\}");
    }
}
