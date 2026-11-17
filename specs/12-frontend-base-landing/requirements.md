# 12 — Frontend base + landing · Requirements

HU covered: none directly (shared frontend base); landing presents the solution (prototipo.md §3.1). Decision 5: no login, "Você é:" selector.

## Acceptance criteria

1. **Given** the app is running, **when** `GET /` or `/index.html`, **then** 200 HTML containing "Rede de Prato".
2. **Given** the landing, **then** it has the sections of prototipo.md §3.1 (nav, hero, marquee, como funciona, quem ganha, placa, números, ofertantes, footer) with Portuguese text.
3. **Given** active pontos in the API, **then** the "Procure esta placa" card shows the first active ponto (name, address, days and hours); with none, it keeps the placeholder "[Endereço do ponto]".
4. **Given** any internal page, **then** `persona.js` renders the "Você é:" selector (beneficiários or ofertantes from the API), keeps the choice in `localStorage` (try/catch) and in the URL (`?beneficiario=` / `?ofertante=`), and shows "Olá, {nome}" with an avatar initial.
5. **Given** an API error, **then** `api.js` throws `{status, message, fields}` from the `ApiError` body so pages can show the Portuguese message.
6. Accessibility: focus ring `4px solid #D9532B`, real buttons/links, `prefers-reduced-motion` stops the marquee, contrast ≥ 4.5:1, works without Google Fonts (fallback fonts).

## Rules
- BR-12.1 colors/fonts/shapes exactly as the tokens in prototipo.md §1.
- BR-12.2 icons always next to a word; no emoji.
- BR-12.3 no external JS libraries (plain JS, same origin `/api`).
