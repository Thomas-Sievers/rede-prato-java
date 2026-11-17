<!-- Source: Claude Project doc "Rede de Prato — Protótipo" (17/11/2026) + the 5 artboards of the design canvas (https://claude.ai/artifact/FRWYgLp56b2AG8ofAFJ23y), transcribed into a build spec for the frontend. Screenshots, if exported, live in docs/prototipo/*.png. -->

# Rede de Prato — Prototype (frontend build spec)

The frontend must follow these 5 screens. All user-facing text is **Portuguese**, exactly as below where quoted. The prototype used hard-coded sample data; the real frontend replaces it with API data.

## 1. Design tokens (use these exact values)

| Token | Value | Use |
| --- | --- | --- |
| `--verde-escuro` | `#1A3A2A` | Text, header/footer background, dark buttons, 3px borders |
| `--verde-folha` | `#9CCC6C` | Highlights, "DE GRAÇA" tag, active nav item, Selo background |
| `--verde-medio` | `#2F6B2B` | Confirm buttons, handwritten subtitles, selected-state border |
| `--verde-claro` | `#DCEFC7` | Selected tile/option background |
| `--creme` | `#FAEFD9` | Page background |
| `--creme-claro` | `#FFF8EA` | Cards, inputs |
| `--laranja-xepa` | `#C2410C` | Xepa alerts, discounted price, impact numbers |
| `--amarelo` | `#F2C230` | "Dados de exemplo" badge, "Falta dizer o destino" badge, ofertante avatar |
| `--marrom-adubo` | `#8C5C2B` | Compost (adubo) bar segment and icon |
| `--cinza-texto` | `#4A5548` | Secondary text (meets 4.5:1 on cream) |
| `--desabilitado` | `#E4DCCB` | Disabled button background |
| focus ring | `outline: 4px solid #D9532B; outline-offset: 3px` | All `:focus-visible` |

**Fonts (Google Fonts):** `Archivo` 700/900 for headings, buttons and big numbers (headings UPPERCASE); `Atkinson Hyperlegible` 400/700 for body (base 20px, line-height 1.45); `Caveat` 700 for the small handwritten subtitle above each H1 (e.g. "perto de você hoje"), color `--verde-medio`. Always add a sans-serif fallback (the app must work offline from Google Fonts).

**Shapes:** pill buttons (`border-radius: 999px`), cards `border-radius: 24–32px` with `3px solid #1A3A2A` border; selected state = `5px solid #2F6B2B` border + `#DCEFC7` background.

**Sizes:** big buttons min-height 64–84px, font 20–26px; chips min-height 60px; +/- round buttons 60–76px; touch targets ≥ 44px everywhere. Content `max-width: 1280px`, padding `32px 40px`. Responsive: grids drop to 2 columns under 1000px and 1 column under 640px.

**Icons:** inline stroke SVG (stroke-width ~2.2, round caps) **always next to a word** — never icon-only without `aria-label`, never emoji. Food illustrations are simple flat SVGs (tomato, lettuce, carrot, banana, apple, orange) on a pastel tile; pick the illustration by category (frutas → maçã/banana/laranja, verduras → alface, legumes → tomate/cenoura, outros → caixa).

**Assets:** logo (`logo.png`) and the "placa" illustration (`placa.png`) — see `docs/prototipo/` if Thomas exported them; otherwise use a text wordmark "REDE DE PRATO" and a simple SVG sign (plate + fork + knife).

**Accessibility:** real `<button>`, `<a href>`, `<input>` + `<label>`; `aria-pressed` on toggle chips; `role="radiogroup"`/`role="radio"` for the pickup-place choice; `role="status"` on confirmations; `aria-live="polite"` on +/- counters; `prefers-reduced-motion` stops the marquee.

## 2. Common layout

- **Header (internal screens):** dark green bar; left = logo + "REDE DE PRATO" (link to landing); right = **"Você é:" selector** (decision 5: no login) showing the current persona with a round avatar initial.
  - Beneficiary screens (Lotes): selector lists active beneficiários (e.g. "Rosana"). Header greeting "Olá, **{nome}**".
  - Ofertante screens (Registrar, Retiradas, Painel): nav pills **"Registrar sobra" · "Retiradas" · "Meu impacto"** (active = `--verde-folha` background) + selector of active ofertantes (e.g. "Banca do Seu Antônio").
  - The chosen persona id is kept in `localStorage` (wrapped in try/catch) and in the URL (`?ofertante=ID` / `?beneficiario=ID`).
- **H1 block:** Caveat subtitle + Archivo 900 uppercase title (52–56px).

## 3. Screens

### 3.1 Landing — `index.html` (prototype `Main.dc.html`)

Sections, top to bottom:
1. **Nav:** logo; links "Como funciona" (#como), "Onde buscar" (#pontos), "Mercados e feiras" (#ofertantes); dark button **"Quero comida"** → lotes.
2. **Hero:** subtitle "comida boa não é lixo"; H1 (84px) "Comida boa perto de você. ~~De graça~~ ou bem barata." with "De graça" on a `--verde-folha` highlight; text "Mercados e feiras do Paraíso avisam quando sobra fruta, verdura e legume. Você reserva e busca perto de casa."; two buttons **"Quero pegar comida"** (dark, → lotes) and **"Tenho comida sobrando"** (outline, → registrar); checklist "Não precisa de documento" · "Funciona no WhatsApp" · "Pontos com placa perto de casa"; right side: illustration of a crate full of vegetables on a green circle with a "DE GRAÇA" tag.
3. **Marquee:** dark band scrolling "TOMATE · CENOURA · ALFACE · BANANA · MAÇÃ · LARANJA · BETERRABA" with small food icons (36s loop, pauses on hover, off with reduced motion).
4. **Como funciona (#como):** "como funciona" / "Três passos. Só isso." — 3 cards numbered 1–3: "O mercado ou a feira avisa" (Sobrou comida boa? Eles colocam na Rede de Prato.), "Você reserva" (Escolhe o que quer no celular ou pelo WhatsApp. Fica guardado para você.), "Busca no ponto da placa" (Vá até o ponto com esta placa, no dia e na hora marcados.).
5. **Quem ganha (dark section):** "todo mundo ganha" / "A comida **chega** em quem precisa" — numbered rows 01 Famílias, 02 Supermercados, 03 Feirantes, 04 O bairro, each with one line of text and an illustration.
6. **Placa (#pontos):** "sem celular? sem problema" / "Procure esta placa" — placa image; card with the first active **ponto de distribuição from the API** (name, address, dias + horário); dark box "Pouca internet? Mande **OI** no WhatsApp **[NÚMERO DA REDE]**" (keep the placeholder).
7. **Números:** "por que isso importa" / "Tem comida indo para o lixo" — 3 stats in orange: "33 mil t" (Fonte: Jornal da USP (2018)), "R$ 700/dia" (Fonte: entrevista da equipe (15/11/2026)), "968 feiras" (Fonte: Prefeitura de SP (2025)). Static text.
8. **Ofertantes (#ofertantes, green section):** "para mercados e feiras" / "Sobrou? A gente leva até quem precisa." — 3 checkmarks; buttons "Registrar sobra" and "Ver meu impacto"; round Selo badge "PARCEIRO / REDE DE PRATO".
9. **Footer:** logo, "ONG · Paraíso, São Paulo"; "Protótipo acadêmico · FIAP Global Solution 2026 · Equipe Luxor Obelisc (18). ODS 2, 11 e 12."

### 3.2 Lotes disponíveis + reserva — `lotes.html` (HU05, HU09, HU10, HU12)

- **Xepa banner** (orange, `role="status"`, bell icon): "**Xepa agora!** A {ofertante} tem {descrições} até as {hora}." — built from the most recent XEPA alerts (`GET /api/alertas?tipo=XEPA&recentes=true` or equivalent). Also show NOVO_LOTE alerts as a lighter notice (HU05). Hidden when there are none.
- H1: "perto de você hoje" / **"Escolha sua comida"**; right badge "Sacola: vazia / 1 reserva / N reservas" = the current beneficiary's ATIVA reservations (click opens "Minhas reservas" with **"Cancelar reserva"** per item → HU12).
- **Filters** (chips, `aria-pressed`): category "Tudo · Frutas · Verduras · Legumes" (from `GET /api/categorias`), price "Todos · De graça · Com desconto" (modalidade), and bairro (select, from the ofertantes' addresses). Filters map to query params of `GET /api/lotes/disponiveis`.
- **Cards grid** (3/2/1 columns): pastel illustration tile with "{peso} kg" pill; title = descrição (uppercase); "DE GRAÇA" tag (DOACAO) or "**R$ 3,00** ~~era R$ 9,00~~" (VENDA: preco, and valorEstimado as "era" when present); location line "{ofertante} · {bairro}"; clock line "Buscar até {hoje|amanhã}, {HH}h"; big button **"Reservar"**.
- Empty state: dashed box "Nada deste tipo agora. Toque em **Tudo** para ver o resto." — and when there are no lots at all, list the pontos and horários (UC12 alt flow).
- **Reservation modal** (`role="dialog"`): "você escolheu" / "{DESCRIÇÃO} · {peso} kg" / value line; "Onde você quer buscar?" radio cards: **"No próprio lugar"** ({ofertante} · {endereço}) or one card per active ponto **"No ponto da placa"** ({ponto} · {dias}, {abertura} às {fechamento}); disabled button "Escolha onde buscar" until a choice is made, then **"Confirmar reserva"**; link "Escolher outro alimento"; close button (aria-label "Fechar").
- **Success state:** green check, **"Reservado!"**, dark box "Seu número" + **{idReserva}** (big, letter-spaced) + "Mostre este número na hora de buscar", place + "Buscar até …", note "Também mandamos tudo no seu WhatsApp." (simulated), button "Ver mais comida".
- **Error state:** 409 (already reserved) → "Alguém já reservou este alimento. Escolha outro." and refresh the list; 400 (expired) → "O prazo deste alimento acabou." Show the API's `message`.

### 3.3 Registrar lote + xepa — `registrar.html` (HU02, HU03, HU04)

- **Xepa block** (orange section): stall illustration; H1 "Fim de feira?"; "Avise a xepa com **um toque**. As famílias perto recebem o aviso na hora."; button **"Avisar xepa agora"** → sends XEPA for all DISPONIVEL lots of the current ofertante (optionally a checklist to deselect lots; zero lots → message "Você não tem comida registrada agora. Registre abaixo."). Success: "**Pronto!** Xepa avisada para {n} lotes até as {hora}." with link "Desfazer" (deletes the alerts just created).
- "ou conte o que sobrou" / **"Registrar sobra"** — a 7-step form, each step a numbered fieldset (round dark number):
  1. **"O que é?"** — category tiles with illustrations (Frutas, Verduras, Legumes, Outros… from the API) + text input "Qual alimento? (pode falar no microfone do teclado)" → `descricao`.
  2. **"Quanto pesa, mais ou menos?"** — big +/- buttons (aria-labels "Menos 1 quilo"/"Mais 1 quilo"), number in 64px "{n} kg", hint "Uma caixa de feira cheia tem uns 20 kg." → `pesoKg` (min 1 in the UI, >0 in the API).
  3. **"Como está?"** — three option cards: "Boa · Bonita e firme" (BOA), "Madura · Comer logo" (MADURA), "Feia, mas boa · Machucada por fora" (CONSUMO_IMEDIATO).
  4. **"Doar ou vender barato?"** — "Doar · Vai para a ONG e para as famílias" (DOACAO) / "Vender barato · Você recupera uma parte" (VENDA). When VENDA: inputs "Preço com desconto (R$)" → `preco` and "Preço normal (R$)" → `valorEstimado`. (Optional field "Valor estimado (R$)" also for DOACAO, for supermarkets.)
  5. **"Até quando pode buscar?"** — chips "Hoje até 14h" · "Hoje até 18h" · "Amanhã cedo" (= tomorrow 10h) → `dataLimiteRetirada`.
  6. **"Foto (se quiser)"** — dashed button "Tirar foto"; in this MVP it is a text field for `urlFoto` (decision 9: URL only).
  7. **"Confira: assim as famílias vão ver"** — live preview of the lot card; note "Você pode mudar ou cancelar enquanto ninguém reservar."
  - Submit button **"Avisar as famílias"** → `POST /api/lotes` (the API also creates the NOVO_LOTE alert).
  - Success: "**Obrigado! As famílias foram avisadas.**" + summary + buttons "Registrar outra sobra" / "Ver retiradas".
- **"Meus lotes" list** (below the form): the ofertante's lots with status chips; DISPONIVEL ones have **"Mudar"** (PUT) and **"Cancelar"** (DELETE, HU03); show the 409 message when not allowed.

### 3.4 Retirada + destino — `retiradas.html` (HU13, HU14)

- H1: "quem vem buscar hoje" / **"Retiradas"**; on the right a form "Número da pessoa" (numeric input, big letter-spaced) + button **"Conferir"** → finds the reservation by id and highlights it.
- **List (left):** one card per reservation of the current ofertante's lots (ATIVA first, then RETIRADA): illustration tile, "{DESCRIÇÃO} · {peso} kg", person line "{beneficiário} · {tipo}", "Número **{idReserva}** · {busca na banca | levado ao ponto {nome}}". Right side by state:
  - ATIVA → green button **"Já buscou"** → `PUT /api/reservas/{id}/retirada`.
  - RETIRADA without destination → yellow badge "Falta dizer o destino".
  - Destination registered → light badge "✓ {peso} kg: {x} comida, {y} adubo".
- **Destination panel (right aside):** idle = sprout illustration, "Para onde foi a comida?", "Quando a pessoa buscar, toque em **JÁ BUSCOU** e diga quanto virou comida e quanto virou adubo." Active = "retirada confirmada" / "O que aconteceu com os {peso} kg de {descrição}?"; split bar (comida `--verde-medio` / adubo `--marrom-adubo`); row "Virou comida" with +/- (default = full weight); row "Virou adubo" computed = peso − comida; note "Adubo = o que não dava mais para comer. Vai para a horta comunitária."; button **"Salvar"** → creates up to two `Destinacao` (CONSUMO and/or COMPOSTAGEM, skip 0 kg).
- **Expired lots** of the ofertante appear in a small section "Venceu sem retirada" with a button "Mandar para adubo" → COMPOSTAGEM only (decision 4).

### 3.5 Painel de impacto — `painel.html` (HU15, HU20)

- H1: "{mês de ano}" (e.g. "novembro de 2026") / **"Meu impacto"**; yellow badge "Dados de exemplo do protótipo" only when the data comes from the seed.
- **Hero (dark):** "Este mês você salvou do lixo" / **{total} kg** (150px, `--verde-folha`) / "de fruta, verdura e legume." + crate illustration.
- **4 summary cards:** "{x} kg viraram **comida**" · "{y} kg viraram **adubo**" · "{n} **famílias** e instituições atendidas" (distinct beneficiaries with RETIRADA) · "{m} **lotes** retirados".
- **"Quilos salvos por semana":** bar chart of the last 4 weeks (CSS bars, value label on top, last bar darker, labels "Semana 1 … Esta semana"); below, bar "Para onde foi": "{p}% comida" / "{q}%" adubo + "Nada foi para o lixo comum: o que não dava para comer virou adubo."
- **Selo de Parceiro card (green):** when `possuiSelo` → "você é parceiro", round badge "PARCEIRO / REDE DE PRATO", quote "Este mês, a {ofertante} salvou **{total} kg** de comida do lixo.", buttons "Mandar no WhatsApp" (opens `https://wa.me/?text=...`) and "Baixar o selo" (downloads an SVG). When no selo → "Ainda sem selo" and a short text on how to get it.
- **Admin strip** (small, bottom; HU20): "Administração: conceder selo" → select ofertante + button → `PUT /api/ofertantes/{id}/selo`.
- Also show **"Impacto da rede"** totals (all ofertantes) for the Administrador view.

## 4. UX decisions (from the Agile doc)

- Low-income, low-literacy audience: short texts, Atkinson Hyperlegible, large titles, many illustrations, icons always with a word, big buttons (64–84px), few steps, +/- instead of typing numbers.
- Visual reference: houseofgiants.com/services (cream background, heavy uppercase titles, dark numbered section), adapted to the logo colors (#1A3A2A and #9CCC6C) and orange #C2410C for xepa.
- Fictional store names (Hortifrúti do Paraíso, Banca do Seu Antônio, Mercado Bom Preço); fictional people (Rosana, Cozinha Solidária do bairro). The logo says "REDE PRATO"; the official name is "Rede de Prato" — use "REDE DE PRATO" in text.
- Pending placeholders kept as-is: [Endereço do ponto] (replaced by API data when a ponto exists) and [NÚMERO DA REDE].
