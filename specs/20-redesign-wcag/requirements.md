# 20 — Redesign (type scale, 3D landing icons) + WCAG 2.2 AA fixes · Requirements

No new HU. Thomas approved a redesign on 17/11/2026. Source: `docs/redesign/README.md` (the spec) and the five `docs/redesign/*.dc.html` design files (reference markup only). Only the static frontend changes; backend, API calls and database stay as they are.

## Acceptance criteria

### Type scale (README §1)
1. **Given** any page, **then** sizes come from CSS variables in `css/app.css`: body 17px (line-height 1.5); secondary text/notes/hints 15px; landing hero H1 `clamp(36px, 5vw, 56px)`; page H1 `clamp(30px, 4vw, 40px)`; landing section H2 `clamp(30px, 4vw, 42px)`; card titles 19–21px; Caveat subtitle 26–30px; big buttons 16–17px and 52–56px tall (xepa/submit 60px); chips 15px and 48px tall; painel hero number `clamp(64px, 10vw, 104px)`; stat numbers 34px; +/- steppers 56px (retiradas 52px).
2. **Given** any interactive control, **then** its touch target is at least 44 × 44 px.

### 3D icons, landing only (README §2)
3. **Given** the landing hero, **then** it shows `img/caixa-laranjas.png` with alt "Caixa de feira de madeira cheia de laranjas", the `.crate` mask (outer 8% fades) and a rotated "DE GRAÇA" `<span>` over it; the hero check marks stay flat green strokes.
4. **Given** the landing marquee, "Como funciona" steps 1–2 and the four "Quem ganha" rows, **then** they use the 3D-style SVGs whose gradients (`g-red`, `g-orange`, `g-yellow`, `g-green`, `g-purple`, `g-skin`, `g-skin2`, `g-leaf`, `g-carrot`, `g-wood`, `g-dark`, `g-cream`, `g-soil`) and `soft` filter are defined once in a hidden `<svg>`; step 3 keeps the placa logo; "para mercados e feiras" uses the dark 3D ball check. Other pages keep their flat food art.

### WCAG fixes (README §3)
5. **A1 — Given** the landing marquee, **when** the user activates the "Pausar animação" button, **then** the strip stops, the button says "Continuar animação" and `aria-pressed="true"`; activating it again resumes. The `prefers-reduced-motion` rule still stops the animation.
6. **A2 — Given** any focusable element on any background, **when** it gets keyboard focus, **then** a two-tone ring shows: `outline: 3px solid #1A3A2A; outline-offset: 3px; box-shadow: 0 0 0 3px #FFF8EA`.
7. **A3 — Given** a 320px wide viewport, **when** index, registrar or painel loads, **then** there is no horizontal scroll (stepper and rows wrap, chart bars shrink with short labels "Sem. 1", hero image `max-width: 100%`, no fixed width over 320px).
8. **A4 — Given** the landing, **then** the hero image has alt text and every decorative SVG has `aria-hidden="true"`.
9. **A5 — Given** any of the 5 pages, **then** the first element in `<body>` is `<a class="skip" href="#conteudo">Pular para o conteúdo</a>` (hidden until focused) and the content is in `<main id="conteudo" tabindex="-1">`.
10. **A6 — Given** the landing menu, **then** each link is `inline-flex` with `min-height: 44px`.
11. **A7 — Given** any page, **then** it has exactly one `<h1>` and headings do not skip levels: registrar has a visually hidden h1 "Registrar sobra e avisar xepa" with h2 sections ("Fim de feira?", "Registrar sobra", "Meus lotes") and h3 inside; retiradas has an h2 "Reservas da sua banca" before the h3 cards.
12. **A8 — Given** the painel, **then** the weekly bars container has `role="img"` and an `aria-label` listing every week's kg, and the comida/adubo bar has `role="img"` with an `aria-label` giving both percentages.
13. **A9 — Given** the lotes list, **then** each "Reservar" button has `aria-label="Reservar <descrição>, <peso> kg"`, and the pickup-place radios keep their `aria-label` "No ponto da placa: …".

### Behaviour
14. **Given** any page, **then** every current API call, the "Você é:" selector and all behaviour work as before; `./mvnw test` stays green.

## Business rules
- BR-20.1 Frontend only: no change to Java code under `src/main/java`, to `db/`, to endpoints or to messages returned by the API.
- BR-20.2 The `.dc.html` files are reference markup: their canvas format and runtime (`<x-dc>`, `{{holes}}`, `<sc-for>`, `<sc-if>`, `DCLogic`) and sample data are not copied into the app.
- BR-20.3 If the design conflicts with current behaviour, the behaviour wins and the conflict is written in `DECISIONS.md`.
