# CLAUDE.md — Rede de Prato (FIAP GS 2026.2 · Domain Driven Design – Java)

You are building this whole repository **alone, overnight, with nobody to answer questions**. The full plan is in **`docs/PLAN.md`**: read it completely before doing anything, and re-read section 2 ("Operating rules") whenever you resume after a context compaction.

## Non-negotiable rules (short version — details in docs/PLAN.md §2)

1. **Never ask questions, never wait for input.** When something is unclear, pick the simplest option that fits the docs, write it in `DECISIONS.md` (decision + reason), and continue. If truly blocked, write it in `BLOCKED.md` and move to the next feature.
2. **Spec first.** No code for a feature before `specs/<NN>-<name>/requirements.md`, `design.md` and `tasks.md` exist and are committed on the feature branch. Behaviour changes → update the spec first.
3. **Git flow.** One branch per feature, `feature/<NN>-<short-name>`, from an up-to-date `main`. Small commits. Merge to `main` with `--no-ff` only when `./mvnw test` passes, then `git push origin main` and delete the local branch. Never push anything except `main`. Never force-push, never rewrite history.
4. **Tests.** Every feature ships JUnit 5 + Mockito unit tests on its BO (one test per business rule + happy path + edge cases). Tests run on H2 only, never Oracle.
5. **Secrets.** Never read, print or commit credentials. Oracle settings come only from `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD` env vars. Do not run `env`/`printenv`.
6. **Source of truth.** `docs/decisoes.md` (final decisions, do not reopen) > Mariana's DDL in `db/` (when present) > `docs/classes.md` > other docs. Never change files in `db/` except `db/README.md`.
7. **Language.** Code, comments, specs, commits and logs in English. All user-facing text (API error messages, HTML pages) in **Portuguese**.
8. **Deadline guard.** Never commit after **2026-11-19 22:00 (America/Sao_Paulo)**. Check `date` before each merge.
9. Update `PROGRESS.md` after every feature (and whenever you stop).

## Quick facts

- Java 21 · Spring Boot 4.x · Maven wrapper (`./mvnw`) · Lombok · Spring Data JPA · H2 (default) · Oracle (`oracle` profile).
- Base package: `br.com.fiap.redeprato`. Layers: `entity` (Model) · `dao` (DAO) · `bo` (BO) · `controller` · `dto` · `exception` · `converter` · `config`.
- Run tests: `./mvnw test`. Build: `./mvnw -B package`.
- Do not start long-running processes (`spring-boot:run`, `java -jar`) — verify with tests (`@SpringBootTest` + MockMvc) instead.
- Deploy: Render builds `./Dockerfile` on every push to `main` → https://rede-prato-java.onrender.com (health: `/api/health`).
