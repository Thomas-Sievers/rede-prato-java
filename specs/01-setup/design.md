# 01 — Setup · Design

## Build
- Maven, parent `spring-boot-starter-parent` 4.1.1 (same as the teacher's CP3 project), Java 21.
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-h2console`, `lombok` (optional + annotation processor), `h2` (runtime), `ojdbc17` (runtime), `spring-boot-starter-test` (test).
- Maven wrapper (`./mvnw`). Plain jar disabled so `target/*.jar` matches one file.
- Multi-stage `Dockerfile` (temurin 21 jdk → jre), `.dockerignore`.

## Configuration
- `application.properties`: H2 in-memory `MODE=Oracle`, `create-drop`, `open-in-view=false`, `server.port=${PORT:8080}`, `app.seed.enabled=true`.
- `application-oracle.properties`: datasource from env vars only, `validate`, `app.seed.enabled=false`.
- `config/ClockConfig`: `Clock.system(ZoneId.of(app.timezone))`.
- `config/WebConfig`: CORS allow all origins on `/api/**`.
- `RedePratoApplication`: `TimeZone.setDefault(America/Sao_Paulo)`, `@EnableScheduling`.

## Endpoint
| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | `/api/health` | — | `{"status":"UP","app":"Rede de Prato","profile":"default","time":"2026-11-17T10:00:00-03:00"}` | 200 |

`profile` = active profiles joined by `,`, or `default`.

## Exception layer (`br.com.fiap.redeprato.exception`)
- `NotFoundException`, `BusinessRuleException`, `ConflictException` (all `RuntimeException`, message in Portuguese).
- `ApiError` record: `OffsetDateTime timestamp, int status, String error, String message, String path, Map<String,String> fields`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`), mapping:

| Exception | Status | Message |
| --- | --- | --- |
| NotFoundException | 404 | exception message |
| BusinessRuleException | 400 | exception message |
| ConflictException | 409 | exception message |
| MethodArgumentNotValidException | 400 | `"Dados inválidos."` + `fields` |
| HttpMessageNotReadableException | 400 | `"Requisição inválida: corpo da requisição mal formatado."` |
| MethodArgumentTypeMismatchException | 400 | `"Requisição inválida: parâmetro '<n>' com valor inválido."` |
| MissingServletRequestParameterException | 400 | `"Requisição inválida: parâmetro '<n>' é obrigatório."` |
| DataIntegrityViolationException (ORA-00001 / unique / 23505) | 409 | `"Registro duplicado: já existe um registro com esses dados."` |
| DataIntegrityViolationException (ORA-02292 / referential / 23503) | 409 | `"Não é possível excluir: existem registros ligados a este."` |
| other DataIntegrityViolationException | 400 | `"Dados inválidos: uma restrição do banco foi violada."` |
| NoResourceFoundException | 404 | `"Recurso não encontrado."` |
| HttpRequestMethodNotSupportedException | 405 | `"Método não permitido para este recurso."` |
| Exception | 500 | `"Erro interno. Tente novamente."` |

The handler reads the most specific cause message of `DataIntegrityViolationException` to classify it.

## Converters (`br.com.fiap.redeprato.converter`)
- `BooleanSNConverter implements AttributeConverter<Boolean,String>` (not auto-applied).
- `LocalTimeHHMMConverter implements AttributeConverter<LocalTime,String>`, pattern `HH:mm`, strict.

## Tests
- `BooleanSNConverterTest`, `LocalTimeHHMMConverterTest` (all branches).
- `GlobalExceptionHandlerTest` (MockMvc standalone with a test controller throwing each exception).
- `RedePratoApplicationTests` (context load), `HealthControllerTest` (MockMvc).
