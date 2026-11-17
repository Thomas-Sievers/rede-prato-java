# 07 — Reservas · Tasks

- [ ] Enum StatusReserva, entity Reserva
- [ ] ReservaDAO
- [ ] ReservaBO + tests (BR-07.1–07.13, every UC12 criterion)
- [ ] DTOs, ReservaController
- [ ] LoteBO: expiry cancels ATIVA reserva; delete checks + cascade of CANCELADA reservas (+ tests)
- [ ] Integration test: reservar → some de disponíveis → segunda reserva 409 → cancelar → volta
