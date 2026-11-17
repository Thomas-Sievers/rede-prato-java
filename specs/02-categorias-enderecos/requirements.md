# 02 — Categorias & Endereços · Requirements

HU covered: **HU19** (administrador gerencia categorias de alimento), base for **HU01** (endereço do ofertante).

## Acceptance criteria

1. **Given** categorias exist, **when** `GET /api/categorias`, **then** 200 with the list ordered by nome (empty list when none).
2. **Given** a valid body `{nome, descricao?}`, **when** `POST /api/categorias`, **then** 201, `Location: /api/categorias/{id}` and the saved categoria.
3. **Given** a categoria with nome "Frutas" exists, **when** another is created with nome " frutas ", **then** 409 `"Já existe uma categoria com esse nome."`.
4. **Given** an unknown id, **when** `GET/PUT/DELETE /api/categorias/{id}`, **then** 404 `"Categoria 99 não encontrada."`.
5. **Given** a categoria used by a lote, **when** `DELETE`, **then** 409 `"Não é possível excluir: existem lotes nesta categoria."` (check wired in feature 04 when Lote exists).
6. **Given** a valid endereço body, **when** `POST /api/enderecos`, **then** 201; cep stored with 8 digits only; uf upper-cased.
7. **Given** `GET /api/enderecos?bairro=paraíso`, **then** only endereços whose bairro matches ignoring case.
8. **Given** an endereço used by an ofertante, ponto or beneficiário, **when** `DELETE`, **then** 409 `"Não é possível excluir: este endereço está em uso."` (checks wired in features 03, 05, 06).
9. **Given** an unused categoria/endereço, **when** `DELETE`, **then** 204.

## Business rules

- BR-02.1 categoria `nome` required, trimmed, ≤ 50 chars (`"Nome da categoria é obrigatório."` / `"Nome da categoria deve ter no máximo 50 caracteres."`).
- BR-02.2 categoria `nome` unique ignoring case, also on update (other id) → 409 `"Já existe uma categoria com esse nome."`.
- BR-02.3 categoria `descricao` optional, ≤ 200 (`"Descrição deve ter no máximo 200 caracteres."`); blank → null.
- BR-02.4 categoria in use by a lote cannot be deleted → 409.
- BR-02.5 endereço required: `logradouro`, `bairro`, `cidade`, `uf`, `cep` (`"<Campo> é obrigatório."`).
- BR-02.6 `cep`: strip `-`, `.` and spaces; must be exactly 8 digits (`"CEP deve ter 8 dígitos."`).
- BR-02.7 `uf`: trimmed, upper-cased, one of the 27 Brazilian UFs (`"UF inválida."`).
- BR-02.8 lengths: logradouro ≤ 100, numero ≤ 10, complemento ≤ 50, bairro ≤ 60, cidade ≤ 60 (`"<Campo> deve ter no máximo N caracteres."`).
- BR-02.9 endereço in use (ofertante, ponto, beneficiário) cannot be deleted → 409.
