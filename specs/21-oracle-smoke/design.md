# 21 — Oracle smoke test · Design

## Tool: `tools/smoke/OracleSmokeTest.java`
- Single-file Java 21 program, launched with `java tools/smoke/OracleSmokeTest.java [baseUrl]`. It uses only the JDK (`java.net.http.HttpClient`), lives outside `src/` and is not part of the Maven build.
- **Helpers:**
  - `call(method, path, body)` returns status and body. Every call records `METHOD + normalised path` (numeric segments become `{id}`, query removed) for the coverage check.
  - `expect(label, method, path, body, status)` prints a PASS/FAIL line. When the expected status is ≥ 400, it also checks that the body has `status`, `message` and `path`.
  - `assertThat(ok, method, path, expected, actual, reason)` prints extra assertion lines on fields.
  - JSON is read with regex (`str`, `num`, `raw`) plus a small splitter for top-level array objects.
- **Order:**
  1. health (abort unless `oracle`);
  2. seeded data;
  3. identity and the endereço/categoria CRUD;
  4. lote rules;
  5. reserva flow;
  6. destinação;
  7. xepa and alert CRUD;
  8. selo;
  9. logical delete and reactivation;
  10. physical delete;
  11. expiry, impacto and relatório;
  12. 404s and malformed JSON;
  13. cleanup of what can be removed;
  14. coverage against the `| n | \`METHOD\` | \`/path\` |` rows of `docs/endpoints.md`.
- **Test data:**
  - `[TESTE] <label> <run id>`, where the run id is a timestamp, so names are unique across runs. `T_RP_CATEGORIA_ALIMENTO.nome` is unique.
  - Deadlines are now + 3 days; the "past deadline" case is now − 1 day.
- **End state:**
  - Test ofertante, beneficiário and ponto are logically deleted again, so they don't show in the demo screens.
  - The extra test lote is deleted; the retirado test lote and its destinos stay, as history.

## Profiles (D-16)
| File | Content |
| --- | --- |
| `application.properties` | Common settings (port, encoding, time zone, `open-in-view`) + `spring.profiles.default=oracle` |
| `application-oracle.properties` | Unchanged: Oracle datasource from `ORACLE_*` env vars, `validate`, seed off, H2 console off |
| `application-dev.properties` | New: the H2 block moved out of `application.properties` (`create-drop`, seed on, H2 console) |
| `src/test/resources/config/application.properties` | Test classpath only: `spring.profiles.active=dev`. `classpath:/config/` overrides `classpath:/` |

Docs that said "H2 = default profile" are updated: README, `docs/run.md`, `docs/oracle-checklist.md`, CLAUDE.md quick facts.

## Docs
- `docs/run.md`: section "Teste com Oracle" (Portuguese) on how to run it and read the output.
- `PROGRESS.md`: result (passed/failed counts, date).
