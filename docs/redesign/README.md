# Redesign — smaller type, 3D landing icons, WCAG 2.2 AA fixes

Approved by Thomas on 17/11/2026. Design canvas: https://claude.ai/artifact/D6YH5viw14NnCxAqHGYVYr

The five `*.dc.html` files in this folder are the **approved design**, exported from the canvas. They are reference markup, not app code: they use the canvas format (`<x-dc>`, `<helmet>`, `{{holes}}`, `<sc-for>`, `<sc-if>`, a `DCLogic` class) and hard-coded sample data. Re-implement the look in the existing static pages (`src/main/resources/static/`) and keep all current API wiring and behavior. Asset paths in them: `assets/caixa-laranjas.png` (new, copy to `static/img/`), `assets/logo.png` and `assets/logo_light.png` (already in `static/img/`).

| Design file | App page |
| --- | --- |
| `Main.dc.html` | `index.html` |
| `Lotes.dc.html` | `lotes.html` |
| `RegistrarLote.dc.html` | `registrar.html` |
| `Retirada.dc.html` | `retiradas.html` |
| `Painel.dc.html` | `painel.html` |

## 1. Type scale (all pages)

| Element | Before | After |
| --- | --- | --- |
| Body text | 20px | 17px (line-height 1.5) |
| Secondary text, notes, hints | 17–18px | 15px |
| Landing hero H1 | 84px | `clamp(36px, 5vw, 56px)` |
| Page H1 (other pages) | 52–56px | `clamp(30px, 4vw, 40px)` |
| Landing section H2 | 60px | `clamp(30px, 4vw, 42px)` |
| Card titles | 26–28px | 19–21px |
| Handwritten subtitle (Caveat) | 38–40px | 26–30px |
| Big buttons | 20–26px, 64–84px tall | 16–17px, 52–56px tall (xepa/submit 60px) |
| Chips / filters | 19px, 60–64px tall | 15px, 48px tall |
| Big numbers (painel hero) | 150px | `clamp(64px, 10vw, 104px)` |
| Stat numbers | 48px | 34px |
| +/- stepper buttons | 76px | 56px (retiradas 52px) |

Touch targets stay ≥ 44px everywhere.

## 2. 3D icons — landing page only

- Hero: replace the drawn crate SVG with `img/caixa-laranjas.png` (alt "Caixa de feira de madeira cheia de laranjas"), no border radius, with the `.crate` CSS mask from `Main.dc.html` (fades the outer 8% into the page) and the rotated "DE GRAÇA" tag as an HTML `<span>` over it.
- Marquee foods, the three "Como funciona" step pictures (except step 3, which keeps the placa logo) and the four "Quem ganha" pictures: use the 3D-style SVGs from `Main.dc.html` (radial/linear gradients `g-red`, `g-orange`, `g-yellow`, `g-green`, `g-purple`, `g-skin`, `g-skin2`, `g-leaf`, `g-carrot`, `g-wood`, `g-dark`, `g-cream`, `g-soil` and the `soft` blur filter, defined once in a hidden `<svg>` at the top of the page).
- The hero check marks ("Não precisa de documento" etc.) stay **flat** (simple green stroke check) — Thomas's decision.
- The "para mercados e feiras" check marks use the dark 3D ball check from the design.
- Other pages keep their flat food illustrations.

## 3. WCAG 2.2 AA fixes (all must be implemented)

Audit on 17/11: axe-core 4.10 found 0 violations; these came from manual checks axe does not cover, plus axe best-practice.

| # | Criterion | Problem found | Fix |
| --- | --- | --- | --- |
| A1 | 2.2.2 Pause, Stop, Hide | Landing marquee moves forever; only hover pauses it | Add a real `<button>` next to the strip: "Pausar animação" / "Continuar animação", `aria-pressed`, toggles `animation-play-state`. Keep the `prefers-reduced-motion` rule |
| A2 | 1.4.11 / 2.4.7 Focus visible | Orange focus ring is 2.2:1 on leaf green and 1.3:1 on the xepa orange | Global `:focus-visible { outline: 3px solid #1A3A2A; outline-offset: 3px; box-shadow: 0 0 0 3px #FFF8EA; }` (two-tone ring visible on any background) |
| A3 | 1.4.10 Reflow | Horizontal scroll at 320px: index (422px), registrar (440px, weight stepper), painel (438px, weekly chart) | Stepper and rows `flex-wrap: wrap`; chart bars `flex: 1 1 0; min-width: 0`, smaller gaps, short labels ("Sem. 1"); hero image `max-width: 100%`; no fixed widths wider than 320px |
| A4 | 1.1.1 Non-text content | Hero picture (role=img SVG) had no accessible name | The new hero `<img>` has alt text; decorative SVGs get `aria-hidden="true"` |
| A5 | 2.4.1 Bypass blocks | No skip link | First element in `<body>` on every page: `<a class="skip" href="#conteudo">Pular para o conteúdo</a>` (hidden until focused) and `<main id="conteudo" tabindex="-1">` |
| A6 | 2.5.8 Target size | Landing menu links were 22px tall | Nav links `display: inline-flex; min-height: 44px` |
| A7 | 1.3.1 / heading order | registrar and retiradas jump h1 → h3 | registrar: visually hidden `<h1>` "Registrar sobra e avisar xepa", then h2 sections ("Fim de feira?", "Registrar sobra", "Meus lotes"), h3 inside. retiradas: add h2 "Reservas da sua banca" before the h3 cards |
| A8 | 1.1.1 charts | Painel bars are only visual | Bars container `role="img"` with an `aria-label` listing every week's kg; the comida/adubo bar `role="img"` with its percentages |
| A9 | 4.1.2 Name | "Reservar" buttons repeat the same name; pickup-place radios | `aria-label="Reservar <descrição>, <peso> kg"`; radios keep `aria-label` "No ponto da placa: …" |
