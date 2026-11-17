# 09 — Alertas · Design

## Entity `Alerta` → `T_RP_ALERTA`

| Field | Column | Type |
| --- | --- | --- |
| idAlerta | id_alerta | NUMBER identity PK |
| lote | id_lote | NUMBER FK not null |
| tipo | tipo | VARCHAR2(15) not null, `TipoAlerta` (NOVO_LOTE, XEPA) |
| canal | canal | VARCHAR2(15) not null, `CanalAlerta` (APP, WHATSAPP) |
| dataEnvio | data_envio | DATE not null |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| POST | /api/alertas/xepa | `{idOfertante, idsLotes?, novoPrazo?, canal?}` | XepaResponse | 201, 400, 404 |
| GET | /api/alertas/recentes?horas=24 | — | `[AlertaResponse]` | 200, 400 |
| GET | /api/alertas?tipo=&idLote= | — | `[AlertaResponse]` | 200 |
| GET | /api/alertas/{id} | — | AlertaResponse | 200, 404 |
| POST | /api/alertas | `{idLote, tipo, canal?}` | AlertaResponse | 201, 400, 404 |
| PUT | /api/alertas/{id} | `{tipo, canal}` | AlertaResponse | 200, 400, 404 |
| DELETE | /api/alertas/{id} | — | — | 204, 404 |

```json
// XepaResponse
{ "quantidade": 2, "mensagem": "Xepa avisada para 2 lotes.",
  "alertas": [ { "idAlerta": 30, "idLote": 7, "descricaoLote": "Tomates", "idOfertante": 1,
                 "nomeOfertante": "Banca do Seu Antônio", "tipoOfertante": "FEIRANTE", "bairro": "Paraíso",
                 "dataLimiteRetirada": "2026-11-17T14:00:00", "statusLote": "DISPONIVEL",
                 "tipo": "XEPA", "canal": "APP", "dataEnvio": "2026-11-17T12:30:00" } ] }
```

## Layers
- `AlertaDAO`: `filtrar(tipo, idLote)`, `recentes(desde)` (lote DISPONIVEL), `findByLote_IdLote`.
- `AlertaBO`: `avisarXepa`, CRUD, `recentes`. Uses `OfertanteBO`, `LoteBO` (expiry, find), `LoteDAO`, `Clock`.
- `LoteBO.criar` saves a NOVO_LOTE/APP alert through `AlertaDAO` (no BO cycle); `LoteBO.excluir` deletes the lote's alerts.
