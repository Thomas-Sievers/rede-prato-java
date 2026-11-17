# 14 — Registrar lote + xepa (frontend) · Design

Files: `static/registrar.html`, `static/js/registrar.js`.

| UI | API |
| --- | --- |
| Xepa button | `POST /api/alertas/xepa {idOfertante}` |
| Desfazer | `DELETE /api/alertas/{id}` for each alert returned |
| Category tiles | `GET /api/categorias` |
| Submit | `POST /api/lotes` (or `PUT /api/lotes/{id}` in edit mode) |
| Meus lotes | `GET /api/lotes?idOfertante=` |
| Cancelar | `DELETE /api/lotes/{id}` |

Prazo chips: "Hoje até 14h" (today 14:00), "Hoje até 18h" (today 18:00), "Amanhã cedo" (tomorrow 10:00); a chip whose time already passed is disabled. Peso: integer steps of 1 kg in the UI (min 1). VENDA shows "Preço com desconto (R$)" → `preco` and "Preço normal (R$)" → `valorEstimado`; DOACAO shows optional "Valor estimado (R$)".

Test: page served with "Registrar sobra".
