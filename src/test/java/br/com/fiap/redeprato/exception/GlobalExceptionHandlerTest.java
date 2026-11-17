package br.com.fiap.redeprato.exception;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

    record Body(@NotBlank(message = "Nome é obrigatório.") String nome) {
    }

    @RestController
    static class FakeController {
        @GetMapping("/t/notfound")
        void notFound() {
            throw NotFoundException.of("Lote", 42);
        }

        @GetMapping("/t/business")
        void business() {
            throw new BusinessRuleException("Doação não tem preço.");
        }

        @GetMapping("/t/conflict")
        void conflict() {
            throw new ConflictException("Este lote já tem uma reserva ativa.");
        }

        @PostMapping("/t/valid")
        void valid(@Valid @RequestBody Body body) {
        }

        @GetMapping("/t/param")
        void param(@RequestParam Long id) {
        }

        @GetMapping("/t/ora00001")
        void ora1() {
            throw new DataIntegrityViolationException("could not execute statement",
                    new SQLIntegrityConstraintViolationException(
                            "ORA-00001: unique constraint (RM.UX_RP_RESERVA_ATIVA_LOTE) violated"));
        }

        @GetMapping("/t/h2unique")
        void h2unique() {
            throw new DataIntegrityViolationException("x",
                    new SQLException("Unique index or primary key violation: \"PUBLIC.UK ON T_RP\"; SQL statement", "23505"));
        }

        @GetMapping("/t/ora02292")
        void ora2292() {
            throw new DataIntegrityViolationException("x",
                    new SQLException("ORA-02292: integrity constraint (RM.FK_LOTE) violated - child record found"));
        }

        @GetMapping("/t/h2ref")
        void h2ref() {
            throw new DataIntegrityViolationException("x",
                    new SQLException("Referential integrity constraint violation: FK_X", "23503"));
        }

        @GetMapping("/t/ora02290")
        void ora2290() {
            throw new DataIntegrityViolationException("x",
                    new SQLException("ORA-02290: check constraint (RM.CK_LOTE_PESO) violated"));
        }

        @GetMapping("/t/boom")
        void boom() {
            throw new IllegalStateException("secret stack detail");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-17T13:15:30Z"), ZoneId.of("America/Sao_Paulo"));
        mvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void notFoundIs404WithStandardBody() throws Exception {
        mvc.perform(get("/t/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Lote 42 não encontrado."))
                .andExpect(jsonPath("$.path").value("/t/notfound"))
                .andExpect(jsonPath("$.timestamp").value("2026-11-17T10:15:30-03:00"))
                .andExpect(jsonPath("$.fields").value(nullValue()));
    }

    @Test
    void businessRuleIs400() throws Exception {
        mvc.perform(get("/t/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Doação não tem preço."));
    }

    @Test
    void conflictIs409() throws Exception {
        mvc.perform(get("/t/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este lote já tem uma reserva ativa."));
    }

    @Test
    void beanValidationIs400WithFields() throws Exception {
        mvc.perform(post("/t/valid").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos."))
                .andExpect(jsonPath("$.fields.nome").value("Nome é obrigatório."));
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/t/valid").contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição inválida: corpo da requisição mal formatado."));
    }

    @Test
    void typeMismatchIs400() throws Exception {
        mvc.perform(get("/t/param").param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição inválida: parâmetro 'id' com valor inválido."));
    }

    @Test
    void missingParamIs400() throws Exception {
        mvc.perform(get("/t/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição inválida: parâmetro 'id' é obrigatório."));
    }

    @Test
    void oracleUniqueViolationIs409() throws Exception {
        mvc.perform(get("/t/ora00001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MSG_DUPLICADO));
    }

    @Test
    void h2UniqueViolationIs409() throws Exception {
        mvc.perform(get("/t/h2unique"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MSG_DUPLICADO));
    }

    @Test
    void oracleChildRecordIs409() throws Exception {
        mvc.perform(get("/t/ora02292"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MSG_DEPENDENTES));
    }

    @Test
    void h2ReferentialViolationIs409() throws Exception {
        mvc.perform(get("/t/h2ref"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MSG_DEPENDENTES));
    }

    @Test
    void otherConstraintViolationIs400() throws Exception {
        mvc.perform(get("/t/ora02290"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MSG_RESTRICAO));
    }

    @Test
    void unexpectedIs500WithoutDetails() throws Exception {
        mvc.perform(get("/t/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente."));
    }

    @Test
    void wrongMethodIs405() throws Exception {
        mvc.perform(post("/t/boom"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
