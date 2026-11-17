# 04 — Lotes · Requirements

HU covered: **HU02** (registrar lote), **HU03** (editar/cancelar lote não reservado), **HU09** (consultar lotes disponíveis com filtros), **HU21** (expiração automática), hook for **HU05** (alerta NOVO_LOTE, wired in feature 09).

## Acceptance criteria

1. **Given** an active ofertante and a categoria, **when** `POST /api/lotes` with valid data, **then** 201, `status=DISPONIVEL`, `dataCadastro=now`, Location header.
2. **Given** `modalidade=DOACAO` and a `preco`, **when** creating, **then** 400 `"Doação não tem preço."`.
3. **Given** `modalidade=VENDA` without preco or with preco ≤ 0, **when** creating, **then** 400 `"Lote de venda precisa de preço maior que zero."`.
4. **Given** a deadline in the past, **when** creating, **then** 400 `"O prazo de retirada deve ser no futuro."`.
5. **Given** an inactive ofertante, **when** creating, **then** 400 `"Ofertante inativo não pode registrar lotes."`.
6. **Given** lots in several states, **when** `GET /api/lotes/disponiveis`, **then** only DISPONIVEL lots with deadline > now from active ofertantes are returned, ordered by deadline ascending; `?idCategoria=&modalidade=&bairro=` filter it; expiry runs first.
7. **Given** a lote, **when** `GET /api/lotes/{id}`, **then** a flat DTO with ofertante name/type/address and categoria name.
8. **Given** a lote that is not DISPONIVEL, **when** `PUT /api/lotes/{id}`, **then** 409 `"Só é possível alterar um lote disponível."`.
9. **Given** a lote with a reserva ATIVA/RETIRADA or any destinação, **when** `DELETE`, **then** 409; otherwise its CANCELADA reservas and its alertas are deleted with it and the answer is 204 (cascade wired when Reserva/Destinação/Alerta exist: features 07, 08, 09).
10. **Given** DISPONIVEL or RESERVADO lots past their deadline, **when** expiry runs (`POST /api/lotes/expirar`, scheduler every 5 min, before listing disponíveis and before reserving), **then** they become EXPIRADO and the response is `{"expirados": n}`.
11. **Given** a deadline with hours and minutes, **when** saved on H2 (`DATE` column), **then** hours and minutes are kept.
12. **Given** a categoria used by a lote, **when** `DELETE /api/categorias/{id}`, **then** 409 (BR-02.4).

## Business rules

- BR-04.1 descricao required, ≤ 200 (`"Descrição é obrigatória."`).
- BR-04.2 pesoKg required, > 0, max 2 decimals, ≤ 999999.99 (`"Peso é obrigatório."`, `"Peso deve ser maior que zero."`, `"Peso deve ter no máximo 2 casas decimais."`, `"Peso deve ser no máximo 999999,99 kg."`).
- BR-04.3 DOACAO ⇒ preco null (`"Doação não tem preço."`).
- BR-04.4 VENDA ⇒ preco required and > 0; max 2 decimals, ≤ 99999999.99.
- BR-04.5 valorEstimado optional, ≥ 0 (`"Valor estimado não pode ser negativo."`), max 2 decimals.
- BR-04.6 dataLimiteRetirada required (`"Prazo de retirada é obrigatório."`); on create > now; on update ≥ dataCadastro and > now.
- BR-04.7 ofertante must exist (404) and be ativo (400).
- BR-04.8 categoria must exist (404).
- BR-04.9 status/dataCadastro never set by the client (ignored).
- BR-04.10 update only while DISPONIVEL (409); idOfertante cannot change.
- BR-04.11 delete rules (decisão 6), 409 `"Não é possível excluir: o lote tem reserva ativa ou retirada, ou destino registrado."`.
- BR-04.12 urlFoto optional, ≤ 255, starts with `http://` or `https://` (`"URL da foto deve começar com http:// ou https://."`).
- BR-04.13 expiry: DISPONIVEL/RESERVADO with deadline < now → EXPIRADO; when a RESERVADO lote expires its ATIVA reserva becomes CANCELADA in the same transaction (wired in feature 07).
- BR-04.14 condicao and modalidade required (`"Condição é obrigatória."`, `"Modalidade é obrigatória."`); precisaTransporte null → false.
