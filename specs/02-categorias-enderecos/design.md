# 02 — Categorias & Endereços · Design

## Entities

`CategoriaAlimento` → `T_RP_CATEGORIA_ALIMENTO`

| Field | Column | Type |
| --- | --- | --- |
| idCategoria | id_categoria | NUMBER, identity, PK |
| nome | nome | VARCHAR2(50), not null, unique |
| descricao | descricao | VARCHAR2(200) |

`Endereco` → `T_RP_ENDERECO`

| Field | Column | Type |
| --- | --- | --- |
| idEndereco | id_endereco | NUMBER, identity, PK |
| logradouro | logradouro | VARCHAR2(100) not null |
| numero | numero | VARCHAR2(10) |
| complemento | complemento | VARCHAR2(50) |
| bairro | bairro | VARCHAR2(60) not null |
| cidade | cidade | VARCHAR2(60) not null |
| uf | uf | CHAR(2) not null |
| cep | cep | CHAR(8) not null |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/categorias | — | `[CategoriaResponse]` by nome | 200 |
| GET | /api/categorias/{id} | — | CategoriaResponse | 200, 404 |
| POST | /api/categorias | CategoriaRequest | CategoriaResponse | 201, 400, 409 |
| PUT | /api/categorias/{id} | CategoriaRequest | CategoriaResponse | 200, 400, 404, 409 |
| DELETE | /api/categorias/{id} | — | — | 204, 404, 409 |
| GET | /api/enderecos?bairro= | — | `[EnderecoResponse]` | 200 |
| GET | /api/enderecos/{id} | — | EnderecoResponse | 200, 404 |
| POST | /api/enderecos | EnderecoRequest | EnderecoResponse | 201, 400 |
| PUT | /api/enderecos/{id} | EnderecoRequest | EnderecoResponse | 200, 400, 404 |
| DELETE | /api/enderecos/{id} | — | — | 204, 404, 409 |

## DTOs

```json
// CategoriaRequest / CategoriaResponse
{ "nome": "Frutas", "descricao": "Frutas frescas" }
{ "idCategoria": 1, "nome": "Frutas", "descricao": "Frutas frescas" }
// EnderecoRequest
{ "logradouro": "Rua Manuel da Nóbrega", "numero": "120", "complemento": "Banca 12",
  "bairro": "Paraíso", "cidade": "São Paulo", "uf": "SP", "cep": "04001-000" }
// EnderecoResponse = idEndereco + same fields (cep "04001000")
```

## Layers
- `CategoriaAlimentoDAO`: `findAllByOrderByNomeAsc()`, `existsByNomeIgnoreCase`, `existsByNomeIgnoreCaseAndIdCategoriaNot`.
- `EnderecoDAO`: `findByBairroIgnoreCaseOrderByLogradouroAsc`, `findAllByOrderByIdEnderecoAsc`.
- `CategoriaAlimentoBO`, `EnderecoBO`: CRUD; `validate()` calls private `checkXxx()` methods; `@Transactional` writes; return response DTOs (mapping inside the transaction because relations are LAZY and open-in-view is off).
- Shared helper `bo/Checks` (text required/length, digits).
- In-use checks: the BO receives a `UsageChecker`-style dependency only when the dependent DAOs exist (features 03–06). Until then `excluir` deletes directly.

## Exceptions
- not found → `NotFoundException` (404); rules → `BusinessRuleException` (400); duplicate / in use → `ConflictException` (409).

## Transactions
Write methods `@Transactional`; reads `@Transactional(readOnly = true)`.
