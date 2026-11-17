# 18 — Extras (nice-to-have) · Requirements

HU covered: **HU22** (relatório mensal), **HU24/HU25** (lote precisa de transporte / coletas pendentes), **HU16** (pontos e horários, sem mapa).

## Acceptance criteria

1. (a) **Given** springdoc resolves for Boot 4, **then** `/swagger-ui.html` shows the API and `/v3/api-docs` returns the OpenAPI JSON; otherwise skipped and logged.
2. (b) **Given** `GET /api/relatorios/ofertantes/{id}/mensal?mes=2026-11`, **then** 200 with ofertante name, mês, the lots that received a destinação in the month (data, alimento, categoria, peso, valor estimado, destino consumo/compostagem kg) and totals (kg, consumo, compostagem, valor estimado); unknown ofertante → 404; bad mês → 400.
3. (c) **Given** `GET /api/lotes?precisaTransporte=true`, **then** only lots marked as needing transport are listed (combinable with the other filters).
4. (d) **Given** the landing, **then** an "Onde buscar" section lists all active pontos with address, days and hours (HU16 without a map).

## Business rules
- BR-18.1 the report only uses destinações with `data_destinacao` in the month (same source as the painel, BR-10.1).
- BR-18.2 valorEstimado total ignores lots without valor (null → 0).
- BR-18.3 `precisaTransporte` filter is optional; absent = no filter.
