# 03 — Ofertantes & Selo · Requirements

HU covered: **HU01** (ofertante se cadastra com tipo, contato e endereço), **HU20** (administrador concede o Selo de Parceiro), part of **HU18** (exclusão lógica).

## Acceptance criteria

1. **Given** a valid body `{nome, tipo, telefone, email?, idEndereco}`, **when** `POST /api/ofertantes`, **then** 201 with `ativo=true`, `possuiSelo=false`, `dataSelo=null`, telefone stored as digits only.
2. **Given** ofertantes exist, **when** `GET /api/ofertantes`, **then** only active ones are listed (default `ativo=true`), ordered by nome; `?tipo=FEIRANTE`, `?ativo=false`, `?bairro=Paraíso` filter the list.
3. **Given** an ofertante, **when** `GET /api/ofertantes/{id}`, **then** 200 with endereço fields flat plus `possuiSelo` and `dataSelo`.
4. **Given** an unknown idEndereco, **when** creating/updating, **then** 404 `"Endereço 9 não encontrado."`.
5. **Given** an ofertante, **when** `DELETE /api/ofertantes/{id}`, **then** 204 and it stays in the database with `ativo=false`; repeating the call returns 204 again.
6. **Given** an active ofertante without selo, **when** `PUT /api/ofertantes/{id}/selo`, **then** 200 with `possuiSelo=true` and `dataSelo=today`.
7. **Given** an ofertante that already has the selo, **when** `PUT .../selo`, **then** 409 `"Este ofertante já possui o selo."`.
8. **Given** an inactive ofertante, **when** `PUT .../selo`, **then** 400 `"Ofertante inativo não pode receber o selo."`.
9. **Given** an ofertante with selo, **when** `DELETE .../selo`, **then** 204 with `possuiSelo=false` and `dataSelo=null`.
10. **Given** an inactive ofertante, **when** `PUT /api/ofertantes/{id}` with `"ativo": true`, **then** it is reactivated.
11. **Given** an endereço used by an ofertante, **when** `DELETE /api/enderecos/{id}`, **then** 409 (BR-02.9).

## Business rules

- BR-03.1 nome required, ≤ 100 (`"Nome é obrigatório."`).
- BR-03.2 tipo required, SUPERMERCADO or FEIRANTE (`"Tipo do ofertante é obrigatório."`); unknown enum text → 400 (unreadable body).
- BR-03.3 telefone required, 10–11 digits after stripping non-digits (`"Telefone é obrigatório."` / `"Telefone deve ter 10 ou 11 dígitos (DDD + número)."`).
- BR-03.4 email optional, valid format (`"E-mail inválido."`), ≤ 100.
- BR-03.5 endereço required and must exist (404).
- BR-03.6 selo consistency on every save: `possuiSelo` ⇔ `dataSelo != null` (mirrors `CK_RP_OFERTANTE_SELO`); violation → 400 `"Selo inconsistente: selo e data do selo devem andar juntos."`.
- BR-03.7 no physical delete: DELETE sets `ativo=false` (idempotent).
- BR-03.8 inactive ofertante cannot receive the selo (400); already has it → 409.
- BR-03.9 PUT on an inactive ofertante is allowed; optional `ativo` in the PUT body reactivates/deactivates (null keeps the current value). Clients never set `possuiSelo`/`dataSelo` directly.
