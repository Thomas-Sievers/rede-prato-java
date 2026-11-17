# 05 — Beneficiários · Requirements

HU covered: **HU07** (beneficiário se cadastra sem documento; telefone e endereço opcionais), part of **HU18** (exclusão lógica).

## Acceptance criteria

1. **Given** only `{nome, tipo}`, **when** `POST /api/beneficiarios`, **then** 201 with `ativo=true`, no phone, no address (decisão 12).
2. **Given** `tipo=PESSOA` without qtdPessoas, **when** creating, **then** `qtdPessoas=1`; with qtdPessoas ≠ 1 → 400 `"Pessoa sozinha deve ter quantidade de pessoas igual a 1."`.
3. **Given** `qtdPessoas` 0 or 10000, **when** creating, **then** 400 `"Quantidade de pessoas deve ser entre 1 e 9999."`.
4. **Given** a telefone with 9 digits, **when** creating, **then** 400.
5. **Given** an unknown idEndereco, **when** creating, **then** 404.
6. **Given** beneficiários, **when** `GET /api/beneficiarios?tipo=&ativo=`, **then** filtered list (default active), ordered by nome.
7. **Given** a beneficiário, **when** `DELETE`, **then** 204 and `ativo=false` (logical, idempotent).
8. **Given** an endereço used by a beneficiário, **when** `DELETE /api/enderecos/{id}`, **then** 409.

## Business rules

- BR-05.1 nome required ≤ 100.
- BR-05.2 tipo required (PESSOA, FAMILIA, INSTITUICAO, PEQUENO_NEGOCIO) — `"Tipo do beneficiário é obrigatório."`.
- BR-05.3 qtdPessoas optional, 1–9999; PESSOA ⇒ 1 (set when null; 400 otherwise).
- BR-05.4 telefone optional; when present 10–11 digits.
- BR-05.5 endereço optional; must exist when given (404).
- BR-05.6 no document fields exist (no CPF/RG) — checked by a reflection test on the entity.
- BR-05.7 logical delete only; PUT with `"ativo": true` reactivates (D-6).
