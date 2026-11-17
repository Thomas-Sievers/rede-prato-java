# AI use log (prompts)

The Java project was implemented by Claude Code (Anthropic) running unattended, following a written plan. Thomas Sievers reviewed the result.

## Starting prompt

> Read CLAUDE.md and docs/PLAN.md, then execute the plan from start to finish without stopping or asking me anything. I am asleep until morning. Follow the autonomy rules in the plan exactly.

Plan: `docs/PLAN.md` (spec-driven: every feature has `specs/<NN>-<name>/requirements.md`, `design.md`, `tasks.md`).

## Per feature

- 01-setup — Claude Code implemented 01-setup from `specs/01-setup/`; key decisions: Boot 4.1.1 like the teacher's CP3 pom (D-2), standalone MockMvc tests (D-3).
- 02-categorias-enderecos — Claude Code implemented 02 from `specs/02-categorias-enderecos/`; key decision: all rules in BOs, BOs return response records (D-5).
- 03-ofertantes-selo — Claude Code implemented 03 from `specs/03-ofertantes-selo/`; key decision: reactivation via PUT `ativo` (D-6).
- 04-lotes — Claude Code implemented 04 from `specs/04-lotes/`; key decisions: expiry also cancels the ATIVA reserva (D-7), PUT ignores idOfertante (D-8).
- 05-beneficiarios — Claude Code implemented 05 from `specs/05-beneficiarios/` (no document fields, reflection test).
- 06-pontos — Claude Code implemented 06 from `specs/06-pontos/` (horários as "HH:MM" strings).
- 07-reservas — Claude Code implemented 07 from `specs/07-reservas/`; one test per UC12 criterion.
- 08-retirada-destinacao — Claude Code implemented 08 from `specs/08-retirada-destinacao/`; included the batch endpoint `POST /api/lotes/{id}/destinacoes`.
- 09-alertas — Claude Code implemented 09 from `specs/09-alertas/`; one test per UC04 criterion; only feirantes send xepa (D-9).
- 10-impacto — Claude Code implemented 10 from `specs/10-impacto/`; refeições estimadas not computed (D-11).
- 11-seed — Claude Code implemented 11 from `specs/11-seed/` (simulated data, dates relative to the Clock).
- 17-docs — Claude Code wrote `docs/endpoints.md`, `class-diagram.md`, `der.md`, `run.md`, `oracle-checklist.md` from the code, plus `DocsConsistencyTest` (fails if an endpoint/entity is missing from the docs).
- 18-extras — Claude Code added Swagger UI, the HU22 monthly report, the transport filter and the "Onde buscar" list (D-13).
- 12–16 frontend — Claude Code implemented the 5 screens from `specs/12…16-*` and `docs/prototipo.md` + the prototype PNGs (plain HTML/JS, D-12).
- 19-rename-layers — Thomas asked to follow the teacher's package pattern: Claude Code renamed `dao`/`bo` to `repository`/`service` (D-14, supersedes D-1), updated the docs, and fixed the "Você é:" selector at 375 px and the reservation dialog radios' aria-label.
- 20-redesign-wcag — Thomas approved a redesign (design canvas exported to `docs/redesign/`). Claude Code wrote `specs/20-redesign-wcag/` and applied the smaller type scale (CSS variables), the 3D landing art (hero photo of the crate, gradient SVGs) and the WCAG 2.2 AA fixes A1–A9 (marquee pause button, two-tone focus ring, 320 px reflow, skip link + `<main id="conteudo">`, heading order, chart and button names) to the 5 static pages, plus MockMvc tests (D-15).
