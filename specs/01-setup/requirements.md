# 01 — Setup · Requirements

HU covered: none directly (technical base for every HU; unlocks the Render deploy).

## Acceptance criteria

1. **Given** the project is cloned, **when** `./mvnw -B test` runs, **then** the build succeeds using only H2 (no network, no Oracle).
2. **Given** the app is running with the default profile, **when** `GET /api/health` is called, **then** it returns `200` with JSON `{"status":"UP","app":"Rede de Prato","profile":"default","time":"..."}`.
3. **Given** the `oracle` profile is active, **when** the app starts, **then** the datasource is read only from `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`, `ddl-auto=validate` and seeding is off.
4. **Given** a boolean entity field, **when** it is persisted, **then** it is stored as `'S'`/`'N'` in a `CHAR(1)` column; an unknown DB value raises `IllegalArgumentException`.
5. **Given** a `LocalTime` entity field, **when** it is persisted, **then** it is stored as `"HH:mm"` in a `VARCHAR2(5)` column; invalid text raises `IllegalArgumentException`.
6. **Given** any error in a request, **when** the API answers, **then** the body has `timestamp, status, error, message, path, fields` with a Portuguese message, and the status follows the mapping table in `design.md`.
7. **Given** the repository, **when** Render builds `./Dockerfile`, **then** a single runnable jar is produced and the app listens on `$PORT`.
8. **Given** the README, **when** opened, **then** the Ficha da CitizenTech is at the very top.

## Business rules

- BR-01.1 `BooleanSNConverter`: `true→"S"`, `false→"N"`, `null→null`; `"S"→true`, `"N"→false`, `null→null`; anything else → `IllegalArgumentException`.
- BR-01.2 `LocalTimeHHMMConverter`: `LocalTime→"HH:mm"`; `"HH:mm"→LocalTime`; null-safe; invalid → `IllegalArgumentException`.
- BR-01.3 `NotFoundException` → 404.
- BR-01.4 `BusinessRuleException` → 400.
- BR-01.5 `ConflictException` → 409.
- BR-01.6 `MethodArgumentNotValidException` → 400 with `fields` = `{campo: mensagem}`.
- BR-01.7 unreadable body / type mismatch / missing parameter → 400 `"Requisição inválida: ..."`.
- BR-01.8 `DataIntegrityViolationException` with `ORA-00001` or H2 unique violation → 409 `"Registro duplicado: já existe um registro com esses dados."`.
- BR-01.9 `DataIntegrityViolationException` with `ORA-02292` or H2 referential violation → 409 `"Não é possível excluir: existem registros ligados a este."`.
- BR-01.10 other `DataIntegrityViolationException` → 400 `"Dados inválidos: uma restrição do banco foi violada."`.
- BR-01.11 `NoResourceFoundException` → 404 `"Recurso não encontrado."`.
- BR-01.12 any other exception → 500 `"Erro interno. Tente novamente."`, stack trace logged, never returned.
- BR-01.13 the JVM default time zone and the `Clock` bean are `America/Sao_Paulo`.
