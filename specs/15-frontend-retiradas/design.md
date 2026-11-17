# 15 — Retiradas + destino (frontend) · Design

Files: `static/retiradas.html`, `static/js/retiradas.js`.

| UI | API |
| --- | --- |
| List | `GET /api/reservas?idOfertante=` |
| Destination badges | `GET /api/destinacoes` (grouped by idLote, filtered by idOfertante) |
| Conferir | `GET /api/reservas/{id}` |
| Já buscou | `PUT /api/reservas/{id}/retirada` |
| Salvar destino | `POST /api/lotes/{id}/destinacoes {consumoKg, compostagemKg}` |
| Venceu sem retirada | `GET /api/lotes?idOfertante=&status=EXPIRADO` + `POST /api/lotes/{id}/destinacoes {compostagemKg}` |

Steps of the comida counter: 1 kg (or the remaining fraction), clamped to [0, peso − already registered].
Test: page served with "Retiradas".
