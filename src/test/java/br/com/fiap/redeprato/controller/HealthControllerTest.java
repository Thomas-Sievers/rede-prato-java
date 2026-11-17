package br.com.fiap.redeprato.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void healthIsUpOnDefaultProfile() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new HealthController(new MockEnvironment(), clock)).build();
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.app").value("Rede de Prato"))
                .andExpect(jsonPath("$.profile").value("default"))
                .andExpect(jsonPath("$.time").value("2026-11-17T10:00-03:00"));
    }

    @Test
    void healthShowsActiveProfile() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("oracle");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new HealthController(env, clock)).build();
        mvc.perform(get("/api/health"))
                .andExpect(jsonPath("$.profile").value("oracle"));
    }
}
