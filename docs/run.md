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

## Teste com Oracle

O `tools/smoke/OracleSmokeTest.java` é um teste de fumaça que chama a API que está rodando e confere tudo contra o banco Oracle de verdade. Ele não faz parte do `./mvnw test`, que continua usando só H2.

- **Cobertura:** chama os 58 endpoints de `docs/endpoints.md`.
- **Dados da Mariana:** confere os dados de exemplo (5 ofertantes, 5 categorias, 6 beneficiários, 5 pontos, lote 5 reservado).
- **Fluxos:** lote, reserva, retirada, destino, xepa, selo, exclusão lógica e física, expiração, impacto e relatório.
- **Erros:** 400, 404 e 409.

1. Rodar `db/ddl.sql` e `db/dml.sql` no schema Oracle e subir a aplicação no perfil `oracle`, como na seção anterior.
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
- Variáveis no Render: `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`; `SPRING_PROFILES_ACTIVE=oracle` só depois que o DDL estiver no Oracle (sem ela, o deploy usa H2 com dados de exemplo).
- O plano gratuito "dorme" sem uso: o primeiro acesso pode levar ~1 minuto.
