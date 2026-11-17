# Rede de Prato — Implementation Plan for Claude Code

Version 1 · 2026-11-17 01:30 (America/Sao_Paulo) · Author: Thomas Sievers (with Claude)
Deadline: **2026-11-19 23:55** — commits after it zero the whole technical grade. Internal hard stop: **2026-11-19 22:00**.

This document is your complete instruction set. You run unattended overnight; Thomas reviews in the morning. Read it top to bottom once, then execute section 6 feature by feature.

---

## 1. Context in one page

**What the app is.** Rede de Prato connects surplus food from street markets (feirantes) and supermarkets (ofertantes) to low-income families and institutions (beneficiários) in the Paraíso neighbourhood of São Paulo. An ofertante registers a **lote** (batch, in kg); the platform creates an **alerta**; a beneficiário **reserva** the whole lote and picks it up at the store or at a **ponto de distribuição** (with the "placa"); the ofertante confirms the **retirada** and records the **destinação** (CONSUMO or COMPOSTAGEM). The **painel de impacto** sums kg saved per ofertante. Feirantes can send a one-tap **alerta de xepa** at the end of the market.

**What is graded (DDD – Java, see `docs/gs-ficha.md`).** Model layer matching the Oracle DB (10) · DAO and BO layers (10) · Controller with GET/POST/PUT/DELETE (10) · Exception layer (10) · **Deploy on Render (30)** · PDF docs (30: class diagram, endpoint table, DER, screens, run instructions).

**Docs you must use** (all in the repo):

| File | Use it for |
| --- | --- |
| `docs/decisoes.md` | Final decisions. Highest priority. Do not reopen. |
| `docs/classes.md` | Entities, exact Oracle types, enums, status transitions, deletion rules |
| `docs/backlog.md` | User stories (HU ids), MVP |
| `docs/casos-de-uso.md` | Use cases; full specs of UC12 (reservar) and UC04 (xepa) |
| `docs/prototipo.md` + `docs/prototipo/*.png` | Frontend build spec, design tokens, the 5 screens, logos |
| `docs/gs-ficha.md` | Ficha da CitizenTech (README top) + grading rubric |
| `db/README.md` | Mariana's DDL status (not delivered yet) |

**Teacher's reference pattern:** https://github.com/Thomas-Sievers/CP3-Java (you may WebFetch it from github.com / raw.githubusercontent.com). Copy its `pom.xml` structure, `application*.properties` style, service-validation style and test style. If it cannot be fetched, follow section 4 of this plan.

---

## 2. Operating rules (re-read after every compaction)

### 2.1 Autonomy
- **Never ask a question. Never wait for input. Never end your turn while features remain.** You are expected to work for many hours. When one feature is merged, immediately start the next.
- When something is unclear: choose the simplest option that fits the docs (priority order: `docs/decisoes.md` > DDL in `db/` > `docs/classes.md` > `docs/casos-de-uso.md` > `docs/backlog.md` > `docs/prototipo.md`), append an entry to `DECISIONS.md`, continue.
- When truly blocked (missing access, a tool permission is denied, a dependency cannot be resolved after 2 different attempts): append to `BLOCKED.md` (what, why, what you tried, what Thomas must do), skip only the blocked part, move on.
- A permission denial is not a reason to stop. Find an allowed alternative (e.g. `git rm` instead of `rm`; a test instead of running the server) or log it in `BLOCKED.md`.
- Do not loop: if the same test fails after 3 focused fix attempts, simplify the design (and the spec), or mark the case `@Disabled("see BLOCKED.md")` with an entry in `BLOCKED.md` — never leave `main` red.

### 2.2 Log files (repo root, committed)

`PROGRESS.md` — overwrite the status table after each feature:
```markdown
# Progress
Last update: <date time>
| NN | Feature | Status (todo/in progress/merged/blocked) | Tests | Notes |
...
## Next step
<exactly what to do next, so a fresh session can resume>
```
`DECISIONS.md` — append-only: `## D-<n> · <date> · <feature NN>` + **Decision**, **Why**, **Alternatives considered**.
`BLOCKED.md` — append-only: `## B-<n> · <date> · <feature NN>` + **What**, **Tried**, **Needs from Thomas**.
`prompts.md` — AI-use log required by the GS: the starting prompt Thomas typed, and one line per feature ("Claude Code implemented NN-name from specs/NN-…; key prompts/decisions: …").

### 2.3 Spec-Driven Development
For **every** feature NN (including setup and frontend), before any production code:
1. Create `specs/<NN>-<name>/requirements.md`: HU ids covered; acceptance criteria as numbered, testable **Given / When / Then**; business rules list with ids `BR-<NN>.<k>` (each rule = one or more tests).
2. Create `specs/<NN>-<name>/design.md`: entities/fields touched (with exact column types); endpoints table (method, path, request body, response body, status codes); validations; exceptions thrown and their HTTP status; DTO shapes (JSON examples); notes on transactions.
3. Create `specs/<NN>-<name>/tasks.md`: ordered checklist `- [ ]` of small steps (entity → DAO → BO + tests → DTOs → controller → wiring/seed → docs).
4. Commit the three files (`docs(spec): NN <name>`) **before** writing code.
5. Implement task by task, ticking `- [x]` in `tasks.md` (commit the ticks with the code).
6. If implementation must deviate, **edit the spec first**, commit, then change code.

### 2.4 Git flow
```bash
git switch main && git pull --ff-only origin main   # if pull is denied/fails offline, continue with local main and log it
git switch -c feature/<NN>-<short-name>
# spec commit, then small commits: feat(lote): ..., test(lote): ..., fix(...): ..., docs(...): ...
./mvnw -B test                                       # must be green
date                                                 # deadline guard (see 2.6)
git switch main
git merge --no-ff feature/<NN>-<short-name> -m "merge: feature NN <name>"
./mvnw -B test                                       # green again on main
git push origin main
git branch -d feature/<NN>-<short-name>
```
- Conventional-commit style, imperative, English, ≤ 72 chars subject.
- **Only exception to "never commit on main":** the initial commit of section 6 / step 0.
- Never push feature branches; only `git push origin main` (that also triggers the Render deploy).
- Never commit `target/`, IDE files, `.DS_Store`, `.claude/settings.local.json`, `.env*`, or any credential.

### 2.5 Tests (definition of done)
- **Unit tests for every BO**: JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`, DAOs mocked. At least one test per business rule `BR-xx.k` (assert the exception type **and** message), plus happy path and edge cases (nulls, boundaries, status transitions). You decide how many beyond that.
- Inject a `java.time.Clock` bean into BOs that use "now" so tests can fix time (`Clock.fixed(...)`).
- Integration tests are allowed only on H2: one `@SpringBootTest` context-load test, plus MockMvc tests where useful (exception-handler JSON, a happy-path flow). Prefer `MockMvcBuilders.standaloneSetup(...)` with the `@RestControllerAdvice` if Boot 4 test-slice annotations give trouble.
- Tests never use the `oracle` profile and never need network.
- A feature is **done** only when: spec exists, tasks ticked, `./mvnw -B test` green, merged, pushed, `PROGRESS.md` updated.

### 2.6 Deadline guard
Before every merge run `date`. If it is after **2026-11-19 22:00 -03**, do not commit or push anything else: write the situation in `PROGRESS.md` (uncommitted is fine) and stop.

### 2.7 Secrets and data
- Oracle config only via `${ORACLE_URL}`, `${ORACLE_USERNAME}`, `${ORACLE_PASSWORD}`. Never print them, never put real values in any file.
- Seed data is **simulated**: fictional names (Rosana, Seu Antônio, Banca do Seu Antônio, Hortifrúti do Paraíso, Mercado Bom Preço, Cozinha Solidária do Bairro…), fictional phones (`11900000001`…), real-looking but generic addresses in Paraíso (e.g. "Rua Manuel da Nóbrega" is a public street name; use plausible numbers). No CPF or documents anywhere.

---

## 3. Architecture

### 3.1 Layers and the rubric (put this table in the README)

| Rubric layer | Package | Contents |
| --- | --- | --- |
| Model | `br.com.fiap.redeprato.entity` (+ `entity.enums`) | 9 JPA entities mapped 1:1 to `T_RP_*` tables, 10 enums |
| DAO | `br.com.fiap.redeprato.dao` | `XxxDAO extends JpaRepository<Xxx, Long>` (Spring Data = DAO), custom JPQL queries |
| BO | `br.com.fiap.redeprato.bo` | `XxxBO` (`@Service`): business rules as private `checkXxx()` methods, each with a clear Portuguese message, called by `validate()` before save/update; transactions |
| Controller | `br.com.fiap.redeprato.controller` | `@RestController` per resource, GET/POST/PUT/DELETE under `/api` |
| Exception | `br.com.fiap.redeprato.exception` | `NotFoundException` (404), `BusinessRuleException` (400), `ConflictException` (409), `ApiError`, `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| (support) | `dto`, `converter`, `config` | Request/Response records, JPA converters, Clock/CORS/scheduling config, `DataSeeder` |

Decision (record as D-1 in `DECISIONS.md`): packages are named `dao` and `bo` (not `repository`/`service`) so the graders find the DAO and BO layers by name; classes keep the teacher's style (Spring Data repositories, services with `validate()`).

### 3.2 Entities (from `docs/classes.md`)
- Table names `T_RP_ENDERECO`, `T_RP_OFERTANTE`, `T_RP_CATEGORIA_ALIMENTO`, `T_RP_LOTE`, `T_RP_BENEFICIARIO`, `T_RP_PONTO_DISTRIBUICAO`, `T_RP_RESERVA`, `T_RP_DESTINACAO`, `T_RP_ALERTA`. Column names exactly as in the doc (snake_case). Java fields camelCase.
- **Every column declares `columnDefinition`** with the exact Oracle type: `NUMBER` for ids and FKs, `VARCHAR2(n)`, `CHAR(n)`, `NUMBER(8,2)`, `NUMBER(10,2)`, `NUMBER(4)`, `DATE`. This is required for `ddl-auto=validate` on Oracle.
- Ids: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`, type `Long`.
- FKs are object references (`@ManyToOne(fetch = LAZY) @JoinColumn(name = "id_ofertante", columnDefinition = "NUMBER")`), never loose `Long`s — the class diagram must equal the DER.
- Enums: `@Enumerated(EnumType.STRING)` + `columnDefinition = "VARCHAR2(n)"` with the size from the doc.
- S/N columns: `boolean` + `@Convert(converter = BooleanSNConverter.class)` + `columnDefinition = "CHAR(1)"`.
- `horario_abertura` / `horario_fechamento`: `LocalTime` + `LocalTimeHHMMConverter` (`"HH:mm"` string) + `columnDefinition = "VARCHAR2(5)"`.
- `DATE` columns: `LocalDateTime` (or `LocalDate` for `data_selo`) with `columnDefinition = "DATE"`.
- Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` on entities (no `@Data` on entities: avoid `equals/hashCode` and `toString` over lazy relations).
- No bidirectional collections unless needed; query children through DAOs.

### 3.3 Profiles and configuration
`src/main/resources/application.properties` (default = H2, used by tests, local runs and the first Render deploys):
```properties
spring.application.name=rede-de-prato
server.port=${PORT:8080}
spring.datasource.url=jdbc:h2:mem:redeprato;MODE=Oracle;DB_CLOSE_DELAY=-1;DEFAULT_NULL_ORDERING=HIGH
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
spring.jackson.time-zone=America/Sao_Paulo
app.timezone=America/Sao_Paulo
app.seed.enabled=true
```
(H2 in `MODE=Oracle` accepts `VARCHAR2`, `NUMBER(p,s)` and treats `DATE` as a timestamp — add a test proving `data_limite_retirada` keeps hours/minutes on H2.)

`application-oracle.properties` (activated by `SPRING_PROFILES_ACTIVE=oracle`):
```properties
spring.datasource.url=${ORACLE_URL}
spring.datasource.username=${ORACLE_USERNAME}
spring.datasource.password=${ORACLE_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.hibernate.ddl-auto=validate
app.seed.enabled=false
```
H2 console: enable only if it works without extra effort in Boot 4 (nice-to-have).

`Clock` bean: `Clock.system(ZoneId.of("America/Sao_Paulo"))`. Also set the JVM default zone at startup (`TimeZone.setDefault` in `main`) so Render (UTC) shows Brazilian times.

### 3.4 Build
- `pom.xml`: parent `spring-boot-starter-parent` **4.0.x** (use the latest 4.0.x that resolves; if 4.x cannot be resolved at all after two attempts, use the latest 3.5.x, log D-n and B-n). Java 21. Dependencies: web MVC starter, data-jpa starter, validation starter, Lombok (with annotation processor configured), H2 (runtime), `com.oracle.database.jdbc:ojdbc11` (runtime), test starter (JUnit 5, Mockito, AssertJ). In Boot 4 several starters were renamed/modularised (e.g. `spring-boot-starter-webmvc`, `*-test` modules): follow the teacher's `pom.xml`; if a starter name fails to resolve, check https://docs.spring.io and fix.
- Maven wrapper: generate with `mvn -N wrapper:wrapper` (Maven is installed), then `chmod +x mvnw`, then use only `./mvnw`.
- `Dockerfile` (multi-stage, used by Render):
  ```dockerfile
  FROM eclipse-temurin:21-jdk AS build
  WORKDIR /app
  COPY .mvn .mvn
  COPY mvnw pom.xml ./
  RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
  COPY src src
  RUN ./mvnw -B -q -DskipTests package
  FROM eclipse-temurin:21-jre
  WORKDIR /app
  COPY --from=build /app/target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java","-jar","/app/app.jar"]
  ```
  (Tests run locally before every merge; skipping them in the image keeps the free Render build fast.) Make sure only one jar matches `target/*.jar` (disable the plain jar if needed). Add `.dockerignore` (`target`, `.git`, `.idea`, `.claude`, `docs`, `specs`, `db`).
- `.gitignore`: `target/`, `.idea/`, `*.iml`, `.vscode/`, `.DS_Store`, `.claude/settings.local.json`, `.env`, `.env.*`, `*.log`.

### 3.5 Exception layer (feature 01, used everywhere)
JSON body for every error:
```json
{ "timestamp": "2026-11-17T10:15:30-03:00", "status": 409, "error": "Conflict",
  "message": "Este lote já tem uma reserva ativa.", "path": "/api/reservas", "fields": null }
```
| Exception | Status |
| --- | --- |
| `NotFoundException` | 404 (`"Lote 42 não encontrado."`) |
| `BusinessRuleException` | 400 |
| `ConflictException` | 409 |
| `MethodArgumentNotValidException` | 400, `fields` = `{campo: mensagem}` |
| `HttpMessageNotReadableException`, type mismatch, missing param | 400 (`"Requisição inválida: ..."`) |
| `DataIntegrityViolationException` containing `ORA-00001` (or H2 unique violation) | 409 `"Registro duplicado: já existe um registro com esses dados."` |
| `DataIntegrityViolationException` containing `ORA-02292` (child record) | 409 `"Não é possível excluir: existem registros ligados a este."` |
| other `DataIntegrityViolationException` (`ORA-02290` check, etc.) | 400 or 409 with a clear message |
| `NoResourceFoundException` on `/api/**` | 404 |
| any other `Exception` | 500 `"Erro interno. Tente novamente."` (log the stack trace, never return it) |

Unit-test the handler (each mapping, including a fake ORA-00001 message → 409).

### 3.6 API conventions
- Base path `/api`. JSON only. Request DTOs = Java `record`s with Bean Validation annotations; response DTOs = `record`s with flat fields (e.g. `LoteResponse` has `idOfertante`, `nomeOfertante`, `bairro`, not a nested Ofertante → Endereco tree). Mapping in static `from(entity)` methods or small mapper classes.
- POST → 201 + `Location` header + body. PUT → 200 + body. DELETE → 204. GET list → 200 (empty list, never 404).
- Enum values in JSON exactly as the Java enum names (`"DOACAO"`, `"DISPONIVEL"`).
- Dates in JSON ISO-8601 local (`"2026-11-17T18:00:00"`), times `"16:00"`, money/kg as numbers.
- CORS: allow all origins for `/api/**` (frontend is same-origin anyway; Swagger/other tools may need it).
- Clients never set `status`, `dataCadastro`, `ativo`, `possuiSelo`/`dataSelo` (except via the selo endpoint) or `dataReserva`/`dataRetirada` directly.

### 3.7 Repository structure (target)
```
rede-prato-java/
├── CLAUDE.md  README.md  PROGRESS.md  DECISIONS.md  BLOCKED.md  prompts.md
├── Dockerfile  .dockerignore  .gitignore  pom.xml  mvnw  mvnw.cmd  .mvn/
├── db/README.md                      (Mariana's ddl.sql arrives later — never edit it)
├── docs/  PLAN.md decisoes.md classes.md backlog.md casos-de-uso.md prototipo.md gs-ficha.md prototipo/*.png
│         endpoints.md  class-diagram.md  der.md  run.md  oracle-checklist.md   (feature 17)
├── specs/01-setup/ … specs/17-docs/  (requirements.md, design.md, tasks.md each)
└── src/
    ├── main/java/br/com/fiap/redeprato/
    │   ├── RedePratoApplication.java
    │   ├── entity/ (+ enums/)  dao/  bo/  controller/  dto/  exception/  converter/  config/
    └── main/resources/
    │   ├── application.properties  application-oracle.properties
    │   └── static/ index.html lotes.html registrar.html retiradas.html painel.html
    │              css/app.css  js/api.js js/persona.js js/<page>.js  img/logo.png img/logo_light.png
    └── test/java/br/com/fiap/redeprato/ bo/ exception/ converter/ controller/ RedePratoApplicationTests.java
```

---

## 4. Phase order and priorities

| Phase | Features | Priority |
| --- | --- | --- |
| 0 | Initial commit of docs + plan on `main` | MVP-critical |
| 1 – Setup | 01 | MVP-critical (unlocks the 30-point deploy) |
| 2 – Backend | 02 → 10 | MVP-critical (the end-to-end flow + full CRUD) |
| 2b – Seed | 11 | MVP-critical (demo data for the deploy and frontend) |
| 3 – Frontend | 12 → 16 | MVP-critical: 13, 14, 15, 16 core flows; 12 landing (prototype screen) |
| 4 – Deliverables | 17 | MVP-critical (PDF needs class diagram, endpoints, DER, run instructions) |
| 5 – Extras | 18 | Nice-to-have, only after 01–17 are merged |

Inside each feature, items marked *(nice)* are optional: do them only if the feature is otherwise done and green.

If time is short, the order of sacrifice is: 18 → *(nice)* items → landing-page polish (12) → painel weekly chart (16). Never sacrifice: tests, exception layer, Dockerfile/health, full CRUD on all 9 entities, UC12 rules.

---

## 5. Feature list (overview)

| NN | Branch | HU | Main endpoints | Key business rules | Tests expected (minimum) |
| --- | --- | --- | --- | --- | --- |
| 01 | `feature/01-setup` | — | `GET /api/health` | profiles, converters, error JSON | converters, exception handler mappings, context load, health |
| 02 | `feature/02-categorias-enderecos` | HU19, (HU01) | CRUD `/api/categorias`, `/api/enderecos` | unique nome; CEP 8 digits; UF valid; delete only if unused | per rule + CRUD happy paths |
| 03 | `feature/03-ofertantes-selo` | HU01, HU20, (HU18) | CRUD `/api/ofertantes`, `PUT/DELETE /api/ofertantes/{id}/selo` | logical delete; selo ⇔ data; phone required | per rule |
| 04 | `feature/04-lotes` | HU02, HU03, HU09, HU21, (HU05 hook) | CRUD `/api/lotes`, `GET /api/lotes/disponiveis`, `POST /api/lotes/expirar` | modalidade×preço; peso>0; prazo; edit/delete only DISPONIVEL; delete cascade rules; expiry | per rule + transitions |
| 05 | `feature/05-beneficiarios` | HU07, (HU18) | CRUD `/api/beneficiarios` | no document; phone/address optional; qtd>0; logical delete | per rule |
| 06 | `feature/06-pontos` | HU17, (HU16 data) | CRUD `/api/pontos` | HH:MM; abertura<fechamento; logical delete | per rule |
| 07 | `feature/07-reservas` | HU10, HU12, (HU11) | CRUD `/api/reservas`, `PUT /api/reservas/{id}/cancelar` | UC12 rules, one ATIVA per lote, same transaction | every UC12 criterion |
| 08 | `feature/08-retirada-destinacao` | HU13, HU14 | `PUT /api/reservas/{id}/retirada`, CRUD `/api/destinacoes` | ATIVA→RETIRADA; decision 4; sum ≤ peso | per rule |
| 09 | `feature/09-alertas` | HU04, HU05 | `POST /api/alertas/xepa`, CRUD `/api/alertas`, `GET /api/alertas/recentes` | UC04 rules; NOVO_LOTE on lote creation | every UC04 criterion |
| 10 | `feature/10-impacto` | HU15, (HU22 basis) | `GET /api/impacto/ofertantes/{id}`, `GET /api/impacto/rede` | kg only from Destinacao; weekly buckets | aggregation math |
| 11 | `feature/11-seed` | — | — | simulated data, relative dates, H2 only | seed loads; counts |
| 12 | `feature/12-frontend-base-landing` | — | `/` | shared CSS/JS, "Você é:" selector, landing | static page served |
| 13 | `feature/13-frontend-lotes` | HU05, HU09, HU10, HU12 | `/lotes.html` | — | static page served |
| 14 | `feature/14-frontend-registrar` | HU02, HU03, HU04 | `/registrar.html` | — | static page served |
| 15 | `feature/15-frontend-retiradas` | HU13, HU14 | `/retiradas.html` | — | static page served |
| 16 | `feature/16-frontend-painel` | HU15, HU20 | `/painel.html` | — | static page served |
| 17 | `feature/17-docs` | — | — | endpoint table, Mermaid class diagram + DER, run guide, Oracle checklist | doc ↔ code consistency check |
| 18 | `feature/18-extras` *(nice)* | HU16, HU22, HU24 | OpenAPI UI, `/api/relatorios/ofertantes/{id}/mensal`, `precisaTransporte` filter | — | per addition |

---

## 6. Feature details

### Step 0 — initial commit (on `main`, the only direct commit)
1. Create `.gitignore` (3.4), `PROGRESS.md` (all features `todo`), `DECISIONS.md` (with D-1 from 3.1), `BLOCKED.md` (empty header), `prompts.md` (header + the starting prompt Thomas typed + "Plan: docs/PLAN.md").
2. `git add` the docs, db, plan, CLAUDE.md and these files (check `git status` that `.claude/settings.local.json` and `.DS_Store` are NOT staged). Commit `chore: add project docs, plan and logs`. `git push origin main`.

### 01 — Setup (`feature/01-setup`)
Goal: a deployable skeleton, early (Render builds it as soon as it reaches `main`).
- Maven project (3.4), wrapper, `RedePratoApplication` (sets default TimeZone `America/Sao_Paulo`, `@EnableScheduling`).
- `application.properties` + `application-oracle.properties` (3.3). `config/ClockConfig`, `config/WebConfig` (CORS).
- `converter/BooleanSNConverter` (`true↔"S"`, `false↔"N"`, `null↔null`; unknown DB value → `IllegalArgumentException`), `converter/LocalTimeHHMMConverter` (`"HH:mm"`; null-safe; invalid → `IllegalArgumentException`).
- Exception layer complete (3.5).
- `controller/HealthController`: `GET /api/health` → `200 {"status":"UP","app":"Rede de Prato","profile":"default|oracle","time":"..."}`.
- `Dockerfile`, `.dockerignore`.
- `README.md` v1: **Ficha da CitizenTech at the very top** (copy from `docs/gs-ficha.md`, deploy link `https://rede-prato-java.onrender.com`), then team, tech stack, layer table (3.1), how to run (`./mvnw spring-boot:run`, `SPRING_PROFILES_ACTIVE=oracle` with the 3 env vars), link to `docs/endpoints.md` (to be written in 17).
- Tests: both converters (all branches), handler mappings (each row of 3.5), context-load test, health endpoint test (MockMvc).
- Merge + push **as early as possible** — Render's first green deploy depends on it.

### 02 — Categorias & Endereços (`feature/02-categorias-enderecos`) · HU19
Entities `CategoriaAlimento`, `Endereco`; DAOs; BOs; controllers.

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/categorias` | list ordered by nome |
| GET | `/api/categorias/{id}` | 404 if missing |
| POST | `/api/categorias` | `{nome, descricao?}` → 201 |
| PUT | `/api/categorias/{id}` | → 200 |
| DELETE | `/api/categorias/{id}` | 204; 409 if any lote uses it |
| GET | `/api/enderecos` (`?bairro=`) | list |
| GET/POST/PUT/DELETE | `/api/enderecos[/{id}]` | DELETE 409 if used by ofertante, ponto or beneficiário |

Rules: BR-02.1 categoria `nome` required, trimmed, ≤ 50 chars; BR-02.2 `nome` unique ignoring case (409 `"Já existe uma categoria com esse nome."`); BR-02.3 `descricao` ≤ 200; BR-02.4 categoria in use cannot be deleted (409); BR-02.5 endereço required fields `logradouro, bairro, cidade, uf, cep`; BR-02.6 `cep` exactly 8 digits (strip `-` and spaces before validating); BR-02.7 `uf` 2 letters, upper-cased, one of the 27 Brazilian UFs; BR-02.8 lengths per `docs/classes.md`; BR-02.9 endereço in use cannot be deleted (409). (The DAO methods needed for 2.4/2.9 — e.g. `existsByCategoria_IdCategoria` — may be added in later features; until those entities exist, implement the check when the dependent entity arrives and note it in the spec's tasks.)

### 03 — Ofertantes & Selo (`feature/03-ofertantes-selo`) · HU01, HU20
| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/ofertantes` (`?tipo=&ativo=&bairro=`) | default `ativo=true` |
| GET | `/api/ofertantes/{id}` | includes endereço fields flat + `possuiSelo`, `dataSelo` |
| POST | `/api/ofertantes` | `{nome, tipo, telefone, email?, idEndereco}` → 201, `ativo=true`, `possuiSelo=false` |
| PUT | `/api/ofertantes/{id}` | updates nome, tipo, telefone, email, idEndereco |
| DELETE | `/api/ofertantes/{id}` | **logical**: `ativo=false` → 204 (idempotent) |
| PUT | `/api/ofertantes/{id}/selo` | grants selo: `possuiSelo=true`, `dataSelo=today` → 200; 409 if already has it; 400 if inactive |
| DELETE | `/api/ofertantes/{id}/selo` | revokes: `possuiSelo=false`, `dataSelo=null` → 204 |

Rules: BR-03.1 nome required ≤ 100; BR-03.2 tipo required (SUPERMERCADO/FEIRANTE); BR-03.3 telefone required, 10–11 digits after stripping non-digits; BR-03.4 email optional, valid format, ≤ 100; BR-03.5 endereço must exist (404); BR-03.6 selo consistency: `possuiSelo` ⇔ `dataSelo != null` (enforced in BO for every save — mirrors `CK_RP_OFERTANTE_SELO`); BR-03.7 no physical delete; BR-03.8 inactive ofertante cannot receive the selo; BR-03.9 PUT on an inactive ofertante is allowed (to reactivate use `PUT` with `"ativo": true` — record as decision).

### 04 — Lotes (`feature/04-lotes`) · HU02, HU03, HU09, HU21
| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/lotes` (`?status=&idOfertante=&idCategoria=&modalidade=`) | all lots, newest first |
| GET | `/api/lotes/disponiveis` (`?idCategoria=&modalidade=&bairro=`) | **only** `DISPONIVEL`, `dataLimiteRetirada > now`, ofertante ativo; ordered by `dataLimiteRetirada` asc; runs expiry first |
| GET | `/api/lotes/{id}` | flat DTO: lote fields + `nomeOfertante`, `tipoOfertante`, endereço do ofertante (logradouro, número, bairro), `nomeCategoria` |
| POST | `/api/lotes` | `{idOfertante, idCategoria, descricao, pesoKg, condicao, modalidade, preco?, valorEstimado?, dataLimiteRetirada, urlFoto?, precisaTransporte?}` → 201; status `DISPONIVEL`; `dataCadastro = now`; **creates one Alerta NOVO_LOTE/APP** (wire in 09 if Alerta does not exist yet — note it in tasks) |
| PUT | `/api/lotes/{id}` | same fields except idOfertante; only when `DISPONIVEL` (else 409 `"Só é possível alterar um lote disponível."`) |
| DELETE | `/api/lotes/{id}` | decision 6: 409 if any reserva ATIVA/RETIRADA or any destinação; otherwise delete its CANCELADA reservas and its alertas, then the lote → 204 (implement the cascade when 07/09 exist; spec it now) |
| POST | `/api/lotes/expirar` | runs expiry now, returns `{"expirados": n}` (admin/demo) |

Rules: BR-04.1 descricao required ≤ 200; BR-04.2 pesoKg > 0, max 2 decimals, ≤ 999999.99; BR-04.3 DOACAO ⇒ preco must be null (400 `"Doação não tem preço."`); BR-04.4 VENDA ⇒ preco required and > 0; BR-04.5 valorEstimado optional, ≥ 0; BR-04.6 on create, `dataLimiteRetirada` must be in the future (> now); on update ≥ dataCadastro and > now; BR-04.7 ofertante must exist and be ativo (400 `"Ofertante inativo não pode registrar lotes."`); BR-04.8 categoria must exist; BR-04.9 status never set by the client; BR-04.10 update only in DISPONIVEL; BR-04.11 delete rules (decision 6); BR-04.12 urlFoto ≤ 255, must start with `http://` or `https://` if present; BR-04.13 **expiry**: DISPONIVEL or RESERVADO with `dataLimiteRetirada < now` → EXPIRADO; when a RESERVADO lote expires, set its ATIVA reserva to CANCELADA in the same transaction (record as a decision; reason: the lote can no longer be picked up). Expiry runs: `@Scheduled(fixedDelay = 5 min)`, at the start of `GET /disponiveis`, and before creating a reserva.
Status transitions exactly as `docs/classes.md`; write a test per transition.

### 05 — Beneficiários (`feature/05-beneficiarios`) · HU07
CRUD `/api/beneficiarios` (`?tipo=&ativo=`), DELETE logical.
Request `{nome, tipo, qtdPessoas?, telefone?, idEndereco?}`.
Rules: BR-05.1 nome required ≤ 100; BR-05.2 tipo required; BR-05.3 qtdPessoas optional, 1–9999; tipo PESSOA ⇒ qtdPessoas = 1 (set it if null, 400 if another value); BR-05.4 telefone optional, 10–11 digits if present; BR-05.5 endereço optional, must exist if given; BR-05.6 **no document fields exist** (do not add CPF/RG); BR-05.7 logical delete only.

### 06 — Pontos de distribuição (`feature/06-pontos`) · HU17
CRUD `/api/pontos` (`?ativo=&bairro=`), DELETE logical. Request `{nome, tipo, diasFuncionamento, horarioAbertura:"16:00", horarioFechamento:"18:00", telefone?, idEndereco}`. Response includes endereço flat fields.
Rules: BR-06.1 nome required ≤ 100; BR-06.2 tipo required; BR-06.3 diasFuncionamento required ≤ 50; BR-06.4 horários required, format `HH:MM` 00:00–23:59 (400 `"Horário deve estar no formato HH:MM."`); BR-06.5 abertura < fechamento; BR-06.6 endereço required and must exist; BR-06.7 logical delete only.

### 07 — Reservas (`feature/07-reservas`) · HU10, HU12 (UC12)
| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/reservas` (`?idBeneficiario=&idOfertante=&status=&idLote=`) | newest first |
| GET | `/api/reservas/{id}` | **confirmation DTO**: `idReserva` (= número de retirada), status, lote (descrição, pesoKg, modalidade, preco, valorEstimado, dataLimiteRetirada), ofertante (nome, endereço), ponto (nome, endereço, dias, horários) or `localRetirada:"OFERTANTE"`, beneficiário (nome, tipo) |
| POST | `/api/reservas` | `{idLote, idBeneficiario, idPonto?}` → 201 with the confirmation DTO |
| PUT | `/api/reservas/{id}` | only while ATIVA: change `idPonto` (null = ofertante) → 200 |
| PUT | `/api/reservas/{id}/cancelar` | ATIVA → CANCELADA; lote RESERVADO → DISPONIVEL (or EXPIRADO if past the deadline) → 200 |
| DELETE | `/api/reservas/{id}` | only CANCELADA → 204; otherwise 409 `"Só é possível excluir reservas canceladas. Cancele a reserva primeiro."` |

Rules (one test each — they are the UC12 acceptance criteria): BR-07.1 beneficiário must exist and be ativo; BR-07.2 lote must exist (404); BR-07.3 run expiry first; lote past deadline → 400 `"O prazo de retirada deste lote já passou."`; BR-07.4 lote must be DISPONIVEL, else 409 `"Este lote já foi reservado por outra pessoa."`; BR-07.5 only one ATIVA reserva per lote (BO check via DAO + DB unique index on Oracle → 409); BR-07.6 ponto optional; if given, must exist and be ativo; BR-07.7 reserva takes the **whole** lote (no partial kg field); BR-07.8 create reserva + lote→RESERVADO in **one `@Transactional`** method (test that both are saved; test that if the lote save fails nothing is committed, at least by verifying call order/rollback annotation); BR-07.9 cancel only ATIVA (409 otherwise); BR-07.10 status/dates never set by the client; `dataReserva = now`; BR-07.11 VENDA lots: response shows `preco` (HU11, pagamento na retirada — no payment).

### 08 — Retirada & Destinação (`feature/08-retirada-destinacao`) · HU13, HU14
- `PUT /api/reservas/{id}/retirada` → reserva ATIVA → RETIRADA, `dataRetirada = now`, lote → RETIRADO (one transaction) → 200. Rules: BR-08.1 only ATIVA (409); BR-08.2 lote must be RESERVADO (409 otherwise).
- CRUD `/api/destinacoes` (`GET ?idLote=&tipo=`). POST `{idLote, tipo, pesoKg, dataDestinacao?}` (default now). PUT same fields. DELETE always allowed → 204.
- Rules (decision 4): BR-08.3 lote RETIRADO ⇒ tipo CONSUMO or COMPOSTAGEM; BR-08.4 lote EXPIRADO ⇒ only COMPOSTAGEM (400 `"Lote vencido só pode ir para compostagem."`); BR-08.5 lote DISPONIVEL/RESERVADO ⇒ 400 `"Registre a retirada antes do destino."`; BR-08.6 pesoKg > 0; BR-08.7 sum of destinações of the lote (excluding the one being updated) + new pesoKg ≤ lote.pesoKg (400 `"A soma dos destinos passa do peso do lote (X kg)."`); BR-08.8 dataDestinacao not in the future.
- *(nice)* `POST /api/lotes/{id}/destinacoes` batch `{consumoKg, compostagemKg}` creating up to two rows in one transaction (the Retiradas screen uses it if present; otherwise it calls POST twice).

### 09 — Alertas (`feature/09-alertas`) · HU04, HU05 (UC04)
- `POST /api/alertas/xepa` `{idOfertante, idsLotes?: [..], novoPrazo?: "2026-11-17T14:00:00", canal?: "APP"}` → 201 `{"quantidade": n, "alertas": [...], "mensagem": "Xepa avisada para n lotes."}`.
  Rules: BR-09.1 ofertante exists, ativo and **FEIRANTE** (400 `"Só feirantes podem avisar xepa."` — UC04 actor; record as decision); BR-09.2 if `idsLotes` empty/absent → all DISPONIVEL lots of the ofertante; if the resulting set is empty → 400 `"Selecione pelo menos um lote disponível."`; BR-09.3 every listed lote must belong to the ofertante and be DISPONIVEL (400 naming the lote); BR-09.4 `novoPrazo` optional, must be in the future; it updates `dataLimiteRetirada` of those lots; BR-09.5 one Alerta per lote, tipo XEPA, canal default APP, `dataEnvio = now`; BR-09.6 no recipient is stored (decision 8); no "pending" status (decisão 1); BR-09.7 all in one transaction.
- Lote creation (04) creates one NOVO_LOTE/APP alert (HU05) — wire it here if 04 left a TODO, with a test in `LoteBOTest`.
- CRUD `/api/alertas` (`GET ?tipo=&idLote=`), POST `{idLote, tipo, canal}` (lote must exist), PUT `{tipo, canal}`, DELETE always → 204.
- `GET /api/alertas/recentes?horas=24` → alerts of lots still DISPONIVEL, newest first, flat DTO with `descricaoLote`, `nomeOfertante`, `bairro`, `dataLimiteRetirada`, `tipo` (feeds the xepa banner, HU05).
- WhatsApp is simulated: canal WHATSAPP is just stored.

### 10 — Impacto (`feature/10-impacto`) · HU15
- `GET /api/impacto/ofertantes/{id}?mes=2026-11` (default current month) →
  ```json
  { "idOfertante":1, "nomeOfertante":"Banca do Seu Antônio", "mes":"2026-11",
    "totalKg":186.0, "consumoKg":152.0, "compostagemKg":34.0, "percentualConsumo":81.7,
    "lotesRetirados":41, "beneficiariosAtendidos":27, "pessoasAtendidas":95,
    "porSemana":[{"inicio":"2026-10-27","fim":"2026-11-02","kg":38.0}, ... 4 items, last = current week],
    "possuiSelo":true, "dataSelo":"2026-11-10" }
  ```
- `GET /api/impacto/rede?mes=` → same totals for the whole network + `ofertantes: [{id, nome, totalKg}]` ranked desc.
- Rules: BR-10.1 kg come **only** from `T_RP_DESTINACAO.peso_kg` (never typed by hand) joined to the lote's ofertante, filtered by `data_destinacao` in the month; BR-10.2 lotesRetirados = lots of the ofertante with a reserva RETIRADA whose `dataRetirada` is in the month; BR-10.3 beneficiariosAtendidos = distinct beneficiários of those reservas; pessoasAtendidas = sum of their `qtdPessoas` (null counts as 1); BR-10.4 percentualConsumo = consumo/total×100, 1 decimal, 0 when total = 0; BR-10.5 porSemana = last 4 Monday–Sunday weeks ending in the current week (relative to the Clock), zero-filled; BR-10.6 ofertante must exist (404). Implement aggregates with JPQL in the DAOs (`SUM`, `COUNT DISTINCT`, `GROUP BY`) and the arithmetic in the BO; unit-test the arithmetic with mocked DAO results.
- Refeições estimadas: **not implemented** (grams per meal undefined in the docs) — record as decision.

### 11 — Seed data (`feature/11-seed`)
`config/DataSeeder` (`CommandLineRunner`, `@ConditionalOnProperty(app.seed.enabled=true)`, never on `oracle`), inserting **through the DAOs** with dates **relative to the Clock** so the demo always looks "today":
- 6 categorias: Frutas, Verduras, Legumes, Padaria, Laticínios, Outros.
- 6+ endereços in Paraíso / Vila Mariana (São Paulo, SP, CEPs `04001-000`–`04110-000` range, fictional numbers).
- 5 ofertantes: Banca do Seu Antônio (FEIRANTE, selo since 10 days ago), Banca da Dona Lúcia (FEIRANTE), Hortifrúti do Paraíso (SUPERMERCADO, selo), Mercado Bom Preço (SUPERMERCADO), Sacolão Vila Mariana (SUPERMERCADO, inactive — shows logical delete).
- 6 beneficiários: Rosana (FAMILIA, 4), Seu Jorge (PESSOA, 1), Família Souza (FAMILIA, 5), Cozinha Solidária do Bairro (INSTITUICAO, 60), Lanchonete da Esquina (PEQUENO_NEGOCIO, 3), Maria (PESSOA, 1, no phone, no address).
- 3 pontos: Ponto Paraíso (ASSOCIACAO, "TER e QUI", 16:00–18:00), Cozinha Solidária do Bairro (COZINHA_SOLIDARIA, "SEG a SEX", 11:00–14:00), ONG Prato Cheio (ONG, "SAB", 09:00–12:00).
- ~20 lotes covering every status: 8 DISPONIVEL (deadlines today 14h/18h and tomorrow 10h/12h — if "today 14h" is already past at startup, use now+2h), 3 RESERVADO with reservas ATIVA, 6 RETIRADO with reservas RETIRADA and destinações (mix CONSUMO/COMPOSTAGEM) spread over the last 4 weeks so the painel chart has data, 2 EXPIRADO (one with a COMPOSTAGEM destinação), 1 CANCELADA reserva history. Descriptions like the prototype: "Bananas", "Tomates", "Alface", "Cenouras", "Maçãs", "Laranjas", "Pão francês do dia", "Iogurte perto do vencimento".
- Alertas: NOVO_LOTE for each lote; 2 XEPA (Banca do Seu Antônio, today).
- Test: context loads with seed; counts per table ≥ 5 (except where the domain has fewer — document).

### 12 — Frontend base + landing (`feature/12-frontend-base-landing`)
Read `docs/prototipo.md` fully and **look at `docs/prototipo/0-landing.png`** (Read tool shows images) before coding.
- `static/css/app.css`: design tokens as CSS variables (prototipo.md §1), fonts (Google Fonts `<link>` + fallbacks `Arial, sans-serif` / `cursive`), components: `.btn`, `.btn-dark`, `.btn-line`, `.chip[aria-pressed]`, `.card`, `.tile`, `.step`, `.num`, `.badge-*`, `.modal`, header/nav, focus ring, responsive grid, `prefers-reduced-motion`.
- `static/js/api.js`: `api.get/post/put/del(path, body)` with `fetch`, JSON, throws `{status, message, fields}` from the `ApiError` body; helpers `fmtKg`, `fmtBRL` (`Intl.NumberFormat('pt-BR')`), `fmtPrazo` ("até hoje, 18h" / "até amanhã, 10h" / "até 20/11, 10h"), `illustrationFor(categoria)` returning inline SVG (tomate, alface, cenoura, banana, maçã, laranja, caixa).
- `static/js/persona.js`: the **"Você é:"** selector (decision 5) rendered in the header: loads `/api/beneficiarios?ativo=true` or `/api/ofertantes?ativo=true`, stores the choice in `localStorage` (wrapped in try/catch) and `?beneficiario=`/`?ofertante=` URL param; shows "Olá, **{nome}**" + avatar initial.
- `static/index.html` = landing (prototipo.md §3.1), "Procure esta placa" card filled from `GET /api/pontos` (first active), images `img/logo.png` / `img/logo_light.png` copied from `docs/prototipo/`.
- Test: MockMvc `GET /` and `/index.html` return 200 HTML containing "Rede de Prato".

### 13 — Lotes disponíveis + reserva (`feature/13-frontend-lotes`) · HU05, HU09, HU10, HU12
Look at `docs/prototipo/1-lotes.png`. Implement prototipo.md §3.2 against: `GET /api/alertas/recentes`, `GET /api/categorias`, `GET /api/lotes/disponiveis`, `GET /api/pontos`, `POST /api/reservas`, `GET /api/reservas?idBeneficiario=&status=ATIVA`, `PUT /api/reservas/{id}/cancelar`. Show API error messages in the modal (409/400). Test: page served.

### 14 — Registrar lote + xepa (`feature/14-frontend-registrar`) · HU02, HU03, HU04
Look at `docs/prototipo/2-registrar.png`. Implement prototipo.md §3.3 against `POST /api/alertas/xepa`, `DELETE /api/alertas/{id}` ("Desfazer"), `GET /api/categorias`, `POST /api/lotes`, `GET /api/lotes?idOfertante=`, `PUT /api/lotes/{id}`, `DELETE /api/lotes/{id}`. The xepa block is shown only when the selected ofertante is FEIRANTE (supermarkets see only the form). Test: page served.

### 15 — Retiradas + destino (`feature/15-frontend-retiradas`) · HU13, HU14
Look at `docs/prototipo/3-retiradas.png`. Implement prototipo.md §3.4 against `GET /api/reservas?idOfertante=`, `GET /api/reservas/{id}` ("Conferir"), `PUT /api/reservas/{id}/retirada`, `POST /api/destinacoes` (or the batch endpoint), `GET /api/destinacoes?idLote=`, `GET /api/lotes?idOfertante=&status=EXPIRADO`. Test: page served.

### 16 — Painel de impacto + selo (`feature/16-frontend-painel`) · HU15, HU20
Look at `docs/prototipo/4-painel.png`. Implement prototipo.md §3.5 against `GET /api/impacto/ofertantes/{id}`, `GET /api/impacto/rede`, `PUT /api/ofertantes/{id}/selo`. Weekly chart = CSS bars sized from `porSemana`. "Baixar o selo" downloads a generated SVG (Blob). Test: page served.

### 17 — Deliverables docs (`feature/17-docs`)
- `docs/endpoints.md`: one table with **every** endpoint (method, path, description in Portuguese, request example, success status, error statuses). Generate it from the controllers (read them; do not invent). Link it from the README.
- `docs/class-diagram.md`: Mermaid `classDiagram` of the 9 entities + 10 enums with fields/types exactly as in the code, associations with multiplicities; a second diagram of the layers (Controller → BO → DAO → Entity, plus exception classes). Must match the code.
- `docs/der.md`: Mermaid `erDiagram` of the 9 tables with columns, PK/FK and cardinalities from `docs/classes.md` (note that Mariana's Oracle Data Modeler diagram is the official DER).
- `docs/run.md` (run instructions for the PDF): tools and versions (JDK 21, Maven wrapper, Spring Boot version from the pom, IntelliJ IDEA, Oracle Database FIAP), GitHub link, deploy link, step by step: clone → `./mvnw test` → `./mvnw spring-boot:run` → open `http://localhost:8080`; Oracle profile with the three env vars; Render settings.
- `docs/oracle-checklist.md`: steps for Thomas when the DDL arrives (put it in `db/ddl.sql`, run it in the schema, set `SPRING_PROFILES_ACTIVE=oracle` locally, fix `validate` errors, then set it on Render), and a table "entity field → expected column/type" generated from the entities.
- README final: Ficha, links, layer table, endpoint summary, screens list, how to run, team, AI-use note (`prompts.md`).
- Test: none new; run the full suite.

### 18 — Extras *(nice, only if 01–17 are merged)* (`feature/18-extras`)
In this order, each optional: (a) springdoc-openapi UI at `/swagger-ui.html` if a Boot-4-compatible version resolves (otherwise skip, log); (b) `GET /api/relatorios/ofertantes/{id}/mensal` (HU22: lotes doados no mês com data, alimento, peso, valor estimado, destino); (c) `GET /api/lotes?precisaTransporte=true` (HU24/HU25); (d) "Onde buscar" list of pontos with horários on the landing (HU16, no map); (e) more MockMvc controller tests.

---

## 7. Definition of done for the night
1. Features 01–11 merged and pushed (backend complete, seed, deploy-ready). 
2. Features 12–16 merged (5 screens working against the API).
3. Feature 17 merged (docs for the PDF).
4. `PROGRESS.md` says exactly what is done, what is blocked and the next step; `DECISIONS.md` and `BLOCKED.md` are up to date; `./mvnw test` green on `main`; `main` pushed.

When everything above is done, do feature 18. When that is done too, stop and write a final summary at the top of `PROGRESS.md` (what was built, test count, open issues, what Thomas must do in the morning).

## 8. Things Thomas will do (do not attempt)
- Render dashboard (service exists: `rede-prato-java`, Docker, branch `main`, health `/api/health`, env vars set; `SPRING_PROFILES_ACTIVE` intentionally **not** set until Oracle is ready).
- Mariana's DDL into `db/ddl.sql`, running it on Oracle, switching the profile.
- The PDF and the ZIP submission.
