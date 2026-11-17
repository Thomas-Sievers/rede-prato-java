# 07 — Reservas · Design

## Entity `Reserva` → `T_RP_RESERVA`

| Field | Column | Type |
| --- | --- | --- |
| idReserva | id_reserva | NUMBER identity PK (= número de retirada) |
| lote | id_lote | NUMBER FK not null |
| beneficiario | id_beneficiario | NUMBER FK not null |
| ponto | id_ponto | NUMBER FK nullable (null = retirada no ofertante) |
| dataReserva | data_reserva | DATE not null |
| dataRetirada | data_retirada | DATE |
| status | status | VARCHAR2(15) not null, `StatusReserva` (ATIVA, RETIRADA, CANCELADA) |

Oracle index `UX_RP_RESERVA_ATIVA_LOTE` (function-based, in Mariana's DDL) is not created on H2; the BO check covers H2.

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/reservas?idBeneficiario=&idOfertante=&status=&idLote= | — | `[ReservaResponse]` newest first | 200 |
| GET | /api/reservas/{id} | — | ReservaResponse (confirmação) | 200, 404 |
| POST | /api/reservas | `{idLote, idBeneficiario, idPonto?}` | ReservaResponse | 201, 400, 404, 409 |
| PUT | /api/reservas/{id} | `{idPonto}` | ReservaResponse | 200, 400, 404, 409 |
| PUT | /api/reservas/{id}/cancelar | — | ReservaResponse | 200, 404, 409 |
| DELETE | /api/reservas/{id} | — | — | 204, 404, 409 |

```json
// ReservaResponse (confirmation)
{ "idReserva": 12, "numeroRetirada": 12, "status": "ATIVA", "dataReserva": "2026-11-17T10:05:00",
  "dataRetirada": null,
  "idLote": 7, "descricaoLote": "Caixa de tomates maduros", "pesoKg": 8.5, "condicao": "MADURA",
  "modalidade": "VENDA", "preco": 12.9, "valorEstimado": 40.0, "pagamentoNaRetirada": true,
  "dataLimiteRetirada": "2026-11-17T18:00:00", "statusLote": "RESERVADO", "nomeCategoria": "Legumes",
  "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio", "telefoneOfertante": "11900000001",
  "enderecoOfertante": "Rua Manuel da Nóbrega, s/n - Banca 12 - Paraíso",
  "localRetirada": "PONTO", "idPonto": 1, "nomePonto": "Ponto Paraíso",
  "enderecoPonto": "Rua Tutóia, 300 - Paraíso", "diasFuncionamento": "TER e QUI",
  "horarioAbertura": "16:00", "horarioFechamento": "18:00",
  "idBeneficiario": 1, "nomeBeneficiario": "Rosana", "tipoBeneficiario": "FAMILIA", "qtdPessoas": 4 }
```

## Layers
- `ReservaDAO`: `filtrar(...)` with fetch joins; `buscarDetalhe(id)`; `existsByLote_IdLoteAndStatus`; `existsByLote_IdLoteAndStatusIn`; `findFirstByLote_IdLoteAndStatus`; `findByLote_IdLoteAndStatus` (list, for the cascade).
- `ReservaBO`: `criar`, `atualizar`, `cancelar`, `excluir`, `listar`, `buscar`, `find`; uses `LoteBO` (expiry, find), `LoteDAO` (save lote status), `BeneficiarioBO`, `PontoDistribuicaoBO`, `Clock`.
- `LoteBO` changes: `expirarVencidos` cancels the ATIVA reserva of RESERVADO lots; `excluir` refuses when a reserva ATIVA/RETIRADA exists and deletes CANCELADA reservas first.

## Transactions
`criar`, `cancelar`, `atualizar`, `excluir` are `@Transactional` (default rollback on RuntimeException): if saving the lote fails, the reserva insert is rolled back too.
