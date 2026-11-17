# 11 — Seed data · Requirements

HU covered: none directly — demo data for the deploy, the frontend and the presentation (all simulated).

## Acceptance criteria

1. **Given** the default (H2) profile with `app.seed.enabled=true`, **when** the app starts, **then** the database holds 6 categorias, 10 endereços, 5 ofertantes, 6 beneficiários, 3 pontos, 19 lotes covering every status, reservas in every status, destinações and alertas.
2. **Given** the `oracle` profile or `app.seed.enabled=false`, **then** nothing is inserted.
3. **Given** the seed already ran (categorias exist), **when** it runs again, **then** nothing is duplicated.
4. **Given** any start time, **then** all dates are relative to the Clock: DISPONIVEL/RESERVADO lots always have a deadline in the future (today 14h/18h or tomorrow 10h/12h; if today's hour already passed, now + 2h/3h), so the demo always looks "today".
5. **Given** the painel of "Banca do Seu Antônio", **then** it shows kg in more than one of the last 4 weeks.

## Business rules

- BR-11.1 only fictional names, fictional phones (`119000000xx`, `11300000xx`), generic Paraíso / Vila Mariana addresses, CEPs 04001-000–04110-000; no documents.
- BR-11.2 inserted through the DAOs (BO validations would reject past dates of the history).
- BR-11.3 every seeded row respects the domain rules (selo ⇔ data, DOACAO without preço, sum of destinos ≤ peso, one ATIVA reserva per lote, EXPIRADO only COMPOSTAGEM).
- BR-11.4 Sacolão Vila Mariana is inactive (shows logical delete).
- BR-11.5 one CANCELADA reserva in the history; 2 XEPA alerts of Banca do Seu Antônio today; one NOVO_LOTE alert per lote.
