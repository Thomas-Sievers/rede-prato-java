# Blocked

Append-only log of things that could not be done unattended (`## B-<n> · <date> · <feature NN>` + What / Tried / Needs from Thomas).

## B-1 · 2026-11-17 · feature 12–16
**What:** The HTML/JS screens could not be opened in a browser or syntax-checked during the night run.
**Tried:** Starting the server is forbidden by CLAUDE.md; running `node --check` on the JS files was not permitted in this session. Pages are covered only by MockMvc "page is served" tests.
**Needs from Thomas:** Run `./mvnw spring-boot:run`, open http://localhost:8080 and click through the 5 screens (or check the Render deploy); report any JS console error.

## B-2 · 2026-11-17 · feature 01
**What:** Could not look up the latest Spring Boot 4.0.x on Maven Central (web fetch / curl not permitted).
**Tried:** Used 4.1.1, the version in the teacher's CP3 `pom.xml`, which resolved fine (D-2).
**Needs from Thomas:** Nothing, unless the course requires 4.0.x.
