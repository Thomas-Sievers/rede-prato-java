<!-- Source: Claude Project doc "Rede de Prato — Product Backlog" (13/11/2026). Jira: redeprato.atlassian.net, project SCRUM (SCRUM-12..37 = HU01..HU26). -->

# Rede de Prato — Product Backlog

26 histórias, 7 épicos, MoSCoW, Story Points (Fibonacci). **MVP = as 13 histórias Must (48 SP)**: HU01, 02, 03, 04, 05, 07, 09, 10, 13, 14, 15, 19, 20 — o ciclo completo, do registro do lote ao kg salvo no painel.

Incluídas no projeto Java por decisão (docs/decisoes.md): **HU12** (cancelar reserva) e **HU21** (expiração automática), ambas Should.

## Épicos

| Épico | Casos de uso |
| --- | --- |
| E1 – Gestão de lotes | UC01, UC02, UC03, UC06 |
| E2 – Alertas e xepa | UC04, UC07 |
| E3 – Reservas e retirada | UC08, UC09, UC11, UC12, UC13, UC14 |
| E4 – Pontos de distribuição | UC15, UC16 |
| E5 – Impacto e relatórios | UC10, UC22, UC23, UC24 |
| E6 – Administração | UC17, UC18, UC19 |
| E7 – Logística | UC05, UC20, UC21 |

## Histórias

| ID | Épico | História (resumo) | UC | MoSCoW | MVP | SP |
| --- | --- | --- | --- | --- | --- | --- |
| HU01 | E1 | Ofertante se cadastra com tipo, contato e endereço | UC01 | Must | Sim | 3 |
| HU02 | E1 | Ofertante registra lote (alimento, categoria, peso, condição, modalidade, prazo) | UC02 | Must | Sim | 5 |
| HU03 | E1 | Ofertante edita ou cancela lote ainda não reservado | UC03 | Must | Sim | 3 |
| HU04 | E2 | Feirante envia alerta de xepa em poucos toques | UC04 | Must | Sim | 5 |
| HU05 | E2 | Beneficiário é avisado na plataforma de lote novo ou xepa | UC07 | Must | Sim | 5 |
| HU06 | E2 | Beneficiário com pouca internet recebe avisos por WhatsApp | UC07 | Could | Não | 8 |
| HU07 | E3 | Beneficiário se cadastra sem documento (telefone e endereço opcionais) | UC11 | Must | Sim | 2 |
| HU08 | E3 | Operador do ponto cadastra quem não tem celular | UC11 | Should | Não | 2 |
| HU09 | E3 | Beneficiário consulta lotes disponíveis com filtros | UC08 | Must | Sim | 3 |
| HU10 | E3 | Beneficiário reserva lote e escolhe onde retirar | UC12 | Must | Sim | 8 |
| HU11 | E3 | Pequeno negócio vê preço com desconto ao reservar lote de venda | UC13 | Should | Não | 3 |
| HU12 | E3 | Beneficiário cancela reserva | UC14 | Should | Não (incluída no Java) | 2 |
| HU13 | E3 | Ofertante ou operador confirma a retirada | UC09 | Must | Sim | 3 |
| HU14 | E1 | Ofertante registra quanto foi consumido e quanto virou adubo | UC06 | Must | Sim | 3 |
| HU15 | E5 | Ofertante vê painel com kg salvos do descarte | UC10 | Must | Sim | 5 |
| HU16 | E4 | Beneficiário vê no mapa os pontos e horários | UC15 | Should | Não | 5 |
| HU17 | E4 | Operador mantém dados e horários do ponto | UC16 | Should | Não | 2 |
| HU18 | E6 | Administrador gerencia cadastros com exclusão lógica | UC17 | Should | Não | 5 |
| HU19 | E6 | Administrador gerencia categorias de alimento | UC18 | Must | Sim | 1 |
| HU20 | E6 | Administrador concede o Selo de Parceiro | UC19 | Must | Sim | 2 |
| HU21 | E5 | Lotes vencidos expiram automaticamente | UC22 | Should | Não (incluída no Java) | 3 |
| HU22 | E5 | Ofertante recebe relatório mensal de impacto | UC23 | Should | Não | 5 |
| HU23 | E5 | Supermercado recebe declaração de cada doação | UC24 | Could | Não | 3 |
| HU24 | E7 | Ofertante indica que o lote precisa de transporte | UC05 | Could | Não | 1 |
| HU25 | E7 | Parceiro de logística vê coletas pendentes | UC20 | Could | Não | 2 |
| HU26 | E7 | Parceiro de logística registra coleta e entrega | UC21 | Won't | Não | 5 |

Totais: Must 13 histórias/48 SP; Should 8/27; Could 4/14; Won't 1/5; total 26/94.

Nota para o Claude Code: os critérios de aceite completos estão no documento de Agile (fora do repo). Para UC12 (HU10) e UC04 (HU04) os critérios estão em `docs/casos-de-uso.md`. Para as demais, derive os critérios Given/When/Then do resumo acima + `docs/classes.md` + `docs/decisoes.md`.
