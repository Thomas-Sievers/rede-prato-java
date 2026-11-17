# 06 — Pontos de distribuição · Requirements

HU covered: **HU17** (operador mantém dados e horários do ponto), data for **HU16** (pontos e horários), part of **HU18**.

## Acceptance criteria

1. **Given** a valid body with `horarioAbertura:"16:00"` and `horarioFechamento:"18:00"`, **when** `POST /api/pontos`, **then** 201 with the horários as `"HH:MM"` strings and endereço fields flat.
2. **Given** `horarioAbertura:"9h"` or `"24:00"`, **when** creating, **then** 400 `"Horário deve estar no formato HH:MM."`.
3. **Given** abertura ≥ fechamento, **when** creating, **then** 400 `"Horário de abertura deve ser antes do fechamento."`.
4. **Given** no idEndereco or an unknown one, **when** creating, **then** 400 / 404.
5. **Given** pontos, **when** `GET /api/pontos?ativo=&bairro=`, **then** filtered list (default active) ordered by nome.
6. **Given** a ponto, **when** `DELETE`, **then** 204, `ativo=false` (logical).
7. **Given** an endereço used by a ponto, **when** `DELETE /api/enderecos/{id}`, **then** 409.

## Business rules

- BR-06.1 nome required ≤ 100.
- BR-06.2 tipo required (COZINHA_SOLIDARIA, ONG, ASSOCIACAO) — `"Tipo do ponto é obrigatório."`.
- BR-06.3 diasFuncionamento required ≤ 50 (`"Dias de funcionamento é obrigatório."`).
- BR-06.4 horários required (`"Horário de abertura é obrigatório."` / `"Horário de fechamento é obrigatório."`), strict `HH:MM` 00:00–23:59.
- BR-06.5 abertura < fechamento.
- BR-06.6 endereço required and must exist.
- BR-06.7 logical delete only; telefone optional (10–11 digits when present).
