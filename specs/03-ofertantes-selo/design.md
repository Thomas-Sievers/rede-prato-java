# 03 — Ofertantes & Selo · Design

## Entity `Ofertante` → `T_RP_OFERTANTE`

| Field | Column | Type |
| --- | --- | --- |
| idOfertante | id_ofertante | NUMBER identity PK |
| nome | nome | VARCHAR2(100) not null |
| tipo | tipo | VARCHAR2(20) not null, enum `TipoOfertante` (SUPERMERCADO, FEIRANTE) |
| telefone | telefone | VARCHAR2(15) not null |
| email | email | VARCHAR2(100) |
| possuiSelo | possui_selo | CHAR(1) not null, `BooleanSNConverter`, default false |
| dataSelo | data_selo | DATE, `LocalDate` |
| ativo | ativo | CHAR(1) not null, `BooleanSNConverter`, default true |
| endereco | id_endereco | NUMBER FK → T_RP_ENDERECO, `@ManyToOne(LAZY)` not null |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/ofertantes?tipo=&ativo=&bairro= | — | `[OfertanteResponse]` (ativo default true) | 200, 400 (bad enum) |
| GET | /api/ofertantes/{id} | — | OfertanteResponse | 200, 404 |
| POST | /api/ofertantes | OfertanteRequest | OfertanteResponse | 201, 400, 404 |
| PUT | /api/ofertantes/{id} | OfertanteRequest (+ `ativo?`) | OfertanteResponse | 200, 400, 404 |
| DELETE | /api/ofertantes/{id} | — | — (logical) | 204, 404 |
| PUT | /api/ofertantes/{id}/selo | — | OfertanteResponse | 200, 400, 404, 409 |
| DELETE | /api/ofertantes/{id}/selo | — | — | 204, 404 |

```json
// OfertanteRequest
{ "nome": "Banca do Seu Antônio", "tipo": "FEIRANTE", "telefone": "(11) 90000-0001",
  "email": null, "idEndereco": 1, "ativo": null }
// OfertanteResponse
{ "idOfertante": 1, "nome": "Banca do Seu Antônio", "tipo": "FEIRANTE", "telefone": "11900000001",
  "email": null, "possuiSelo": true, "dataSelo": "2026-11-07", "ativo": true,
  "idEndereco": 1, "logradouro": "Rua Manuel da Nóbrega", "numero": "s/n", "complemento": "Banca 12",
  "bairro": "Paraíso", "cidade": "São Paulo", "uf": "SP", "cep": "04001000" }
```

## Layers
- `OfertanteDAO`: `filtrar(tipo, ativo, bairro)` JPQL with optional params, join fetch endereco, order by nome; `existsByEndereco_IdEndereco`.
- `OfertanteBO`: CRUD + `concederSelo`, `revogarSelo`; `find(id)` reused by Lote/Alerta/Impacto BOs; uses `Clock` for `dataSelo`.
- `EnderecoBO.checkSemUso` now asks `OfertanteDAO.existsByEndereco_IdEndereco` (BR-02.9).

## Exceptions
404 ofertante/endereço; 400 rules; 409 selo already granted / endereço in use.
