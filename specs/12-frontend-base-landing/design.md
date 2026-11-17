# 12 — Frontend base + landing · Design

Static files in `src/main/resources/static/` served by Spring Boot (same origin as the API).

| File | Content |
| --- | --- |
| `css/app.css` | tokens as CSS variables, fonts with fallbacks, components (`.btn`, `.btn-dark`, `.btn-line`, `.chip[aria-pressed]`, `.card`, `.tile`, `.step`, `.num`, `.badge-*`, `.modal`), header/nav, focus ring, responsive grid (2 cols < 1000px, 1 col < 640px), reduced motion |
| `js/api.js` | `api.get/post/put/del`, error → `{status, message, fields}`; `fmtKg`, `fmtBRL`, `fmtPrazo`, `fmtHora`, `illustrationFor(categoria, descricao)` (inline SVG), `esc()` (HTML escaping), `icon(name)` |
| `js/persona.js` | `Persona.init({kind: 'beneficiario'|'ofertante', onChange})`; header selector; localStorage key `rp.<kind>`; URL param |
| `index.html` | landing (§3.1); loads `/api/pontos` for the placa card |
| `img/logo.png`, `img/logo_light.png` | copied from `docs/prototipo/` |

APIs used: `GET /api/pontos`.

Test: `FrontendPagesTest` (`@SpringBootTest` + MockMvc) — `GET /` and `/index.html` return 200 HTML with "Rede de Prato"; css/js assets are served.
