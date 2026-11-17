# 10 — Impacto · Requirements

HU covered: **HU15** (ofertante vê painel com kg salvos do descarte — UC10), basis for **HU22** (relatório mensal).

## Acceptance criteria

1. **Given** destinações of an ofertante's lots in November 2026, **when** `GET /api/impacto/ofertantes/{id}?mes=2026-11`, **then** `totalKg`, `consumoKg`, `compostagemKg` are the sums of `T_RP_DESTINACAO.peso_kg` with `data_destinacao` in that month.
2. **Given** consumo 152 and compostagem 34, **then** `percentualConsumo = 81.7` (1 decimal); with no destinações → 0.
3. **Given** reservas RETIRADA with `dataRetirada` in the month, **then** `lotesRetirados` counts their distinct lotes, `beneficiariosAtendidos` the distinct beneficiários, and `pessoasAtendidas` sums their `qtdPessoas` (null counts as 1).
4. **Given** no `mes`, **then** the current month (from the Clock) is used; `mes=2026-13` or `nov` → 400 `"Mês deve estar no formato AAAA-MM."`.
5. **Given** any request, **then** `porSemana` has 4 Monday–Sunday weeks, the last one being the current week, zero-filled.
6. **Given** an unknown ofertante, **then** 404.
7. **Given** `GET /api/impacto/rede?mes=`, **then** the same totals for the whole network plus `ofertantes: [{idOfertante, nome, totalKg}]` ranked by kg desc.
8. The response includes `possuiSelo` and `dataSelo` of the ofertante (painel shows the selo).

## Business rules

- BR-10.1 kg come only from Destinacao.peso_kg joined to the lote's ofertante, filtered by data_destinacao in [first day of month, first day of next month).
- BR-10.2 lotesRetirados = distinct lotes of the ofertante with a reserva RETIRADA whose dataRetirada is in the month.
- BR-10.3 beneficiariosAtendidos = distinct beneficiários of those reservas; pessoasAtendidas = Σ qtdPessoas (null → 1).
- BR-10.4 percentualConsumo = consumo / total × 100, HALF_UP, 1 decimal; 0 when total = 0.
- BR-10.5 porSemana = last 4 Monday–Sunday weeks ending in the current week (Clock), zero-filled.
- BR-10.6 ofertante must exist (404).
- BR-10.7 mes format `yyyy-MM`; default current month.
- Refeições estimadas are not computed (grams per meal undefined in the docs — D-11).
