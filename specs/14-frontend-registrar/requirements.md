# 14 — Registrar lote + xepa (frontend) · Requirements

HU covered: **HU02** (registrar lote), **HU03** (editar/cancelar lote não reservado), **HU04** (xepa em poucos toques). Screen: prototipo.md §3.3, `docs/prototipo/2-registrar.png`.

## Acceptance criteria

1. **Given** the selected ofertante is a FEIRANTE, **then** the orange "Fim de feira?" block is shown; for supermarkets it is hidden.
2. **Given** "Avisar xepa agora", **then** `POST /api/alertas/xepa {idOfertante}` is sent; success shows "**Pronto!** Xepa avisada para {n} lotes até as {hora}." with a "Desfazer" link that deletes the alerts just created; with no lots, "Você não tem comida registrada agora. Registre abaixo.".
3. **Given** the 7-step form (categoria tiles + descrição, peso +/-, condição, doar/vender with preço/valor, prazo chips, URL da foto, live preview), **when** "Avisar as famílias", **then** `POST /api/lotes`; success shows "**Obrigado! As famílias foram avisadas.**" with "Registrar outra sobra" / "Ver retiradas"; API errors are shown with the Portuguese message.
4. **Given** "Meus lotes", **then** the ofertante's lots are listed with status; DISPONIVEL ones have **Mudar** (fills the form, then `PUT /api/lotes/{id}`) and **Cancelar** (`DELETE /api/lotes/{id}`); 409 messages are shown.
5. The persona selector lists active ofertantes; the nav has "Registrar sobra · Retiradas · Meu impacto" with the current page marked.
6. Accessibility: +/- buttons with aria-labels "Menos 1 quilo"/"Mais 1 quilo", counter `aria-live="polite"`, options are toggle buttons with `aria-pressed`.
