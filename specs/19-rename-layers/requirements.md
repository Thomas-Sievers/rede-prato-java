# 19 — Rename layers (repository/service) + frontend fixes · Requirements

No new HU. Decided by Thomas: packages follow the teacher's pattern (D-14, supersedes D-1). Two frontend fixes found in the browser review.

## Acceptance criteria

1. **Given** the source tree, **then** there is no `dao` or `bo` package: `br.com.fiap.redeprato.repository` holds `XxxRepository extends JpaRepository` and `br.com.fiap.redeprato.service` holds `XxxService` (`@Service`) and `Checks`; tests are `service/XxxServiceTest`.
2. **Given** any repository, **then** its Javadoc says it is the **DAO layer**; **given** any service, **then** its Javadoc says it is the **BO layer (business rules)**.
3. **Given** README, CLAUDE.md, docs/PLAN.md §3.1 and docs/class-diagram.md, **then** the layer table maps `repository` = DAO layer and `service` = BO layer, and no doc names the old packages/classes as current code.
4. **Given** a 375 px wide viewport, **when** a page with the "Você é:" selector loads, **then** the selector stays inside the header (it wraps and shrinks; no horizontal overflow).
5. **Given** the reservation dialog, **then** each pickup-place option (`role="radio"`) has an `aria-label` with its title and place (e.g. "No ponto da placa: Cozinha Solidária · Seg a Sex, 08:00 às 17:00").
6. **Given** `./mvnw test`, **then** all tests pass (≥ 310) — behaviour is unchanged.

## Business rules
- BR-19.1 Pure rename: no business rule, endpoint, message or JSON shape changes.
- BR-19.2 `docs/decisoes.md`, `docs/classes.md`, `docs/gs-ficha.md` (source documents) and the historical specs `specs/01…18` are not rewritten; "BO"/"DAO" there names the rubric layer, which still exists under the new package names (noted in D-14).
