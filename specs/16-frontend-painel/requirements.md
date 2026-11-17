# 16 — Painel de impacto + selo (frontend) · Requirements

HU covered: **HU15** (painel com kg salvos), **HU20** (conceder selo). Screen: prototipo.md §3.5, `docs/prototipo/4-painel.png`.

## Acceptance criteria

1. **Given** the selected ofertante, **then** the page shows "{mês de ano}" / **Meu impacto**, the hero "Este mês você salvou do lixo **{total} kg**", and 4 cards (comida, adubo, famílias e instituições atendidas, lotes retirados) from `GET /api/impacto/ofertantes/{id}`.
2. **Given** `porSemana`, **then** 4 CSS bars sized proportionally with the kg on top, labels "Semana 1 … Esta semana", last bar darker; plus "Para onde foi" bar with % comida / % adubo.
3. **Given** `possuiSelo`, **then** the green Selo card ("você é parceiro", badge, quote with the total, "Mandar no WhatsApp" → `https://wa.me/?text=...`, "Baixar o selo" → downloads a generated SVG); otherwise "Ainda sem selo" and how to get it.
4. **Given** the default (H2/seed) profile from `/api/health`, **then** a yellow badge "Dados de exemplo do protótipo" is shown.
5. **Given** the admin strip, **then** choosing an ofertante and "Conceder selo" calls `PUT /api/ofertantes/{id}/selo` and shows the result or the API error (409 already has it).
6. **Given** `GET /api/impacto/rede`, **then** an "Impacto da rede" block shows network totals and the ranking of ofertantes.
7. A month selector (`<input type="month">`) reloads the data for another month.
