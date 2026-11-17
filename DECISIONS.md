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
