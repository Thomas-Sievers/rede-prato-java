# Decisions

Append-only log of decisions taken while implementing `docs/PLAN.md` (final project decisions live in `docs/decisoes.md`).

## D-1 · 2026-11-17 · feature 00
**Decision:** Packages are named `dao` and `bo` (not `repository`/`service`).
**Why:** The graders look for the DAO and BO layers by name (rubric "Camadas DAO e BO"). Classes keep the teacher's style: Spring Data repositories (`XxxDAO extends JpaRepository`) and `@Service` classes with `validate()`.
**Alternatives considered:** `repository`/`service` (Spring convention) — rejected, harder for the graders to map to the rubric.
