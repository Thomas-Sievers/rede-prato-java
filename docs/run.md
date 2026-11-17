# Rede de Prato — Instruções para rodar

## Ferramentas e versões

| Ferramenta | Versão |
| --- | --- |
| JDK | 21 (Temurin / OpenJDK 21) |
| Spring Boot | 4.1.1 (Spring Framework 7, Hibernate 7) |
| Maven | via Maven Wrapper (`./mvnw`, não precisa instalar) |
| IDE | IntelliJ IDEA (Community ou Ultimate) — com o plugin Lombok (já vem por padrão) |
| Banco (padrão) | H2 2.x em memória, modo Oracle (sobe junto com a aplicação) |
| Banco (produção acadêmica) | Oracle Database da FIAP (`oracle.fiap.com.br`), driver `ojdbc17` |
| Testes | JUnit 5 + Mockito + AssertJ (`spring-boot-starter-test`) |
| Deploy | Render.com (Docker, `./Dockerfile`) |

## Links

- GitHub: https://github.com/Thomas-Sievers/rede-prato-java
- Deploy: https://rede-prato-java.onrender.com (saúde: https://rede-prato-java.onrender.com/api/health)
- Telas: `/` (início), `/lotes.html`, `/registrar.html`, `/retiradas.html`, `/painel.html`
- H2 console (só no perfil padrão): `/h2-console` — JDBC URL `jdbc:h2:mem:redeprato`, usuário `sa`, senha vazia

## Passo a passo (perfil padrão, H2 com dados de exemplo)

1. Clonar: `git clone https://github.com/Thomas-Sievers/rede-prato-java.git && cd rede-prato-java`
2. Rodar os testes: `./mvnw test` (Windows: `mvnw.cmd test`) — usa só H2, sem internet e sem Oracle.
3. Subir a aplicação: `./mvnw spring-boot:run`
4. Abrir http://localhost:8080 — as telas já mostram dados simulados (o `DataSeeder` cria categorias, ofertantes, beneficiários, pontos, lotes em todos os status, reservas, destinos e alertas com datas relativas a "hoje").
5. Testar a API: `curl http://localhost:8080/api/lotes/disponiveis` (lista completa em `docs/endpoints.md`).

No IntelliJ: *File → Open* na pasta do projeto, esperar o Maven importar, e rodar a classe `br.com.fiap.redeprato.RedePratoApplication`.

## Com o Oracle da FIAP (perfil `oracle`)

1. Rodar os scripts da Mariana no seu schema Oracle, nesta ordem: `db/ddl.sql` (tabelas) e `db/dml.sql` (dados de exemplo e ajuste dos contadores IDENTITY). O `db/dql.sql` traz as consultas da disciplina de banco.
2. Definir as variáveis de ambiente (nunca colocar senha em arquivo):
   ```bash
   export ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
   export ORACLE_USERNAME=<seu RM>
   export ORACLE_PASSWORD=<sua senha>
   export SPRING_PROFILES_ACTIVE=oracle
   ./mvnw spring-boot:run
   ```
   No perfil `oracle`: `ddl-auto=validate` (o Hibernate confere se as entidades batem com as tabelas), sem dados de exemplo e sem H2 console.
3. Se aparecer erro de validação, ver `docs/oracle-checklist.md`.

## Deploy no Render

- Serviço `rede-prato-java`, tipo **Docker**, branch `main`, health check `/api/health`. Cada `git push` na `main` gera um deploy novo.
- O `Dockerfile` compila com `eclipse-temurin:21-jdk` e roda com `eclipse-temurin:21-jre` na porta `$PORT`.
- Variáveis no Render: `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`; `SPRING_PROFILES_ACTIVE=oracle` só depois que o DDL estiver no Oracle (sem ela, o deploy usa H2 com dados de exemplo).
- O plano gratuito "dorme" sem uso: o primeiro acesso pode levar ~1 minuto.
