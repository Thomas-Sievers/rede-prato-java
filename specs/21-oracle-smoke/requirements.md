# 21 — Oracle smoke test · Requirements

No new HU. Mariana's scripts (`db/ddl.sql`, `db/dml.sql`, `db/dql.sql`) were run on the FIAP Oracle schema. The app runs locally on the `oracle` profile. This feature checks the whole API against that real database.

## Acceptance criteria

1. **Given** `java tools/smoke/OracleSmokeTest.java [baseUrl]` (JDK only, default `http://localhost:8080`), **then** it prints one line per check (PASS/FAIL, method, path, expected vs actual, short reason), a summary, and exits 1 if any check failed.
2. **Given** `GET /api/health` does not report `"profile":"oracle"`, **then** the tool stops and does not test against H2.
3. **Given** a run, **then** each of the 58 endpoints in `docs/endpoints.md` is called at least once, and the tool reports any endpoint left uncovered.
4. **Given** Mariana's DML, **then** these exist:
   - 5 ofertantes, 5 categorias, 6 beneficiários and 5 pontos (records not prefixed `[TESTE]`);
   - lote 5 is `RESERVADO` with an `ATIVA` reserva.
5. **Identity:** creating an endereço, categoria, lote, reserva, destinação and alerta returns 201 with an id above the seeded ids (no ORA-00001 → 409).
6. **Lote:**
   - a new lote is `DISPONIVEL`, has a `NOVO_LOTE` alert and is listed in `/api/lotes/disponiveis`;
   - DOACAO with preço → 400; VENDA without preço → 400; a past deadline → 400;
   - PUT on a lote that is not `DISPONIVEL` → 409.
7. **Reserva:**
   - creating a reserva makes the lote `RESERVADO`; a second reserva on the same lote → 409;
   - PUT changes the ponto;
   - cancel → lote `DISPONIVEL`; reserving again works;
   - retirada → reserva `RETIRADA` with `dataRetirada`, lote `RETIRADO`;
   - cancelling a `RETIRADA` → 409;
   - an inactive beneficiário → 400.
8. **Destinação:**
   - consumo + compostagem via `/api/lotes/{id}/destinacoes` → 201;
   - going over the lote weight → 400;
   - PUT and DELETE work.
9. **Xepa:**
   - feirante 1 on a test lote → 201;
   - a SUPERMERCADO → 400;
   - `/api/alertas/recentes` lists the new alert.
10. **Selo on ofertante 3** (exercises `CK_RP_OFERTANTE_SELO_DATA`): grant → 200 with `dataSelo`; grant again → 409; revoke → 204 and `dataSelo` is null.
11. **Logical delete** of a test ofertante, beneficiário and ponto sets `ativo` false; PUT with `"ativo":true` makes them active again.
12. **Physical delete:**
    - an unused test categoria and endereço → 204;
    - an endereço in use → 409;
    - a categoria with lotes → 409.
13. **Expiry and reports:**
    - `POST /api/lotes/expirar` returns a count;
    - impacto (ofertante and rede) and the monthly relatório return 200 with numbers for 2026-11.
14. **Errors:**
    - unknown ids → 404 for each resource;
    - malformed JSON → 400;
    - every error body has `status`, `message` and `path`.
15. Every record the tool creates has a name or description starting with `[TESTE]`.

## Business rules
- BR-21.1 No change to `db/*.sql` or to any entity. A mismatch with Mariana's database goes to `BLOCKED.md`.
- BR-21.2 Only app bugs found by the smoke test are fixed in this feature, each with a unit test, and `./mvnw test` stays green.
- BR-21.3 The tool never reads or prints `ORACLE_*` variables and never starts or stops the app.
- BR-21.4 Seeded data the demo relies on is left as found: lote 5 and its reserva are only read, and the selo on ofertante 3 ends revoked, as in the DML.
