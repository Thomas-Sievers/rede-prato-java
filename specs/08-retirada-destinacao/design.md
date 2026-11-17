# 08 — Retirada & Destinação · Design

## Entity `Destinacao` → `T_RP_DESTINACAO`

| Field | Column | Type |
| --- | --- | --- |
| idDestinacao | id_destinacao | NUMBER identity PK |
| lote | id_lote | NUMBER FK not null |
| tipo | tipo | VARCHAR2(15) not null, `TipoDestinacao` (CONSUMO, COMPOSTAGEM) |
| pesoKg | peso_kg | NUMBER(8,2) not null |
| dataDestinacao | data_destinacao | DATE not null |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| PUT | /api/reservas/{id}/retirada | — | ReservaResponse | 200, 404, 409 |
| GET | /api/destinacoes?idLote=&tipo= | — | `[DestinacaoResponse]` | 200 |
| GET | /api/destinacoes/{id} | — | DestinacaoResponse | 200, 404 |
| POST | /api/destinacoes | `{idLote, tipo, pesoKg, dataDestinacao?}` | DestinacaoResponse | 201, 400, 404 |
| PUT | /api/destinacoes/{id} | same | DestinacaoResponse | 200, 400, 404 |
| DELETE | /api/destinacoes/{id} | — | — | 204, 404 |
| POST | /api/lotes/{id}/destinacoes | `{consumoKg?, compostagemKg?}` | `[DestinacaoResponse]` | 201, 400, 404 |

```json
// DestinacaoResponse
{ "idDestinacao": 3, "idLote": 7, "descricaoLote": "Tomates", "pesoLoteKg": 8.5, "statusLote": "RETIRADO",
  "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio",
  "tipo": "CONSUMO", "pesoKg": 7.0, "dataDestinacao": "2026-11-17T19:00:00" }
```

## Layers
- `DestinacaoDAO`: `filtrar(idLote, tipo)`, `somaPorLote(idLote, idExcluir)`, `existsByLote_IdLote`.
- `DestinacaoBO`: CRUD + `registrarLote(idLote, consumoKg, compostagemKg)`.
- `ReservaBO.confirmarRetirada(id)`.
- `LoteBO.checkPodeExcluir` also refuses when a destinação exists.
