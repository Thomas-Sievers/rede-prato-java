# 04 — Lotes · Design

## Entity `Lote` → `T_RP_LOTE`

| Field | Column | Type |
| --- | --- | --- |
| idLote | id_lote | NUMBER identity PK |
| ofertante | id_ofertante | NUMBER FK not null (`@ManyToOne LAZY`) |
| categoria | id_categoria | NUMBER FK not null (`@ManyToOne LAZY`) |
| descricao | descricao | VARCHAR2(200) not null |
| pesoKg | peso_kg | NUMBER(8,2) not null, BigDecimal |
| condicao | condicao | VARCHAR2(20) not null, `CondicaoLote` (BOA, MADURA, CONSUMO_IMEDIATO) |
| modalidade | modalidade | VARCHAR2(15) not null, `ModalidadeLote` (DOACAO, VENDA) |
| preco | preco | NUMBER(10,2) |
| valorEstimado | valor_estimado | NUMBER(10,2) |
| dataCadastro | data_cadastro | DATE not null, LocalDateTime |
| dataLimiteRetirada | data_limite_retirada | DATE not null, LocalDateTime |
| urlFoto | url_foto | VARCHAR2(255) |
| precisaTransporte | precisa_transporte | CHAR(1) not null S/N |
| status | status | VARCHAR2(15) not null, `StatusLote` (DISPONIVEL, RESERVADO, RETIRADO, EXPIRADO) |

## Status transitions (docs/classes.md)
| From | To | Where |
| --- | --- | --- |
| DISPONIVEL | RESERVADO | ReservaBO.criar (07) |
| RESERVADO | DISPONIVEL | ReservaBO.cancelar (07) |
| RESERVADO | RETIRADO | ReservaBO.confirmarRetirada (08) |
| DISPONIVEL | EXPIRADO | LoteBO.expirarVencidos (04) |
| RESERVADO | EXPIRADO | LoteBO.expirarVencidos (04) + reserva ATIVA → CANCELADA (07) |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/lotes?status=&idOfertante=&idCategoria=&modalidade= | — | `[LoteResponse]` newest first | 200 |
| GET | /api/lotes/disponiveis?idCategoria=&modalidade=&bairro= | — | `[LoteResponse]` by deadline asc | 200 |
| GET | /api/lotes/{id} | — | LoteResponse | 200, 404 |
| POST | /api/lotes | LoteRequest | LoteResponse | 201, 400, 404 |
| PUT | /api/lotes/{id} | LoteRequest (idOfertante ignored) | LoteResponse | 200, 400, 404, 409 |
| DELETE | /api/lotes/{id} | — | — | 204, 404, 409 |
| POST | /api/lotes/expirar | — | `{"expirados": n}` | 200 |

```json
// LoteRequest
{ "idOfertante": 1, "idCategoria": 2, "descricao": "Caixa de tomates maduros", "pesoKg": 8.5,
  "condicao": "MADURA", "modalidade": "DOACAO", "preco": null, "valorEstimado": 40.00,
  "dataLimiteRetirada": "2026-11-17T18:00:00", "urlFoto": null, "precisaTransporte": false }
// LoteResponse
{ "idLote": 7, "descricao": "Caixa de tomates maduros", "pesoKg": 8.5, "condicao": "MADURA",
  "modalidade": "DOACAO", "preco": null, "valorEstimado": 40.0, "dataCadastro": "2026-11-17T10:00:00",
  "dataLimiteRetirada": "2026-11-17T18:00:00", "urlFoto": null, "precisaTransporte": false,
  "status": "DISPONIVEL", "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio",
  "tipoOfertante": "FEIRANTE", "ofertantePossuiSelo": true, "logradouro": "Rua Manuel da Nóbrega",
  "numero": "s/n", "bairro": "Paraíso", "idCategoria": 2, "nomeCategoria": "Legumes" }
```

## Layers
- `LoteDAO`: `filtrar(...)`, `disponiveis(now, idCategoria, modalidade, bairro)` (join fetch ofertante/endereço/categoria), `findByStatusInAndDataLimiteRetiradaBefore`, `existsByCategoria_IdCategoria`.
- `LoteBO`: CRUD, `listarDisponiveis`, `expirarVencidos()` (returns count), `find(id)`; `Clock` for now.
- `config/ExpiracaoJob`: `@Scheduled(fixedDelay = 5 min, initialDelay = 1 min)` → `loteBO.expirarVencidos()`; disabled with `app.expiracao.enabled=false`.
- `CategoriaAlimentoBO.checkSemLotes` uses `LoteDAO.existsByCategoria_IdCategoria` (BR-02.4).
- Hooks to be filled later: `LoteBO.excluir` dependency checks + cascade (07/08/09), expiry cancelling reserva (07), NOVO_LOTE alert on create (09).

## Transactions
`criar`, `atualizar`, `excluir`, `expirarVencidos`, `listarDisponiveis` are `@Transactional`.
