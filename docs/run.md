# Rede de Prato — Instruções para rodar

## Ferramentas e versões

| Ferramenta | Versão |
| --- | --- |
| JDK | 21 (Temurin / OpenJDK 21) |
| Spring Boot | 4.1.1 (Spring Framework 7, Hibernate 7) |
| Maven | via Maven Wrapper (`./mvnw`, não precisa instalar) |
| IDE | IntelliJ IDEA (Community ou Ultimate) — com o plugin Lombok (já vem por padrão) |
| Banco (padrão) | Oracle Database da FIAP (`oracle.fiap.com.br`), driver `ojdbc17` — perfil `oracle` |
| Banco (desenvolvimento e testes) | H2 2.x em memória, modo Oracle — perfil `dev` (o `./mvnw test` usa sempre este) |
| Testes | JUnit 5 + Mockito + AssertJ (`spring-boot-starter-test`) |
| Deploy | Render.com (Docker, `./Dockerfile`) |

## Links

- GitHub: https://github.com/Thomas-Sievers/rede-prato-java
- Deploy: https://rede-prato-java.onrender.com (saúde: https://rede-prato-java.onrender.com/api/health)
- Telas: `/` (início), `/lotes.html`, `/registrar.html`, `/retiradas.html`, `/painel.html`
- H2 console (só no perfil `dev`): `/h2-console` — JDBC URL `jdbc:h2:mem:redeprato`, usuário `sa`, senha vazia

## Passo a passo com o Oracle da FIAP (padrão)

A aplicação usa o Oracle por padrão (decisão D-16). O H2 serve só para desenvolvimento e testes.

1. Clonar: `git clone https://github.com/Thomas-Sievers/rede-prato-java.git && cd rede-prato-java`
2. Rodar os scripts da Mariana no seu schema Oracle, nesta ordem: `db/ddl.sql` (tabelas) e `db/dml.sql` (dados de exemplo e ajuste dos contadores IDENTITY). O `db/dql.sql` traz as consultas da disciplina de banco.
3. Na pasta do projeto, definir as variáveis de ambiente (nunca colocar senha em arquivo) e subir a aplicação **no mesmo terminal**:
   ```bash
   export ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
   export ORACLE_USERNAME=<seu RM>
   export ORACLE_PASSWORD=<sua senha>
   ./mvnw spring-boot:run
   ```
   - O log mostra `The following 1 profile is active: "oracle"`, e `/api/health` responde `"profile":"oracle"`.
   - Neste perfil: `ddl-auto=validate` (o Hibernate confere se as entidades batem com as tabelas), sem dados simulados e sem H2 console.
   - Sem as variáveis `ORACLE_*`, a aplicação não sobe.
4. Abrir http://localhost:8080. As telas mostram os dados do `db/dml.sql`.
5. Se aparecer erro de validação, ver `docs/oracle-checklist.md`.

No IntelliJ: *File → Open* na pasta do projeto, esperar o Maven importar, colocar as variáveis `ORACLE_*` na *Run Configuration* e rodar a classe `br.com.fiap.redeprato.RedePratoApplication`.

## Desenvolvimento e testes (H2, perfil `dev`)

1. Rodar os testes: `./mvnw test` (Windows: `mvnw.cmd test`). Eles usam sempre o H2 (`src/test/resources/config/application.properties` ativa o perfil `dev`), sem internet e sem Oracle.
2. Subir a aplicação sem Oracle: `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run`
   - As telas mostram dados simulados: o `DataSeeder` cria categorias, ofertantes, beneficiários, pontos, lotes em todos os status, reservas, destinos e alertas, com datas relativas a "hoje".
   - Testar a API: `curl http://localhost:8080/api/lotes/disponiveis` (lista completa em `docs/endpoints.md`).

## Teste com Oracle

O `tools/smoke/OracleSmokeTest.java` é um teste de fumaça que chama a API que está rodando e confere tudo contra o banco Oracle de verdade. Ele não faz parte do `./mvnw test`, que continua usando só H2.

- **Cobertura:** chama os 58 endpoints de `docs/endpoints.md`.
- **Dados da Mariana:** confere os dados de exemplo (5 ofertantes, 5 categorias, 6 beneficiários, 5 pontos, lote 5 reservado).
- **Fluxos:** lote, reserva, retirada, destino, xepa, selo, exclusão lógica e física, expiração, impacto e relatório.
- **Erros:** 400, 404 e 409.

1. Rodar `db/ddl.sql` e `db/dml.sql` no schema Oracle e subir a aplicação, como em "Passo a passo com o Oracle da FIAP".
2. Em outro terminal, na pasta do projeto, rodar o teste. Só precisa do JDK 21; não usa Maven nem bibliotecas:
   ```bash
   java tools/smoke/OracleSmokeTest.java                     # usa http://localhost:8080
   java tools/smoke/OracleSmokeTest.java https://rede-prato-java.onrender.com
   ```
3. Ler a saída:
   - uma linha por verificação, no formato `PASS`/`FAIL`, método, caminho, status esperado × recebido e o motivo;
   - no fim, o resumo e a lista das falhas.
   - O código de saída é `0` se tudo passou e `1` se algo falhou.

Cuidados:
- **Só roda no Oracle:** a primeira verificação é `GET /api/health`. Se o perfil não for `oracle`, o teste para e não roda contra o H2.
- **Grava dados de verdade no banco.** Tudo que ele cria começa com `[TESTE]`. No fim ele:
  - desativa o ofertante, o beneficiário e o ponto de teste;
  - apaga o lote de teste que sobrou.

  O lote de teste retirado fica no banco, com seus destinos, como histórico.
- **Dados de exemplo:**
  - não mexe no lote 5 nem na reserva dele;
  - o selo do ofertante 3 é concedido e retirado, e termina como no `dml.sql` (sem selo).
- Pode rodar quantas vezes quiser: os nomes levam um número da execução, então não repetem.

## Deploy no Render

- Serviço `rede-prato-java`, tipo **Docker**, branch `main`, health check `/api/health`. Cada `git push` na `main` gera um deploy novo.
- O `Dockerfile` compila com `eclipse-temurin:21-jdk` e roda com `eclipse-temurin:21-jre` na porta `$PORT`.
- Variáveis no Render: `ORACLE_URL`, `ORACLE_USERNAME` e `ORACLE_PASSWORD` são obrigatórias, porque o padrão é Oracle (D-16). Para uma demo só no H2, com dados simulados, definir `SPRING_PROFILES_ACTIVE=dev`.
- O plano gratuito "dorme" sem uso: o primeiro acesso pode levar ~1 minuto.
