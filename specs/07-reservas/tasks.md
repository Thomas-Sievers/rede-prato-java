# 07 — Reservas · Tasks

- [x] Enum StatusReserva, entity Reserva
- [x] ReservaDAO
- [x] ReservaBO + tests (BR-07.1–07.13, every UC12 criterion)
- [x] DTOs, ReservaController
- [x] LoteBO: expiry cancels ATIVA reserva; delete checks + cascade of CANCELADA reservas (+ tests)
- [x] Integration test: reservar → some de disponíveis → segunda reserva 409 → cancelar → volta
