# Progress
Last update: 2026-11-17 02:05 (America/Sao_Paulo)

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
| 09 | Alertas | todo | | |
| 10 | Impacto | todo | | |
| 11 | Seed | todo | | |
| 12 | Frontend base + landing | todo | | |
| 13 | Frontend lotes | todo | | |
| 14 | Frontend registrar | todo | | |
| 15 | Frontend retiradas | todo | | |
| 16 | Frontend painel | todo | | |
| 17 | Docs | todo | | |
| 18 | Extras (nice) | todo | | |

(Test column = total suite size after the merge.)

## Next step
Feature 09 (`feature/09-alertas`): Alerta entity, xepa endpoint (UC04), NOVO_LOTE alert on lote creation, alert cascade on lote delete.
