# Progress
Last update: 2026-11-17 20:18 (America/Sao_Paulo) — feature 21 (Oracle smoke test; Oracle as default profile, D-16) merged and pushed 20:24 (Render has the `ORACLE_*` vars)

## Oracle smoke test result
**2026-11-17 20:17 — 149 checks, 149 passed, 0 failed** (`java tools/smoke/OracleSmokeTest.java` against the local app on the FIAP Oracle schema with Mariana's `ddl.sql` + `dml.sql`).
- All 58 endpoints were called.
- The seeded data matches the DML, and the identity counters start after the seeded ids. The `CHECK`s (`CK_RP_OFERTANTE_SELO_DATA`, lote preço/prazo) and the unique index for one ATIVA reserva per lote behave as expected.
- One app fix: `/api/health` reported "default" when Oracle was the default profile.
- No database mismatch, so nothing was added to `BLOCKED.md`.

## Final summary (night run)

**All 18 features are merged into `main` and pushed** (each via `feature/NN-*` → `--no-ff` merge, spec committed first). `./mvnw test`: **325 tests, 0 failures** (always H2 via the `dev` profile; the app itself runs on Oracle by default, D-16).

What was built:
- **API REST** (Spring Boot 4.1.1, Java 21): 9 entities mapped to `T_RP_*` with exact Oracle `columnDefinition`s, 10 enums, DAO layer (`repository`, `XxxRepository`), BO layer (`service`, `XxxService`, all business rules with Portuguese messages; D-14), 12 controllers / **58 endpoints** (GET/POST/PUT/DELETE), exception layer (`GlobalExceptionHandler` → `ApiError` JSON, ORA-00001/ORA-02292 → 409).
- Core flows: lote → alerta NOVO_LOTE; xepa (UC04); reserva of the whole lote with one ATIVA per lote (UC12); cancel; retirada; destinação (comida/adubo, sum ≤ peso); automatic expiry (job + on listing/reserving); impact panel and monthly report.
- **Seed** with simulated data relative to "today" (H2 profile only).
- **Frontend** (plain HTML/CSS/JS served by Spring): landing, Lotes + reserva, Registrar + xepa, Retiradas + destino, Painel + selo. Swagger UI at `/swagger-ui.html`. Redesigned in feature 20 (type scale as CSS variables, 3D landing art, WCAG 2.2 AA fixes A1–A9; D-15).
- **Docs for the PDF**: `docs/endpoints.md`, `docs/class-diagram.md` (Mermaid), `docs/der.md` (Mermaid), `docs/run.md`, `docs/oracle-checklist.md`, README with the Ficha on top; `specs/01…18` (requirements/design/tasks).

## What Thomas must do in the morning

1. **Check the Render deploy** (https://rede-prato-java.onrender.com, `/api/health`) — it should be building from `main`. If the Docker build fails, look at the Render log (it was only built locally with `./mvnw package`, not with `docker build`).
2. **Click through the 5 screens** (locally `./mvnw spring-boot:run` → http://localhost:8080, or on Render). The JS could not be run in a browser during the night (B-1): watch for console errors.
3. Mariana's scripts are in `db/` and passed the Oracle smoke test (feature 21). **Before pushing feature 21:** set `ORACLE_URL`, `ORACLE_USERNAME` and `ORACLE_PASSWORD` on Render. Since D-16 the app needs them to start; `SPRING_PROFILES_ACTIVE=dev` gives an H2-only demo instead.
4. Review `DECISIONS.md` (D-1…D-16; D-14 supersedes D-1) and `BLOCKED.md` (B-1, B-2).
5. Build the PDF from the docs above + screenshots of the screens; deadline 19/11 23:55 (no commits after it).

Open issues: frontend not browser-tested (B-1); Render deploy of feature 21 (Oracle by default) to be confirmed at `/api/health`; Spring Boot is 4.1.1 instead of 4.0.x (D-2).

| NN | Feature | Status | Tests | Notes |
| --- | --- | --- | --- | --- |
| 00 | Initial commit | merged | — | docs + plan + logs |
| 01 | Setup | merged | 33 | Boot 4.1.1, H2/Oracle profiles, converters, error layer, health, Dockerfile |
| 02 | Categorias & Endereços | merged | 69 | CRUD, unique nome, CEP/UF rules |
| 03 | Ofertantes & Selo | merged | 99 | logical delete, selo grant/revoke |
| 04 | Lotes | merged | 139 | CRUD, disponíveis, expiry job |
| 05 | Beneficiários | merged | 158 | no documents, optional phone/address |
| 06 | Pontos | merged | 184 | HH:MM schedule |
| 07 | Reservas | merged | 218 | UC12 rules, cancel, delete cascade |
| 08 | Retirada & Destinação | merged | 241 | retirada, destinos, batch endpoint |
| 09 | Alertas | merged | 269 | xepa (UC04), NOVO_LOTE, recentes |
| 10 | Impacto | merged | 280 | JPQL aggregates, weekly buckets |
| 11 | Seed | merged | 286 | simulated data relative to the clock |
| 12 | Frontend base + landing | merged | 292 | CSS tokens, api.js, persona.js, landing |
| 13 | Frontend lotes | merged | 295 | filters, reserva dialog, sacola/cancel |
| 14 | Frontend registrar | merged | 297 | xepa + desfazer, 7-step form, meus lotes |
| 15 | Frontend retiradas | merged | 299 | já buscou, destino panel, conferir número |
| 16 | Frontend painel | merged | 302 | impacto, weekly bars, selo, rede, admin |
| 17 | Docs | merged | 304 | endpoints, diagrams, run guide, Oracle checklist, README |
| 18 | Extras | merged | 310 | Swagger UI, monthly report, transport filter, "Onde buscar" |
| 19 | Rename layers + front fixes | merged | 310 | dao/bo → repository/service (D-14), persona selector at 375 px, aria-label on pickup radios |
| 20 | Redesign + WCAG 2.2 AA | merged | 321 | type scale vars, 3D landing (crate photo + gradient SVGs), A1–A9: pause button, focus ring, 320 px reflow, skip link, headings, chart/button names (D-15). Not yet checked in a browser |
| 21 | Oracle smoke test | merged | 325 | `tools/smoke/OracleSmokeTest.java`: 149/149 checks passed on Oracle. Oracle is the default profile, H2 → `dev` (D-16). Health reports the default profile. Mariana's scripts committed to `db/` |

(Test column = total suite size after the merge.)

## Next step
Nothing left in the plan (19–21 were extra requests from Thomas). Check that the Render deploy answers `"profile":"oracle"`; optionally run `java tools/smoke/OracleSmokeTest.java https://rede-prato-java.onrender.com`. Feature 20 needs a browser pass: 320 px width on index/registrar/painel, Tab through each page (skip link, focus ring), marquee pause button. Next session: react to Thomas's review / the Render deploy log / the Oracle DDL (see `docs/oracle-checklist.md`).
