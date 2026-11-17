# Decisions

Append-only log of decisions taken while implementing `docs/PLAN.md` (final project decisions live in `docs/decisoes.md`).

## D-1 · 2026-11-17 · feature 00
**Decision:** Packages are named `dao` and `bo` (not `repository`/`service`).
**Why:** The graders look for the DAO and BO layers by name (rubric "Camadas DAO e BO"). Classes keep the teacher's style: Spring Data repositories (`XxxDAO extends JpaRepository`) and `@Service` classes with `validate()`.
**Alternatives considered:** `repository`/`service` (Spring convention) — rejected, harder for the graders to map to the rubric.

## D-2 · 2026-11-17 · feature 01
**Decision:** Spring Boot 4.1.1, `spring-boot-starter-web`, `spring-boot-h2console` and `ojdbc17` (instead of 4.0.x / `ojdbc11` from PLAN §3.4).
**Why:** PLAN §3.4 says to follow the teacher's `pom.xml` (CP3-Java), which uses exactly these; they resolve and are proven in class. Direct access to Maven Central metadata was not permitted to look up the latest 4.0.x, and 4.1.1 is a 4.x release.
**Alternatives considered:** latest 4.0.x (unknown exact version offline); `ojdbc11` (works with JDK 21 too, but differs from the teacher).

## D-3 · 2026-11-17 · feature 01
**Decision:** MockMvc tests use `MockMvcBuilders.standaloneSetup(...)` with the `GlobalExceptionHandler`; only the context-load test uses `@SpringBootTest`.
**Why:** Boot 4 moved test-slice annotations into separate modules; standalone setup needs no extra dependency and is fast (allowed by PLAN §2.5).
**Alternatives considered:** `@WebMvcTest` with the `spring-boot-webmvc-test` module.

## D-4 · 2026-11-17 · feature 01
**Decision:** Extra error mappings beyond PLAN §3.5: `HttpRequestMethodNotSupportedException` → 405; generic `DataIntegrityViolationException` → 400 `"Dados inválidos: uma restrição do banco foi violada."`; `MethodArgumentNotValidException` message `"Dados inválidos."`.
**Why:** The plan allows "400 or 409 with a clear message"; 400 fits CHECK/NOT NULL violations (bad data, not state conflict). 405 avoids a misleading 500.
**Alternatives considered:** 409 for every integrity error.

## D-5 · 2026-11-17 · feature 02
**Decision:** All business validation lives in the BOs (private `checkXxx()` methods called by `validate()`); request records carry no Bean Validation annotations. BOs receive request records and return response records; DTOs are grouped as nested records in `XxxDTO` holder classes (`CategoriaDTO.Request`, `CategoriaDTO.Response`).
**Why:** One place per rule, unit-testable with Mockito and asserting the exact Portuguese message; duplicating rules in annotations risks two different messages for the same rule. Mapping to response DTOs inside the BO transaction avoids `LazyInitializationException` (relations are LAZY and `open-in-view=false`). The `MethodArgumentNotValidException` mapping stays in the handler for completeness.
**Alternatives considered:** `@Valid` + annotations on requests; returning entities and mapping in controllers (needs open-in-view or fetch joins everywhere).

## D-6 · 2026-11-17 · feature 03
**Decision:** Reactivating an ofertante (and later beneficiário/ponto) is done with `PUT /api/<recurso>/{id}` and `"ativo": true` in the body; `ativo` absent/null keeps the current value. `GET` lists default to `ativo=true`; `?ativo=false` lists the inactive ones. Revoking a selo that does not exist is a no-op (204, idempotent).
**Why:** PLAN BR-03.9 asks for it; keeps the CRUD at four verbs without an extra "reativar" endpoint.
**Alternatives considered:** `PUT /{id}/reativar`; `?ativo=todos`.

## D-7 · 2026-11-17 · feature 04
**Decision:** When a RESERVADO lote expires (deadline passed without pickup), its ATIVA reserva becomes CANCELADA in the same transaction (wired in feature 07). Expiry runs every 5 minutes (`ExpiracaoJob`, `app.expiracao.enabled`), at the start of `GET /api/lotes/disponiveis`, before creating a reserva, and on `POST /api/lotes/expirar`.
**Why:** PLAN BR-04.13; the lote can no longer be picked up, and leaving an ATIVA reserva on an EXPIRADO lote would block the "one ATIVA per lote" index forever and show a false pending pickup.
**Alternatives considered:** keep the reserva ATIVA until someone cancels it manually.

## D-8 · 2026-11-17 · feature 04
**Decision:** `PUT /api/lotes/{id}` ignores `idOfertante` (a lote never changes owner); deadlines are truncated to whole seconds; `precisaTransporte` null → false.
**Why:** PLAN §6/04 ("same fields except idOfertante"); Oracle `DATE` has second precision.
**Alternatives considered:** 400 when idOfertante differs.
