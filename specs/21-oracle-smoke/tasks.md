# 21 — Oracle smoke test · Tasks

- [x] Spec
- [x] `tools/smoke/OracleSmokeTest.java` (checks 1–15 of requirements)
- [x] Oracle as default profile, H2 → `dev`, tests forced to `dev` (D-16); update docs
- [x] Run against the running app on Oracle: 149/149 passed. App fix: `/api/health` reports the default profile. No database mismatch (`BLOCKED.md` unchanged)
- [x] `./mvnw test` green (325)
- [x] `docs/run.md` "Teste com Oracle"; PROGRESS.md result; prompts.md
- [x] Merge `--no-ff` into main (local only, no push), delete branch
