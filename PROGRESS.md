# Progress
Last update: 2026-11-17 02:35 (America/Sao_Paulo)

## Final summary (night run)

**All 18 features are merged into `main` and pushed** (each via `feature/NN-*` → `--no-ff` merge, spec committed first). `./mvnw test`: **310 tests, 0 failures** (H2 only).

What was built:
- **API REST** (Spring Boot 4.1.1, Java 21): 9 entities mapped to `T_RP_*` with exact Oracle `columnDefinition`s, 10 enums, DAO (`dao`), BO (`bo`, all business rules with Portuguese messages), 12 controllers / **58 endpoints** (GET/POST/PUT/DELETE), exception layer (`GlobalExceptionHandler` → `ApiError` JSON, ORA-00001/ORA-02292 → 409).
- Core flows: lote → alerta NOVO_LOTE; xepa (UC04); reserva of the whole lote with one ATIVA per lote (UC12); cancel; retirada; destinação (comida/adubo, sum ≤ peso); automatic expiry (job + on listing/reserving); impact panel and monthly report.
- **Seed** with simulated data relative to "today" (H2 profile only).
- **Frontend** (plain HTML/CSS/JS served by Spring): landing, Lotes + reserva, Registrar + xepa, Retiradas + destino, Painel + selo. Swagger UI at `/swagger-ui.html`.
- **Docs for the PDF**: `docs/endpoints.md`, `docs/class-diagram.md` (Mermaid), `docs/der.md` (Mermaid), `docs/run.md`, `docs/oracle-checklist.md`, README with the Ficha on top; `specs/01…18` (requirements/design/tasks).

## What Thomas must do in the morning

1. **Check the Render deploy** (https://rede-prato-java.onrender.com, `/api/health`) — it should be building from `main`. If the Docker build fails, look at the Render log (it was only built locally with `./mvnw package`, not with `docker build`).
2. **Click through the 5 screens** (locally `./mvnw spring-boot:run` → http://localhost:8080, or on Render). The JS could not be run in a browser during the night (B-1): watch for console errors.
3. When Mariana's DDL arrives: follow `docs/oracle-checklist.md` (copy to `db/ddl.sql`, run it, `SPRING_PROFILES_ACTIVE=oracle`, fix any `validate` mismatch, then set the profile on Render).
4. Review `DECISIONS.md` (D-1…D-13) and `BLOCKED.md` (B-1, B-2).
5. Build the PDF from the docs above + screenshots of the screens; deadline 19/11 23:55 (no commits after it).

Open issues: frontend not browser-tested (B-1); Oracle profile never run against a real DDL (expected, DDL not delivered); Spring Boot is 4.1.1 instead of 4.0.x (D-2).

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

(Test column = total suite size after the merge.)

## Next step
Nothing left in the plan. Next session: react to Thomas's review / the Render deploy log / the Oracle DDL (see `docs/oracle-checklist.md`).
