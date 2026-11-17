# 07 — Reservas · Requirements

HU covered: **HU10** (reservar lote e escolher onde retirar — UC12), **HU12** (cancelar reserva — UC14), **HU11** (lote de venda mostra preço; pagamento na retirada — UC13).

## Acceptance criteria (UC12 criteria included)

1. **Given** an active beneficiário and a DISPONIVEL lote within the deadline, **when** `POST /api/reservas {idLote, idBeneficiario, idPonto?}`, **then** 201 with the confirmation DTO; the reserva is ATIVA, `dataReserva=now`, and the lote becomes RESERVADO (same transaction) and leaves `GET /api/lotes/disponiveis`.
2. **Given** `idPonto` absent, **then** `localRetirada="OFERTANTE"` and the confirmation shows the ofertante address; with a ponto, `localRetirada="PONTO"` and the ponto name, address, days and hours.
3. **Given** a lote already RESERVADO, **when** a second reserva is attempted, **then** 409 `"Este lote já foi reservado por outra pessoa."`.
4. **Given** a lote past its deadline, **when** reserving, **then** 400 `"O prazo de retirada deste lote já passou."` and nothing is saved.
5. **Given** an inactive beneficiário, **then** 400 `"Beneficiário inativo não pode reservar lotes."`; unknown → 404.
6. **Given** an inactive ponto, **then** 400 `"Este ponto de distribuição não está ativo."`; unknown → 404.
7. **Given** a VENDA lote, **then** the confirmation shows `preco` and `pagamentoNaRetirada=true` (no payment in the prototype).
8. **Given** an ATIVA reserva, **when** `PUT /api/reservas/{id}/cancelar`, **then** 200, reserva CANCELADA, lote back to DISPONIVEL (or EXPIRADO if past the deadline).
9. **Given** a non-ATIVA reserva, **when** cancelling, **then** 409 `"Só é possível cancelar uma reserva ativa."`.
10. **Given** an ATIVA reserva, **when** `PUT /api/reservas/{id} {idPonto}`, **then** the pickup place changes (null = ofertante); non-ATIVA → 409 `"Só é possível alterar uma reserva ativa."`.
11. **Given** a CANCELADA reserva, **when** `DELETE`, **then** 204; otherwise 409 `"Só é possível excluir reservas canceladas. Cancele a reserva primeiro."`.
12. **Given** `GET /api/reservas?idBeneficiario=&idOfertante=&status=&idLote=`, **then** filtered list, newest first.
13. **Given** a RESERVADO lote whose deadline passed, **when** expiry runs, **then** lote EXPIRADO and its ATIVA reserva CANCELADA (BR-04.13, D-7).
14. **Given** a lote with a reserva ATIVA or RETIRADA, **when** `DELETE /api/lotes/{id}`, **then** 409; with only CANCELADA reservas, they are deleted with the lote (decisão 6).

## Business rules

- BR-07.1 beneficiário must exist (404) and be ativo (400).
- BR-07.2 lote must exist (404).
- BR-07.3 expiry runs first; lote EXPIRADO or deadline ≤ now → 400.
- BR-07.4 lote must be DISPONIVEL, else 409.
- BR-07.5 only one ATIVA reserva per lote (DAO check → 409 `"Este lote já tem uma reserva ativa."`; Oracle unique index → ORA-00001 → 409 by the handler).
- BR-07.6 ponto optional; when given, must exist (404) and be ativo (400).
- BR-07.7 the reserva takes the whole lote (no kg field).
- BR-07.8 reserva + lote→RESERVADO in one `@Transactional` method.
- BR-07.9 cancel only ATIVA; lote RESERVADO → DISPONIVEL, or EXPIRADO when the deadline passed.
- BR-07.10 status/dates never set by the client; `dataReserva = now`.
- BR-07.11 VENDA lots expose `preco` and `pagamentoNaRetirada=true`.
- BR-07.12 update (PUT) only while ATIVA, only `idPonto` changes.
- BR-07.13 delete only CANCELADA.
