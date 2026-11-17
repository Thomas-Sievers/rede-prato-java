# Progress
Last update: 2026-11-17 02:25 (America/Sao_Paulo)

| NN | Feature | Status | Tests | Notes |
| --- | --- | --- | --- | --- |
| 00 | Initial commit | merged | — | docs + plan + logs |
| 01 | Setup | merged | 33 | Boot 4.1.1, H2/Oracle profiles, converters, error layer, health, Dockerfile |
| 02 | Categorias & Endereços | merged | 69 | CRUD, unique nome, CEP/UF rules |
| 03 | Ofertantes & Selo | merged | 99 | logical delete, selo grant/revoke |
| 04 | Lotes | merged | 139 | CRUD, disponíveis, expiry job |
| 05 | Beneficiários | merged | 158 | no documents, optional phone/address |
| 06 | Pontos | merged | 184 | HH:MM schedule |
| 07 | Reservas | merged | 218 | UC12 rules, cancel, delete cascade |
| 08 | Retirada & Destinação | merged | 241 | retirada, destinos, batch endpoint |
| 09 | Alertas | merged | 269 | xepa (UC04), NOVO_LOTE, recentes |
| 10 | Impacto | merged | 280 | JPQL aggregates, weekly buckets |
| 11 | Seed | merged | 286 | simulated data relative to the clock |
| 12 | Frontend base + landing | merged | 292 | CSS tokens, api.js, persona.js, landing |
| 13 | Frontend lotes | merged | 295 | filters, reserva dialog, sacola/cancel |
| 14 | Frontend registrar | merged | 297 | xepa + desfazer, 7-step form, meus lotes |
| 15 | Frontend retiradas | merged | 299 | já buscou, destino panel, conferir número |
| 16 | Frontend painel | merged | 302 | impacto, weekly bars, selo, rede, admin |
| 17 | Docs | todo | | |
| 18 | Extras (nice) | todo | | |

(Test column = total suite size after the merge.)

## Next step
Feature 17 (`feature/17-docs`): docs/endpoints.md, class-diagram.md, der.md, run.md, oracle-checklist.md, final README.
