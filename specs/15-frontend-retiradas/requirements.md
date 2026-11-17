# 15 — Retiradas + destino (frontend) · Requirements

HU covered: **HU13** (confirmar a retirada — UC09), **HU14** (quanto virou comida e quanto virou adubo — UC06). Screen: prototipo.md §3.4, `docs/prototipo/3-retiradas.png`.

## Acceptance criteria

1. **Given** the selected ofertante, **then** its reservas are listed (ATIVA first, then RETIRADA; CANCELADA hidden): illustration, "{DESCRIÇÃO} · {peso} kg", "{beneficiário} · {tipo}", "Número **{id}** · busca na banca | levado ao ponto {nome}".
2. **Given** an ATIVA reserva, **when** "Já buscou", **then** `PUT /api/reservas/{id}/retirada` and the destination panel opens for that lote.
3. **Given** a RETIRADA without destination, **then** the yellow badge "Falta dizer o destino" (clickable, opens the panel); with destinations, "✓ {peso} kg: {x} comida, {y} adubo".
4. **Given** the panel, **then** "Virou comida" has +/- (default = full weight), "Virou adubo" = peso − comida, a split bar shows both, and **Salvar** calls `POST /api/lotes/{id}/destinacoes {consumoKg, compostagemKg}` (0 kg is skipped).
5. **Given** "Número da pessoa" + **Conferir**, **then** the reserva is found by id and highlighted; not found / other ofertante → clear Portuguese message.
6. **Given** EXPIRADO lots without destination, **then** a "Venceu sem retirada" section offers **Mandar para adubo** (COMPOSTAGEM only, decisão 4).
7. API errors are shown with their Portuguese message.
