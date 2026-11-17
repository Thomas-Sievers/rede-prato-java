<!-- Source: Claude Project docs "FIAP Global Solution 2026.2 — CitizenTech" (rubric) and "Rede de Prato — Ideia refinada (v2)" (Ficha). -->

# GS 2026.2 — Ficha da CitizenTech + rubrica de Domain Driven Design – Java

## Ficha da CitizenTech (vai no topo do README)

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

## Rubrica — Domain Driven Design – Java

**Conceito:** implementar em Java uma capacidade central da CitizenTech, modelando o domínio de forma organizada e extensível. **Objetivo:** API RESTful com **Spring Boot** usando o banco **Oracle da FIAP**.

### Documentação — PDF único (30 pts)

| Item | Pontos |
| --- | --- |
| Especificação: capa (nome do projeto, nomes completos e RMs), índice, descrição da solução, justificativa e objetivos | 5 |
| Diagrama de classes que reflete o código Java | 10 |
| Protótipos das telas do front-end, tabela de endpoints (GET, POST, PUT, DELETE) e DER | 10 |
| Instruções para rodar: ferramentas (ex.: IntelliJ), versões (ex.: JDK 21, Oracle DB), link do GitHub, link do deploy, passo a passo | 5 |

### Parte técnica — projeto (70 pts)

| Item | Pontos |
| --- | --- |
| Camada Model com todas as classes, igual ao banco (Oracle FIAP) | 10 |
| Camadas DAO e BO | 10 |
| Camada Controller com GET, POST, PUT e DELETE | 10 |
| Camada de tratamento de exceções | 10 |
| Deploy em servidor de aplicação (**render.com**) | 30 |

- Entrega: `.zip` pelo **Portal do Aluno** com o PDF, o link do GitHub e o link do deploy.
- **Commits depois do prazo da GS (19/11/2026, 23h55) não são aceitos e zeram toda a parte técnica.**
- Seguir os padrões ensinados em aula.

### Combinados da GS que valem para o repo

- README organizado, com a Ficha no topo.
- IA como copiloto: registrar os prompts em `prompts.md`.
- Nenhum dado pessoal real: só dados simulados (nomes fictícios, telefones fictícios).
- Pensar em quem vai usar: fonte legível, bom contraste, linguagem simples, telas pequenas.
