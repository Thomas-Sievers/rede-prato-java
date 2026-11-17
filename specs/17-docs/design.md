# 17 — Deliverable docs · Design

Files: `docs/endpoints.md`, `docs/class-diagram.md`, `docs/der.md`, `docs/run.md`, `docs/oracle-checklist.md`, `README.md`.

Consistency check (`src/test/java/.../DocsConsistencyTest`): scans the controller classes with reflection, builds "METHOD /path" for every `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` (class `@RequestMapping` prefix + method path) and asserts each appears in `docs/endpoints.md` as `` `METHOD` | `/path` ``. It also checks that every `@Entity` table name appears in `docs/der.md` and every entity class in `docs/class-diagram.md`.
