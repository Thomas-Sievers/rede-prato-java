# AI use log (prompts)

The Java project was implemented by Claude Code (Anthropic) running unattended, following a written plan. Thomas Sievers reviewed the result.

## Starting prompt

> Read CLAUDE.md and docs/PLAN.md, then execute the plan from start to finish without stopping or asking me anything. Follow the autonomy rules in the plan exactly.

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
- 21-oracle-smoke — Thomas asked for a JDK-only smoke test against the real Oracle schema loaded with Mariana's scripts. Claude Code wrote `tools/smoke/OracleSmokeTest.java` (58 endpoints, the main flows and error cases; 149/149 passed). At Thomas's request it made Oracle the default profile, with H2 only for `dev` and tests (D-16), and fixed `/api/health` to report the default profile.

## Planning, design, tests and documents (Claude in the claude.ai Project "Global Solution - 2026")

Besides Claude Code, Thomas used Claude in a claude.ai Project as a copilot for planning, review and writing. Main requests, in order (summarized; user-facing text in Portuguese):

1. **Setup and plan:** "Guide me through the setup step by step" and "Write an implementation plan so Claude Code can build everything." Result: `CLAUDE.md`, `docs/PLAN.md`, the `specs/` structure and `.claude/settings.local.json`. Rules given by Thomas: Oracle credentials only in environment variables, never commit secrets, Mariana's DDL is the source of truth, no commits after 19/11 23h55.
2. **Package names:** "Use repository / service" (the teacher's CP3 pattern). Result: feature 19.
3. **Redesign:** "Every font is too big; change the landing icons to 3D ones like the attached image; run accessibility tests following the WCAG and change anything that doesn't pass. Create the design in Claude Design, then, after my permission, create the prompt for Claude Code." Follow-ups on the canvas: "This one doesn't need to be 3D" and "Make the background of this image equal to the site background". Result: `docs/redesign/` and the feature 20 prompt.
4. **Tests:** "The project is done, it is open in localhost:8080. Can you check if everything is working?" and "Changes done, test it." Result: page-by-page check and an axe-core 4.10 audit against WCAG 2.2 AA (0 violations), plus instructions for the VoiceOver test, which Thomas ran.
5. **Database scripts:** "Here are Mariana's scripts, can you review them and see if everything is right?" Result: all tables and columns match the entities. Claude found that the fixed IDs in the DML leave the IDENTITY counters at 1, which would break the app's first inserts. Mariana added the `ALTER TABLE ... (START WITH LIMIT VALUE)` lines to her DML. The team decided to keep the sample dates as delivered. The scripts were copied unchanged to `db/`.
6. **Oracle smoke test:** "Even though I started with =oracle, can Claude Code test everything for me?" Result: the feature 21 prompt.
7. **Oracle troubleshooting:**
   - "Schema validation: missing table [t_rp_alerta]": Mariana's scripts had not been run in the schema the app uses.
   - Render log with `ORA-02391: exceeded simultaneous SESSIONS_PER_USER limit`: Claude edited `application-oracle.properties` to cap the Hikari pool at 2 connections (`minimum-idle=1`) and set `OracleDialect`, so Render, the local app and SQL Developer can share the same RM.
8. **Documents:**
   - "Create the PDF using ABNT format." Result: `Rede_de_Prato_DDD_Java_ABNT` (Word + PDF), written from the repository docs and screenshots and updated after each change.
   - "Complete this README with our Java part": the backend section of the README for Mariana's scripts.

## Agile documentation

The Agile deliverable was also made with Claude as a copilot, in other chats of the same Project. Claude helped with research (public data on waste, food insecurity and street markets; laws and tax rules), organization and writing: idea presentation, personas, use cases, product backlog and Jira, prototype and the ABNT formatting.

The team did the fieldwork itself:
- The interviews at Natural da Terra and Yayá (15/11) were done in person by Thomas.
- The Planning Poker estimates were voted by the team.

## How the AI output was checked

- **Automated tests:** 325 tests (JUnit 5 + Mockito) run on H2 with `./mvnw test`. `DocsConsistencyTest` fails if an endpoint or entity is missing from the docs.
- **Oracle smoke test:** `tools/smoke/OracleSmokeTest.java` passed 149/149 checks against the FIAP Oracle schema on 17/11.
- **Accessibility:** axe-core (WCAG 2.2 AA) on the 5 pages, plus a manual VoiceOver pass.
- **Human review:** Thomas reviewed every feature, approved the design and the decisions in `DECISIONS.md`, and checked the screens and the Render deploy himself.
