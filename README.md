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

## Equipe

Luxor Obelisc (equipe 18) — Áurea Sardinha Carminato (RM563837), Mariana Silva do Egito Moreira (RM562544), Mariana Souza França (RM562353), Thomas Soares Sievers (RM563566).

## Tecnologias

Java 21 · Spring Boot 4.1 · Spring Data JPA (Hibernate) · Bean Validation · Lombok · H2 (perfil padrão) · Oracle Database FIAP (perfil `oracle`) · Maven Wrapper · Docker · Render.

## Camadas

| Camada (rubrica) | Pacote | Conteúdo |
| --- | --- | --- |
| Model | `br.com.fiap.redeprato.entity` (+ `entity.enums`) | 9 entidades JPA mapeadas 1:1 para as tabelas `T_RP_*`, 10 enums |
| DAO | `br.com.fiap.redeprato.dao` | `XxxDAO extends JpaRepository<Xxx, Long>` (Spring Data = DAO), consultas JPQL |
| BO | `br.com.fiap.redeprato.bo` | `XxxBO` (`@Service`): regras de negócio validadas antes de gravar; transações |
| Controller | `br.com.fiap.redeprato.controller` | `@RestController` por recurso, GET/POST/PUT/DELETE em `/api` |
| Exception | `br.com.fiap.redeprato.exception` | `NotFoundException` (404), `BusinessRuleException` (400), `ConflictException` (409), `ApiError`, `GlobalExceptionHandler` |
| (apoio) | `dto`, `converter`, `config` | DTOs (records), conversores JPA (S/N, HH:MM), Clock/CORS, carga de dados simulados |

## Como rodar

```bash
./mvnw test                 # testes (H2, sem rede)
./mvnw spring-boot:run      # http://localhost:8080  (saúde: /api/health)
```

Com o Oracle da FIAP:

```bash
export ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
export ORACLE_USERNAME=<seu RM>
export ORACLE_PASSWORD=<sua senha>
SPRING_PROFILES_ACTIVE=oracle ./mvnw spring-boot:run
```

Endpoints: veja `docs/endpoints.md`.
