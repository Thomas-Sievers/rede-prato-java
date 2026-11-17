# 04 — Lotes · Tasks

- [ ] Enums CondicaoLote, ModalidadeLote, StatusLote; entity Lote
- [ ] LoteDAO
- [ ] LoteBO + tests (BR-04.1–04.14, transitions DISPONIVEL/RESERVADO → EXPIRADO)
- [ ] DTOs, LoteController
- [ ] ExpiracaoJob (scheduler)
- [ ] Wire BR-02.4 in CategoriaAlimentoBO + test
- [ ] Integration test: create + GET disponíveis + H2 keeps hours/minutes
- [ ] (07) cancel ATIVA reserva when RESERVADO lote expires; delete cascade of CANCELADA reservas
- [ ] (08) delete refused when destinação exists
- [ ] (09) NOVO_LOTE alert on create; delete cascade of alertas
