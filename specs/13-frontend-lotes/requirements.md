# 13 — Lotes disponíveis + reserva (frontend) · Requirements

HU covered: **HU05** (aviso de lote novo / xepa), **HU09** (consultar lotes com filtros), **HU10** (reservar e escolher onde retirar), **HU12** (cancelar reserva). Screen: prototipo.md §3.2, `docs/prototipo/1-lotes.png`.

## Acceptance criteria

1. **Given** recent XEPA alerts, **then** an orange banner (`role="status"`) says "**Xepa agora!** A {ofertante} tem {descrições} até as {hora}."; recent NOVO_LOTE alerts show a lighter notice; nothing when there are none.
2. **Given** the page, **then** cards show only `GET /api/lotes/disponiveis` lots with illustration, "{peso} kg", title, "DE GRAÇA" or "R$ x ~~era R$ y~~", "{ofertante} · {bairro}", "Buscar até {hoje|amanhã}, {h}h" and a **Reservar** button.
3. **Given** the chips (category, Todos/De graça/Com desconto) and the bairro select, **then** the list reloads with the matching query params; chips use `aria-pressed`.
4. **Given** no lot for the filter, **then** "Nada deste tipo agora. Toque em **Tudo** para ver o resto."; with no lots at all, the pontos and horários are listed.
5. **Given** Reservar, **then** a dialog asks "Onde você quer buscar?" (radio cards: "No próprio lugar" or each active ponto); the confirm button stays disabled ("Escolha onde buscar") until a choice; **Confirmar reserva** calls `POST /api/reservas`.
6. **Given** success, **then** "Reservado!" with "Seu número" = idReserva, place, deadline, and the simulated WhatsApp note; the lot leaves the list.
7. **Given** 409/400, **then** the API message is shown ("Alguém já reservou este alimento. Escolha outro." for 409) and the list refreshes.
8. **Given** the "Sacola" badge, **then** it shows the current beneficiário's ATIVA reservas count and opens "Minhas reservas" with **Cancelar reserva** (`PUT /api/reservas/{id}/cancelar`).
