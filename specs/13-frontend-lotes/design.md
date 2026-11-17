# 13 — Lotes disponíveis + reserva (frontend) · Design

Files: `static/lotes.html`, `static/js/lotes.js` (uses `api.js`, `persona.js`).

| UI | API |
| --- | --- |
| Xepa banner / novidades | `GET /api/alertas/recentes?horas=24` |
| Category chips | `GET /api/categorias` |
| Bairro select | `GET /api/ofertantes?ativo=true` (distinct bairros) |
| Cards | `GET /api/lotes/disponiveis?idCategoria=&modalidade=&bairro=` |
| Pickup choice | `GET /api/pontos?ativo=true` |
| Confirm | `POST /api/reservas {idLote, idBeneficiario, idPonto?}` |
| Sacola | `GET /api/reservas?idBeneficiario=&status=ATIVA` |
| Cancel | `PUT /api/reservas/{id}/cancelar` |

Dialog: one `.modal-backdrop` reused for reservation and "Minhas reservas"; Esc and the "Fechar" button close it; focus moves into the dialog and back to the trigger.

Test: `FrontendPagesTest` — `/lotes.html` served with "Escolha sua comida".
