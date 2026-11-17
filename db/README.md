# db/ — Database scripts (Database Design, Mariana Souza França)

Mariana's DDL is the **source of truth** for tables, columns, types and constraints (see `docs/decisoes.md`). Do not change her scripts.

## Status

- [ ] `ddl.sql` — **NOT DELIVERED YET** (as of 17/11/2026, 01h). It arrives later and is dropped here unchanged.
- [ ] `dml.sql` — optional; seed data for Oracle.

## Until the DDL arrives (instructions for Claude Code)

- Build the entities from `docs/classes.md`, with exact `columnDefinition` types taken from the "Tipo Oracle" column (e.g. `CHAR(1)`, `VARCHAR2(5)`, `NUMBER(8,2)`), table names `T_RP_*`, column names in snake_case exactly as listed.
- Tests and the default profile run on H2 (`create-drop`), so nothing depends on this file.
- When the DDL arrives, Thomas (or a later Claude Code run) diffs it against the entities and fixes any mismatch so `ddl-auto=validate` passes on the `oracle` profile. Record every mismatch in `DECISIONS.md`.
