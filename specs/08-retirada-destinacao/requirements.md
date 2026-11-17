# 08 — Retirada & Destinação · Requirements

HU covered: **HU13** (ofertante ou operador confirma a retirada — UC09), **HU14** (registrar quanto foi consumido e quanto virou adubo — UC06).

## Acceptance criteria

1. **Given** an ATIVA reserva whose lote is RESERVADO, **when** `PUT /api/reservas/{id}/retirada`, **then** 200, reserva RETIRADA with `dataRetirada=now`, lote RETIRADO (one transaction).
2. **Given** a reserva that is not ATIVA, **when** confirming, **then** 409 `"Só é possível confirmar a retirada de uma reserva ativa."`.
3. **Given** a reserva ATIVA whose lote is not RESERVADO, **when** confirming, **then** 409 `"O lote desta reserva não está reservado."`.
4. **Given** a RETIRADO lote, **when** `POST /api/destinacoes {idLote, tipo: CONSUMO|COMPOSTAGEM, pesoKg}`, **then** 201 and `dataDestinacao=now` when absent.
5. **Given** an EXPIRADO lote, **when** posting CONSUMO, **then** 400 `"Lote vencido só pode ir para compostagem."`; COMPOSTAGEM is accepted.
6. **Given** a DISPONIVEL or RESERVADO lote, **when** posting, **then** 400 `"Registre a retirada antes do destino."`.
7. **Given** a lote of 8.5 kg with 6 kg already registered, **when** posting 3 kg, **then** 400 `"A soma dos destinos passa do peso do lote (8,5 kg)."`; on PUT the row being edited is excluded from the sum.
8. **Given** `dataDestinacao` in the future, **then** 400 `"A data do destino não pode ser no futuro."`.
9. **Given** any destinação, **when** `DELETE /api/destinacoes/{id}`, **then** 204 (corrects a wrong entry).
10. **Given** `POST /api/lotes/{id}/destinacoes {consumoKg, compostagemKg}`, **then** up to two rows are created in one transaction with the same rules; both zero/absent → 400 `"Informe o peso consumido ou compostado."`.
11. **Given** a lote with a destinação, **when** `DELETE /api/lotes/{id}`, **then** 409 (BR-04.11).

## Business rules

- BR-08.1 retirada only for ATIVA reservas (409).
- BR-08.2 the lote must be RESERVADO (409).
- BR-08.3 lote RETIRADO ⇒ CONSUMO or COMPOSTAGEM.
- BR-08.4 lote EXPIRADO ⇒ only COMPOSTAGEM (400).
- BR-08.5 lote DISPONIVEL/RESERVADO ⇒ 400.
- BR-08.6 pesoKg required, > 0, max 2 decimals, ≤ 999999.99; tipo required (`"Tipo do destino é obrigatório."`).
- BR-08.7 sum of the lote's destinações (excluding the one being updated) + new pesoKg ≤ lote.pesoKg.
- BR-08.8 dataDestinacao not in the future (default now).
