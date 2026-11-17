# 17 — Deliverable docs · Requirements

Goal: everything the PDF needs (rubric "Documentação", 30 pts) lives in the repo and matches the code.

## Acceptance criteria

1. `docs/endpoints.md` lists **every** endpoint of every controller (method, path, Portuguese description, request example, success status, error statuses) — 57 endpoints, checked against the `@*Mapping` annotations.
2. `docs/class-diagram.md` has a Mermaid `classDiagram` of the 9 entities + 10 enums with the fields and Java types exactly as in the code and the associations with multiplicities, plus a layer diagram (Controller → BO → DAO → Entity, exception classes).
3. `docs/der.md` has a Mermaid `erDiagram` of the 9 `T_RP_*` tables with columns, Oracle types, PK/FK and cardinalities, noting that Mariana's Oracle Data Modeler diagram is the official DER.
4. `docs/run.md` lists tools and versions, GitHub and deploy links, and the step-by-step to run (tests, H2, Oracle profile, Render).
5. `docs/oracle-checklist.md` lists the steps for when the DDL arrives and a table "entity field → column/type".
6. README final: Ficha at the top, links, layer table, endpoint summary, screens, how to run, team, AI-use note.
7. `DocsConsistencyTest` fails if a controller mapping is missing from `docs/endpoints.md`.
