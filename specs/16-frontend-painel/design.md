# 16 — Painel de impacto + selo (frontend) · Design

Files: `static/painel.html`, `static/js/painel.js`.

| UI | API |
| --- | --- |
| Hero, cards, weekly bars, split | `GET /api/impacto/ofertantes/{id}?mes=` |
| Rede block | `GET /api/impacto/rede?mes=` |
| Example-data badge | `GET /api/health` (`profile != oracle`) |
| Admin: conceder selo | `GET /api/ofertantes?ativo=true`, `PUT /api/ofertantes/{id}/selo` |

Selo download: SVG string built in JS → `Blob('image/svg+xml')` → temporary `<a download="selo-rede-de-prato.svg">`.
Test: page served with "Meu impacto".
