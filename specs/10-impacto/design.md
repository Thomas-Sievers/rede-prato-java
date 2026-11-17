# 10 — Impacto · Design

No new table: aggregates over `T_RP_DESTINACAO`, `T_RP_RESERVA`, `T_RP_LOTE`, `T_RP_BENEFICIARIO`.

## Endpoints

| Method | Path | Response | Status |
| --- | --- | --- | --- |
| GET | /api/impacto/ofertantes/{id}?mes=2026-11 | ImpactoOfertante | 200, 400, 404 |
| GET | /api/impacto/rede?mes=2026-11 | ImpactoRede | 200, 400 |

```json
{ "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio", "mes": "2026-11",
  "totalKg": 186.0, "consumoKg": 152.0, "compostagemKg": 34.0, "percentualConsumo": 81.7,
  "lotesRetirados": 41, "beneficiariosAtendidos": 27, "pessoasAtendidas": 95,
  "porSemana": [ {"inicio": "2026-10-26", "fim": "2026-11-01", "kg": 38.0}, "... 4 items, last = current week" ],
  "possuiSelo": true, "dataSelo": "2026-11-07" }
// rede: same totals without idOfertante/nome/selo, plus
  "ofertantes": [ {"idOfertante": 1, "nome": "Banca do Seu Antônio", "totalKg": 186.0} ]
```

## DAO queries (JPQL)
- `DestinacaoDAO.somaPorTipo(idOfertante?, inicio, fim)` → `[KgPorTipo(tipo, kg)]` (`GROUP BY d.tipo`).
- `DestinacaoDAO.somaPeriodo(idOfertante?, inicio, fim)` → BigDecimal (weekly buckets).
- `DestinacaoDAO.ranking(inicio, fim)` → `[KgPorOfertante(id, nome, kg)]` ordered by kg desc.
- `ReservaDAO.contarLotesRetirados(idOfertante?, inicio, fim)` (`COUNT DISTINCT`).
- `ReservaDAO.beneficiariosAtendidos(idOfertante?, inicio, fim)` (distinct beneficiários; the BO sums `qtdPessoas`).

## BO
`ImpactoBO` does the arithmetic (percent, null qtd → 1, zero-fill, week math) — unit-tested with mocked DAO results and a fixed Clock.
