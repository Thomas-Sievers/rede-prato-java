# 19 — Rename layers + frontend fixes · Design

## Packages and classes

| Before | After | Rubric layer |
| --- | --- | --- |
| `br.com.fiap.redeprato.dao.XxxDAO` | `br.com.fiap.redeprato.repository.XxxRepository` | DAO |
| `br.com.fiap.redeprato.bo.XxxBO` | `br.com.fiap.redeprato.service.XxxService` | BO |
| `br.com.fiap.redeprato.bo.Checks` | `br.com.fiap.redeprato.service.Checks` | BO helper |
| `test/.../bo/XxxBOTest` | `test/.../service/XxxServiceTest` | — |

- Files moved with `git mv` (history kept), then identifiers renamed in all Java sources: class names, imports, fields/parameters/locals (`loteBO` → `loteService`, `loteDAO` → `loteRepository`).
- Javadoc on every repository: `DAO layer: …`; on every service: `BO layer (business rules): …`.
- No change to entities, DTOs, controllers' URLs, messages or JSON.

## Docs
README layer table, CLAUDE.md quick facts, docs/PLAN.md §3.1 (+ other "current code" mentions), docs/class-diagram.md layer diagram and table, PROGRESS.md summary, DECISIONS.md D-14 (supersedes D-1).

## Frontend
- `css/app.css`: `.persona` gets `flex-wrap: wrap; min-width: 0; max-width: 100%`; the select may shrink (`min-width: 0; max-width: 100%`); under 640 px the block takes the full header width and the select fills the remaining row.
- `js/lotes.js`: each pickup option button gets `aria-label="<título>: <local>"`.
