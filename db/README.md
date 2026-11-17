# db/ — Database scripts (Database Design, Mariana Souza França)

Mariana's scripts are the **source of truth** for tables, columns, types and constraints (see `docs/decisoes.md`). They are stored here exactly as she delivered them. Do not change them.

## Files

| File | Original name | Content |
| --- | --- | --- |
| `ddl.sql` | `rede_de_prato_backend_ddl.sql` | Tables, PKs, FKs, `CHECK`s, defaults, the conditional unique index `UX_RP_RESERVA_ATIVA_LOTE` and helper indexes. |
| `dml.sql` | `rede_de_prato_backend_dml_atualizado.sql` | Sample data (at least 5 rows per table), then `ALTER TABLE ... (START WITH LIMIT VALUE)` so the identity counters start after the seeded IDs. |
| `dql.sql` | `rede_de_prato_backend_dql.sql` | The 3 queries required by the Database Design rubric (INNER JOIN, GROUP BY, LEFT OUTER JOIN). |

Delivered on 17/11/2026.

## Order

1. `ddl.sql`
2. `dml.sql`
3. `dql.sql` (optional; read-only queries)

Then run the app with `SPRING_PROFILES_ACTIVE=oracle` (see `docs/run.md`).

## Review against the backend (17/11/2026)

- Every table, column, type, length, nullability and enum value matches the entities. No entity changes were needed.
- The `CHECK` rules are also enforced in the services, so the API returns readable messages before Oracle rejects anything.
- Known, accepted data quirks (the team decided to keep the script as delivered):
  - No sample lote is `DISPONIVEL`, so the "Lotes" screen starts empty on Oracle. Register lotes through the app for the demo.
  - All sample lotes have `data_limite_retirada = SYSDATE + 30`, including lote 6, which is `EXPIRADO`.
  - Lote 5 stays `RESERVADO` with an `ATIVA` reserva, which is useful to demo "confirmar retirada".
  - Text has no accents ("Sao Paulo") and category names are in capitals ("FRUTAS").
