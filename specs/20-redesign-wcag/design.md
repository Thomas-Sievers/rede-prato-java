# 20 — Redesign + WCAG · Design

## CSS (`static/css/app.css`)
- **Tokens:** `:root` gets type-scale variables (`--fs-body`, `--fs-small`, `--fs-hero`, `--fs-h1`, `--fs-h2-landing`, `--fs-h2`, `--fs-card`, `--fs-hand`, `--fs-btn`, `--btn-h`, `--btn-h-big`, `--fs-chip`, `--chip-h`, `--fs-bignum`, `--fs-stat`, `--stepper`, `--stepper-sm`). Every rule reads from them; page `<style>` blocks and inline sizes that fought the scale are removed.
- **Focus:** a global two-tone `:focus-visible` rule replaces the orange `--foco` outline (A2). `#conteudo:focus` has no ring, because it is only focused by the skip link.
- **Skip link:** `.skip` sits off-screen and moves to `left: 16px` on `:focus` (A5).
- **Reflow (A3):**
  - Grid and flex children get `min-width: 0`, and headings get `overflow-wrap: break-word`.
  - `.stepper` wraps, and `.big-num` drops to a 110px minimum.
  - The painel chart is flex with `flex: 1 1 0; min-width: 0` and short labels.
  - The landing "Quem ganha" rows are flex-wrap instead of a 3-column grid.
- **Landing (`body.landing`):**
  - Bigger section h2.
  - `.crate` mask, copied from the design.
  - `.tag-graca` rotated span.
  - Marquee: `.marquee-bar` holds the track and the pause button. `.marquee.paused .track { animation-play-state: paused }`, and the `prefers-reduced-motion` rule is kept.
  - Nav links are `inline-flex; min-height: 44px` (A6).
- **App header:**
  - Light logo (`logo_light.png`) with no cream box.
  - Nav pills use body font, 15px, not uppercase.
  - "Você é:" select at 15px, max 220px; the 375px wrap from D-14 is kept.

## Pages
| Page | Changes |
| --- | --- |
| all 5 | `<a class="skip" href="#conteudo">` first in `<body>`; `<main id="conteudo" tabindex="-1">`; exactly one `<h1>`. |
| index | Hidden `<svg>` with the gradient `<defs>`. Hero `<img class="crate">` + `<span class="tag-graca">`. Static 3D marquee (two copies, `aria-hidden`) + `<button id="marquee-toggle" aria-pressed>` driven by inline JS. 3D SVGs for steps 1–2 and the 4 "Quem ganha" rows. Dark-ball checks in "para mercados e feiras". Footer uses the light logo. The inline script keeps the icon slots and the `/api/pontos` call; the marquee is no longer built from `RP.illustrationFor`. |
| lotes | Card title becomes h2 (h1 → h2, no skip). `Reservar` gets `aria-label="Reservar <descrição>, <peso>"` instead of the sr-only span (A9). Radios unchanged (already labelled in feature 19). |
| registrar | `<h1 class="sr-only">Registrar sobra e avisar xepa</h1>`; the form title `#form-title` becomes h2 (JS still switches it to "Mudar lote"). Submit and xepa buttons use `.btn-big`. Weight stepper wraps. Category tiles use a 120px minimum. "Mudar"/"Cancelar" get `aria-label` with the lote name. |
| retiradas | `<h2 id="lista-titulo">Reservas da sua banca</h2>` before the cards (A7). "Já buscou" gets `aria-label="Já buscou: <descrição>, número <n>"`. Steppers are 52px. |
| painel | The hero text becomes an h2. Bars use flex, labels "Sem. 1/2/3/Esta semana", and `aria-label` "Quilos salvos por semana: semana 1, 0 kg; …". `#split` gets `role="img"` + `aria-label` "x% virou comida e y% virou adubo." (A8). The admin heading becomes an h2. The selo card gets an h2 "você é parceiro". No fixed `min-width: 260px`. |

## Tests (`FrontendPagesTest`, MockMvc)
- Parameterized over the 5 pages:
  - `lang="pt-BR"`
  - the skip link
  - `<main id="conteudo"`
  - exactly one `<h1` (counted)
- Index: `id="marquee-toggle"` with `aria-pressed="false"` and "Pausar animação"; the hero `alt`.
- `css/app.css`: a `:focus-visible { … outline … box-shadow … }` rule (regex).
- `/img/caixa-laranjas.png` is served.
