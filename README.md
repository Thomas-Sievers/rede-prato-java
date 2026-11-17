# Rede de Prato

## Ficha da CitizenTech

| Campo | Resposta |
| --- | --- |
| Nome da CitizenTech | Rede de Prato |
| Equipe | Luxor Obelisc (equipe 18) — Áurea Sardinha Carminato (RM563837), Mariana Silva do Egito Moreira (RM562544), Mariana Souza França (RM562353), Thomas Soares Sievers (RM563566) |
| Cidade e bairro | São Paulo (SP) — Paraíso (supermercados Natural da Terra e Yayá; Feira da Rua Manuel da Nóbrega, por pesquisa em fontes públicas) |
| Cidadão beneficiado | Famílias de baixa renda, que passam a ter acesso a alimentos de qualidade perto de casa, por doação ou com desconto. Feirantes e supermercados têm ganhos secundários: menos custo com descarte e mais visibilidade. |
| Problema, em uma frase | Famílias de baixa renda do Paraíso têm pouco acesso a alimentos frescos, enquanto supermercados e feiras do mesmo bairro descartam todos os dias frutas, verduras e legumes ainda próprios para consumo, porque doar exige uma instituição cadastrada e uma organização que o varejo não tem tempo de montar. |
| Como funciona | Supermercados e feirantes registram o excedente. A plataforma avisa famílias e instituições próximas, que compram com desconto ou retiram a doação nos pontos de distribuição, identificados pela placa Rede de Prato. O que não dá mais para comer vira adubo para hortas comunitárias. |
| O que cada parte faz | **Agile:** concepção, personas, casos de uso, backlog, protótipo e pitch · **DDD Java:** API REST com CRUD completo e as telas das funcionalidades principais · **Database Design:** modelo relacional e scripts no Oracle · **Data Science:** dashboard de impacto com previsões (regressão linear e logística) · **Dynamic Programming:** mapa real de São Paulo com dados simulados · **Network:** rede do ponto de distribuição no Packet Tracer · **AR-VR:** vídeo 3D de 30 a 60 s |
| Link do repositório | https://github.com/Thomas-Sievers/rede-prato-java |
| Link do deploy | https://rede-prato-java.onrender.com |
| Indicador de impacto | kg de alimento desviados do descarte |
| ODS conectados | 12 (12.3), 2 (2.1), 11 (11.6) e 13 (secundário) |

## Links rápidos

| O quê | Onde |
| --- | --- |
| Aplicação (telas + API) | https://rede-prato-java.onrender.com |
| Saúde da API | https://rede-prato-java.onrender.com/api/health |
| Tabela de endpoints | [`docs/endpoints.md`](docs/endpoints.md) |
| Diagrama de classes | [`docs/class-diagram.md`](docs/class-diagram.md) |
| DER | [`docs/der.md`](docs/der.md) (oficial: Oracle Data Modeler da Mariana) |
| Como rodar | [`docs/run.md`](docs/run.md) |
| Checklist do Oracle | [`docs/oracle-checklist.md`](docs/oracle-checklist.md) |
| Especificações (SDD) | [`specs/`](specs/) — requisitos, design e tarefas de cada funcionalidade |

## Equipe

Luxor Obelisc (equipe 18) — Áurea Sardinha Carminato (RM563837), Mariana Silva do Egito Moreira (RM562544), Mariana Souza França (RM562353), Thomas Soares Sievers (RM563566).

## O que o sistema faz

1. **Ofertante registra a sobra** (lote em kg, doação ou venda barata, prazo de retirada) — a plataforma cria um aviso.
2. **Feirante avisa a xepa** com um toque no fim da feira.
3. **Beneficiário reserva o lote inteiro** e escolhe onde buscar: no próprio local ou num **ponto de distribuição** com a placa Rede de Prato. Recebe o **número de retirada**.
4. **Ofertante confirma a retirada** e diz quanto virou **comida** e quanto virou **adubo**.
5. **Painel de impacto**: kg salvos do lixo por mês e por semana, famílias atendidas e o **Selo de Parceiro**.

Sem login (decisão 5): cada tela tem o seletor **"Você é:"**. Lotes vencidos expiram sozinhos (job a cada 5 minutos).

## Tecnologias

Java 21 · Spring Boot 4.1 · Spring Data JPA (Hibernate 7) · Lombok · H2 (perfil padrão) · Oracle Database FIAP (perfil `oracle`) · JUnit 5 + Mockito · Maven Wrapper · Docker · Render · Frontend em HTML/CSS/JavaScript puro servido pelo próprio Spring Boot.

## Camadas

| Camada (rubrica) | Pacote | Conteúdo |
| --- | --- | --- |
| Model | `br.com.fiap.redeprato.entity` (+ `entity.enums`) | 9 entidades JPA mapeadas 1:1 para as tabelas `T_RP_*`, 10 enums |
| DAO | `br.com.fiap.redeprato.dao` | `XxxDAO extends JpaRepository<Xxx, Long>` (Spring Data = DAO), consultas JPQL |
| BO | `br.com.fiap.redeprato.bo` | `XxxBO` (`@Service`): regras de negócio validadas antes de gravar; transações |
| Controller | `br.com.fiap.redeprato.controller` | `@RestController` por recurso, GET/POST/PUT/DELETE em `/api` |
| Exception | `br.com.fiap.redeprato.exception` | `NotFoundException` (404), `BusinessRuleException` (400), `ConflictException` (409), `ApiError`, `GlobalExceptionHandler` |
| (apoio) | `dto`, `converter`, `config` | DTOs (records), conversores JPA (S/N, HH:MM), Clock/CORS, carga de dados simulados |

## Resumo dos endpoints (57 no total)

| Recurso | Endpoints |
| --- | --- |
| Categorias `/api/categorias` | GET, GET `/{id}`, POST, PUT `/{id}`, DELETE `/{id}` |
| Endereços `/api/enderecos` | GET (`?bairro`), GET `/{id}`, POST, PUT, DELETE |
| Ofertantes `/api/ofertantes` | GET (`?tipo&ativo&bairro`), GET `/{id}`, POST, PUT, DELETE (lógico), PUT/DELETE `/{id}/selo` |
| Lotes `/api/lotes` | GET, GET `/disponiveis`, GET `/{id}`, POST, PUT, DELETE, POST `/expirar`, POST `/{id}/destinacoes` |
| Beneficiários `/api/beneficiarios` | GET, GET `/{id}`, POST, PUT, DELETE (lógico) |
| Pontos `/api/pontos` | GET, GET `/{id}`, POST, PUT, DELETE (lógico) |
| Reservas `/api/reservas` | GET, GET `/{id}`, POST, PUT `/{id}`, PUT `/{id}/cancelar`, PUT `/{id}/retirada`, DELETE |
| Destinações `/api/destinacoes` | GET, GET `/{id}`, POST, PUT, DELETE |
| Alertas `/api/alertas` | POST `/xepa`, GET `/recentes`, GET, GET `/{id}`, POST, PUT, DELETE |
| Impacto `/api/impacto` | GET `/ofertantes/{id}`, GET `/rede` |
| Saúde | GET `/api/health` |

Detalhes, exemplos e códigos de erro: [`docs/endpoints.md`](docs/endpoints.md).

## Telas

| Tela | Caminho | Para quem |
| --- | --- | --- |
| Início (landing) | `/` | todos |
| Escolha sua comida (lotes + reserva + minhas reservas) | `/lotes.html` | beneficiário |
| Registrar sobra + avisar xepa + meus lotes | `/registrar.html` | ofertante |
| Retiradas + destino (comida/adubo) | `/retiradas.html` | ofertante / ponto |
| Meu impacto + selo + impacto da rede | `/painel.html` | ofertante / administrador |

Protótipos de referência: `docs/prototipo/*.png`.

## Como rodar

```bash
./mvnw test                 # 300+ testes (H2, sem rede)
./mvnw spring-boot:run      # http://localhost:8080  (saúde: /api/health)
```

No perfil padrão o banco é H2 em memória com **dados simulados** (nomes, telefones e endereços fictícios; nenhum documento).

Com o Oracle da FIAP:

```bash
export ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
export ORACLE_USERNAME=<seu RM>
export ORACLE_PASSWORD=<sua senha>
SPRING_PROFILES_ACTIVE=oracle ./mvnw spring-boot:run
```

Passo a passo completo (IntelliJ, Oracle, Render): [`docs/run.md`](docs/run.md).

## Testes

- Testes unitários de todos os BOs (JUnit 5 + Mockito, DAOs simulados): uma ou mais provas para cada regra de negócio (`BR-xx.k` nos `specs/`), incluindo todos os critérios de aceite do UC12 (reservar) e do UC04 (xepa).
- Testes da camada de exceções, dos conversores, integração ponta a ponta no H2 (MockMvc), dados de exemplo e consistência da documentação (`DocsConsistencyTest`).

## Uso de IA

O código Java foi implementado pelo Claude Code (Anthropic) seguindo um plano escrito pela equipe (`docs/PLAN.md`) e especificações por funcionalidade (`specs/`). Os prompts e decisões estão em [`prompts.md`](prompts.md) e [`DECISIONS.md`](DECISIONS.md).
