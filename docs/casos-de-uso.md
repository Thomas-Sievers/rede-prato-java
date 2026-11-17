<!-- Source: Claude Project doc "Rede de Prato — Casos de uso" (12/11/2026). -->

# Rede de Prato — Casos de uso

Escopo completo, para todos os atores; o MVP é escolhido no backlog (MoSCoW). Os casos de uso seguem o modelo de classes (`docs/classes.md`).

## Atores

| Ator | Tipo | Quem é |
| --- | --- | --- |
| Ofertante | Principal | Quem tem o excedente. Especializado em **Feirante** e **Supermercado** (generalização) |
| Beneficiário | Principal | Quem recebe ou compra o alimento. Especializado em **Pessoa / Família**, **Instituição** e **Pequeno negócio** |
| Operador do Ponto de Distribuição | Principal | Voluntário ou responsável pela cozinha solidária, ONG ou associação com a placa; cadastra quem não tem celular e confirma retiradas |
| Administrador (Rede de Prato / ONG) | Principal | Equipe da Rede de Prato: gerencia cadastros, categorias e o selo |
| Parceiro de Logística | Secundário | Coleta no local do ofertante e leva até os pontos (Could no MVP) |
| Tempo (Sistema) | Secundário | Rotinas automáticas: expirar lotes e gerar o relatório mensal |

## Casos de uso

| ID | Caso de uso | Atores | Descrição |
| --- | --- | --- | --- |
| UC01 | Cadastrar ofertante | Ofertante | Cadastro da banca ou do supermercado, com tipo, contato e endereço |
| UC02 | Registrar lote de excedente | Ofertante | Registra alimento, categoria, peso (kg), condição, doação ou venda, preço, valor estimado, prazo de retirada e foto |
| UC03 | Gerenciar lotes (editar e cancelar) | Ofertante | Altera ou cancela um lote ainda não reservado |
| UC04 | Emitir alerta de xepa | Ofertante (Feirante) | No fim da feira, avisa os interessados próximos sobre os lotes da banca |
| UC05 | Solicitar transporte | (estende UC02) | Marca o lote como "precisa de transporte" para o parceiro de logística |
| UC06 | Registrar destinação do alimento | Ofertante | Registra quanto foi consumido e quanto virou adubo; um lote pode ter mais de um destino |
| UC07 | Notificar interessados (app / WhatsApp) | (incluído por UC02 e UC04) | Envia o alerta de novo lote ou de xepa; WhatsApp simulado no protótipo |
| UC08 | Consultar lotes disponíveis | (incluído por UC12) | Lista os lotes DISPONÍVEIS, com filtros por bairro, categoria e modalidade |
| UC09 | Confirmar retirada | Ofertante, Operador do Ponto | Confirma que o beneficiário retirou o lote; a reserva vira RETIRADA e o lote vira RETIRADO |
| UC10 | Consultar painel de impacto | Ofertante, Administrador | Mostra kg desviados do descarte, refeições estimadas e pessoas atendidas |
| UC11 | Cadastrar beneficiário | Beneficiário, Operador do Ponto | Cadastro sem documento; telefone e endereço opcionais. O operador cadastra quem não tem celular |
| UC12 | Reservar lote | Beneficiário | Reserva o lote inteiro e escolhe onde retirar (no ofertante ou em um ponto); só uma reserva ativa por lote |
| UC13 | Reservar lote com desconto (venda) | (estende UC12) | Quando o lote é de VENDA: mostra o preço com desconto; pagamento feito na retirada (sem pagamento real no protótipo) |
| UC14 | Cancelar reserva | Beneficiário | Cancela uma reserva ativa; o lote volta a ficar DISPONÍVEL |
| UC15 | Consultar pontos e horários (mapa) | Beneficiário | Mapa de São Paulo com os pontos de distribuição e seus horários |
| UC16 | Gerenciar ponto de distribuição | Operador do Ponto | Mantém dados, dias e horários de funcionamento do ponto (os mesmos da placa) |
| UC17 | Gerenciar cadastros | Administrador | CRUD de ofertantes, beneficiários e pontos, com exclusão lógica |
| UC18 | Gerenciar categorias de alimento | Administrador | CRUD das categorias (frutas, verduras, legumes, padaria etc.) |
| UC19 | Conceder selo de parceiro | Administrador | Concede o Selo de Parceiro Rede de Prato a um ofertante e registra a data |
| UC20 | Consultar coletas pendentes | Parceiro de Logística | Lista os lotes marcados como "precisa de transporte" |
| UC21 | Registrar coleta e entrega | Parceiro de Logística | Registra a coleta no ofertante e a entrega no ponto |
| UC22 | Expirar lotes vencidos | Tempo (Sistema) | Passa para EXPIRADO os lotes que passaram do prazo sem retirada |
| UC23 | Gerar relatório mensal de impacto | Tempo (Sistema) | No fim do mês, gera o relatório de cada ofertante (kg, valor estimado, destinos) para divulgação e contabilidade |
| UC24 | Emitir declaração de doação | (incluído por UC23) | A Rede de Prato, como ONG, emite a declaração de cada doação, base da dedução fiscal |

## Relacionamentos

| Tipo | De | Para | Leitura |
| --- | --- | --- | --- |
| include | UC02 Registrar lote | UC07 Notificar interessados | Todo lote novo gera aviso |
| include | UC04 Emitir alerta de xepa | UC07 Notificar interessados | O alerta de xepa usa o mesmo envio |
| include | UC12 Reservar lote | UC08 Consultar lotes disponíveis | Para reservar, é preciso ver os lotes |
| include | UC23 Gerar relatório mensal | UC24 Emitir declaração de doação | O relatório traz as declarações do mês |
| extend | UC05 Solicitar transporte | UC02 Registrar lote | Só quando o lote precisa de transporte |
| extend | UC13 Reservar com desconto | UC12 Reservar lote | Só quando o lote é de venda |
| generalização | Feirante, Supermercado | Ofertante | Herdam todos os casos de uso do ofertante |
| generalização | Pessoa / Família, Instituição, Pequeno negócio | Beneficiário | Herdam todos os casos de uso do beneficiário |

## Ligação com o modelo de classes

| Classe | Casos de uso |
| --- | --- |
| Ofertante | UC01, UC17, UC19 |
| Lote | UC02, UC03, UC05, UC08, UC22 |
| Alerta | UC04, UC07 |
| Reserva | UC09, UC12, UC13, UC14 |
| Destinacao | UC06, UC10, UC23 |
| Beneficiario | UC11, UC17 |
| PontoDistribuicao | UC15, UC16, UC17 |
| CategoriaAlimento | UC18 |
| Endereco | UC01, UC11, UC16 |

Logística (UC20, UC21) usa o campo `precisa_transporte` do Lote; registrar a coleta em tabela própria fica como evolução, já que é Could no MVP.

## Especificação Funcional — UC12: Reservar Lote

**Objetivo:** permitir que o beneficiário garanta um lote de alimento disponível e saiba onde e até quando retirá-lo. É o caso de uso que leva o alimento até o cidadão principal.

**Ator principal:** Beneficiário. **Componentes:** interface web, API REST (Java), Oracle, serviço de notificação.

**Pré-condições:** beneficiário cadastrado e ativo; pelo menos um lote DISPONÍVEL dentro do prazo.

**Pós-condições:** reserva ATIVA registrada (lote, beneficiário e ponto opcional); lote passa para RESERVADO e sai da lista; mudança de status e reserva na mesma transação.

**Fluxo principal:** 1) acessa a lista de lotes disponíveis (UC08); 2) sistema mostra alimento, peso, condição, modalidade, local e prazo; 3) filtra por bairro, categoria ou modalidade; 4) seleciona um lote; 5) escolhe onde retirar (ofertante ou ponto); 6) confirma; 7) sistema verifica disponibilidade e prazo; 8) registra a reserva e muda o lote para RESERVADO; 9) exibe confirmação com local, horário e prazo.

**Fluxos alternativos:** lote de venda (UC13) mostra preço com desconto, pago na retirada; lote já reservado por outro → aviso e volta à lista; prazo vencido → aviso, sem reserva; sem lotes → mostra pontos e horários (UC15); desistência → nada é registrado.

**Regras de negócio:** reserva do lote inteiro; só uma reserva ATIVA por lote (API + índice único no banco); só lotes DISPONÍVEIS e no prazo; ponto opcional (vazio = retirada no ofertante); sem documento nem telefone obrigatórios.

**Critérios de aceite:** só lotes disponíveis e no prazo aparecem; é possível reservar e escolher o local; lote reservado vira RESERVADO e some da lista; segunda reserva do mesmo lote é recusada com mensagem clara; lotes vencidos não podem ser reservados; confirmação mostra local, horário e prazo.

## Especificação Funcional — UC04: Emitir Alerta de Xepa

**Objetivo:** permitir que o feirante, no fim da feira, avise rapidamente os interessados próximos sobre o que sobrou. É o diferencial da Rede de Prato.

**Ator principal:** Feirante. **Componentes:** interface web, API REST (Java), Oracle, notificação (app e WhatsApp simulado).

**Pré-condições:** feirante cadastrado e ativo; pelo menos um lote DISPONÍVEL (senão, registra antes pelo UC02).

**Pós-condições:** um alerta XEPA por lote incluído, com canal e data; interessados notificados (UC07); lotes seguem DISPONÍVEIS até reserva ou expiração.

**Fluxo principal:** 1) feirante acessa "Avisar xepa"; 2) sistema mostra os lotes disponíveis da banca; 3) seleciona os lotes; 4) ajusta o prazo para o fim da feira, se quiser; 5) confirma; 6) sistema registra um alerta XEPA por lote; 7) notifica os interessados (UC07); 8) confirma quantos lotes foram anunciados.

**Fluxos alternativos:** banca sem lotes → registro rápido (UC02) e volta ao passo 3; nenhum lote selecionado → não envia e pede seleção; falha no envio → alerta registrado como pendente, não como enviado (NÃO implementado — ver docs/decisoes.md item 1); nenhum interessado próximo → alerta registrado e lotes seguem visíveis.

**Regras de negócio:** aviso geral por lote, sem registrar cada destinatário; proximidade simulada por bairro ou CEP; fluxo curto (poucos minutos, mãos ocupadas); o que não for retirado até o prazo vira EXPIRADO (UC22) e pode ir para compostagem.

**Critérios de aceite:** envio em poucos toques; alerta registrado com tipo XEPA, canal e data; lotes anunciados aparecem como disponíveis; envio sem lote selecionado é recusado com mensagem clara; falhas de envio sinalizadas, sem marcar como enviado (não implementado, decisão 1).
