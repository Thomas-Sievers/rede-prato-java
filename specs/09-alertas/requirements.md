# 09 — Alertas · Requirements

HU covered: **HU04** (feirante envia alerta de xepa em poucos toques — UC04), **HU05** (beneficiário é avisado de lote novo ou xepa — UC07).

## Acceptance criteria (UC04 criteria included)

1. **Given** an active FEIRANTE with DISPONIVEL lots, **when** `POST /api/alertas/xepa {idOfertante}` (no lots listed), **then** 201 with one XEPA alert per DISPONIVEL lot of the banca, canal APP, `dataEnvio=now`, and `"mensagem": "Xepa avisada para n lotes."`.
2. **Given** `idsLotes: [..]`, **then** only those lots are announced.
3. **Given** no DISPONIVEL lot (or an empty selection resolving to none), **then** 400 `"Selecione pelo menos um lote disponível."` and nothing is saved.
4. **Given** a listed lote from another ofertante, **then** 400 `"O lote 5 não pertence a este ofertante."`; a listed lote not DISPONIVEL → 400 `"O lote 5 não está disponível."`.
5. **Given** `novoPrazo` in the future, **then** the `dataLimiteRetirada` of the announced lots becomes `novoPrazo`; a past novoPrazo → 400 `"O novo prazo deve ser no futuro."`.
6. **Given** a SUPERMERCADO or an inactive ofertante, **then** 400 `"Só feirantes podem avisar xepa."` / `"Ofertante inativo não pode avisar xepa."`.
7. **Given** the announced lots, **then** they stay DISPONIVEL (visible in `GET /api/lotes/disponiveis`).
8. **Given** a new lote, **when** `POST /api/lotes`, **then** one NOVO_LOTE/APP alert is stored for it (HU05).
9. **Given** alerts of DISPONIVEL lots in the last N hours, **when** `GET /api/alertas/recentes?horas=24`, **then** they are listed newest first with lote description, ofertante name, bairro, deadline and tipo; alerts of lots no longer DISPONIVEL are hidden; `horas` outside 1–168 → 400.
10. **Given** the CRUD `/api/alertas`, **then** GET (`?tipo=&idLote=`), POST `{idLote, tipo, canal}`, PUT `{tipo, canal}` and DELETE (always, 204) work; "Desfazer" in the xepa screen uses DELETE.
11. **Given** a lote is deleted, **then** its alerts are deleted with it (decisão 6).

## Business rules

- BR-09.1 ofertante exists (404), ativo and FEIRANTE (400) — UC04 actor is the feirante.
- BR-09.2 empty/absent `idsLotes` ⇒ all DISPONIVEL lots of the ofertante; empty result ⇒ 400.
- BR-09.3 every listed lote belongs to the ofertante and is DISPONIVEL (400 naming the lote).
- BR-09.4 `novoPrazo` optional, > now; updates the lots' deadline.
- BR-09.5 one Alerta per lote, tipo XEPA, canal default APP, `dataEnvio=now`.
- BR-09.6 no recipient stored (decisão 8); no "pending" status (decisão 1) — WhatsApp is only stored.
- BR-09.7 all in one transaction; expiry runs first so expired lots are not announced.
- BR-09.8 CRUD: lote must exist; tipo required; canal default APP.
