<!-- Source: Claude Project doc "Rede de Prato — Classes e relacionamentos" (12/11/2026). Domain model shared by Database Design and DDD Java. If Mariana's DDL in db/ differs, the DDL wins (see docs/decisoes.md). -->

# Rede de Prato — Classes e relacionamentos

12/11/2026 · Thomas Sievers

O modelo tem 9 classes (uma por tabela) e 10 enums. Ele vale igual para o banco (Database Design) e para a API Java (DDD). O banco fica no schema do Thomas no Oracle da FIAP; a Mariana Souza faz o diagrama e os scripts, e o Thomas faz o backend.

## Decisões gerais

| # | Decisão | Por quê |
| --- | --- | --- |
| 1 | Supermercado e feirante na mesma classe (Ofertante), com campo `tipo` | Os dois fazem a mesma coisa no sistema: registram excedente. Uma classe só significa um CRUD só |
| 2 | Pessoa sozinha, família, instituição e pequeno negócio na mesma classe (Beneficiario), com campo `tipo` | Todos reservam e retiram alimento do mesmo jeito. O tipo PESSOA garante que quem mora sozinho também é atendido |
| 3 | Uma reserva pega o lote inteiro, e só pode haver uma reserva ATIVA por lote | Reserva parcial exigiria controlar saldo de kg por lote. A regra fica no BO (mensagem amigável) e num índice único no Oracle (proteção final) |
| 4 | Status é um campo da própria classe Lote, limitado por CHECK | São só 4 valores fixos. O destino final (consumo ou adubo) fica em Destinacao, não no status |
| 5 | Quantidade sempre em kg (`peso_kg`) | O indicador de impacto é kg desviados do descarte. Itens em caixa ou unidade são registrados pelo peso estimado |
| 6 | Exclusão lógica (`ativo` = S/N) em Ofertante, Beneficiario e PontoDistribuicao | Eles têm histórico ligado (lotes e reservas). Apagar de verdade quebraria as FKs e o painel de impacto |
| 7 | Selo de parceiro como atributos de Ofertante (`possui_selo`, `data_selo`), com CHECK de consistência | O selo pertence a um ofertante; o CHECK impede selo sem data ou data sem selo. A regra para ganhá-lo fica para o documento de regras de negócio |
| 8 | Alertas gravados na classe Alerta, como aviso geral por lote (sem registrar cada destinatário). A proximidade dos interessados é simulada por bairro ou CEP | Mostra a funcionalidade de xepa no CRUD e alimenta o dashboard sem complicar o protótipo. Registrar o destinatário fica como evolução |
| 9 | Fotos guardadas só como URL | Arquivos no banco deixam tudo pesado; a URL basta para o protótipo |
| 10 | Conexão no schema do Thomas | Ele cria o backend; a Mariana Souza entrega diagrama e scripts, que rodam no mesmo schema |
| 11 | Endereço sem latitude e longitude | O mapa de SP usa dados simulados próprios; o banco guarda só o endereço legível |
| 12 | Telefone e endereço opcionais no Beneficiario | O cidadão principal é de baixa renda: pode não ter celular nem endereço fixo, como pessoas em situação de rua. O cadastro pode ser feito pelo ponto de distribuição |
| 13 | Tabelas sem exclusão lógica só são apagadas se não tiverem registros dependentes; caso contrário, a API devolve erro 409 com mensagem clara (regras por tabela no fim do documento) | O CRUD exige DELETE, e isso evita quebrar FKs e o histórico do painel de impacto |
| 14 | Lote ganha o campo opcional `valor_estimado` (valor de mercado em R$) | A documentação fiscal da doação exige descrição, quantidade e valor do que foi doado. Com esse campo, o relatório mensal de impacto sai pronto para a contabilidade do ofertante. É opcional porque o feirante nem sempre sabe o valor, e ele não tem direito à dedução |

Padrões: tabelas com prefixo `T_RP_` (ex.: `T_RP_LOTE`); no Java, o mesmo nome em camelCase (`Lote`, `idLote`). Chaves primárias são NUMBER gerado por IDENTITY e viram `Long` no Java. Peso e preço usam `BigDecimal`, que não perde centavos nem gramas em contas. Campos S/N são CHAR(1) no banco e `boolean` no Java.

## Endereco (T_RP_ENDERECO)

Guarda o endereço de ofertantes, beneficiários e pontos em um só lugar, para não repetir os mesmos campos em três tabelas.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_endereco | NUMBER (IDENTITY) | Long | Sim | PK |
| logradouro | VARCHAR2(100) | String | Sim | |
| numero | VARCHAR2(10) | String | Não | Texto, para aceitar "s/n" |
| complemento | VARCHAR2(50) | String | Não | Ex.: "Banca 12", "Box 3" |
| bairro | VARCHAR2(60) | String | Sim | Ex.: Paraíso, Piqueri |
| cidade | VARCHAR2(60) | String | Sim | |
| uf | CHAR(2) | String | Sim | |
| cep | CHAR(8) | String | Sim | Só números; texto para não perder o zero à esquerda |

## Ofertante (T_RP_OFERTANTE)

Quem tem o excedente: supermercado ou feirante. É o lado que registra os lotes.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_ofertante | NUMBER (IDENTITY) | Long | Sim | PK |
| nome | VARCHAR2(100) | String | Sim | Nome do mercado ou da banca |
| tipo | VARCHAR2(20) | TipoOfertante | Sim | CHECK: SUPERMERCADO, FEIRANTE |
| telefone | VARCHAR2(15) | String | Sim | Canal para avisos e coleta |
| email | VARCHAR2(100) | String | Não | Muitos feirantes não usam e-mail |
| possui_selo | CHAR(1) | boolean | Sim | Padrão 'N' |
| data_selo | DATE | LocalDate | Não | Preenchido quando ganha o selo; CHECK: 'S' exige data, 'N' exige vazio |
| ativo | CHAR(1) | boolean | Sim | Padrão 'S'; exclusão lógica |
| id_endereco | NUMBER | Endereco | Sim | FK; endereço do mercado ou da feira |

## CategoriaAlimento (T_RP_CATEGORIA_ALIMENTO)

Tabela de apoio para classificar os lotes (frutas, verduras, legumes, padaria, laticínios…). Permite que o dashboard agrupe kg por categoria.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_categoria | NUMBER (IDENTITY) | Long | Sim | PK |
| nome | VARCHAR2(50) | String | Sim | UNIQUE |
| descricao | VARCHAR2(200) | String | Não | |

## Lote (T_RP_LOTE)

O excedente registrado por um ofertante. É o centro do sistema: tudo (reserva, alerta, destino) gira em torno dele.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_lote | NUMBER (IDENTITY) | Long | Sim | PK |
| id_ofertante | NUMBER | Ofertante | Sim | FK |
| id_categoria | NUMBER | CategoriaAlimento | Sim | FK |
| descricao | VARCHAR2(200) | String | Sim | Ex.: "Caixa de tomates maduros" |
| peso_kg | NUMBER(8,2) | BigDecimal | Sim | CHECK > 0 |
| condicao | VARCHAR2(20) | CondicaoLote | Sim | CHECK: BOA, MADURA, CONSUMO_IMEDIATO |
| modalidade | VARCHAR2(15) | ModalidadeLote | Sim | CHECK: DOACAO, VENDA |
| preco | NUMBER(10,2) | BigDecimal | Não | Vazio na doação; maior que 0 na venda (CHECK da tabela) |
| valor_estimado | NUMBER(10,2) | BigDecimal | Não | Valor de mercado estimado do lote (CHECK ≥ 0). Usado no relatório mensal e na documentação fiscal da doação (decisão 14) |
| data_cadastro | DATE | LocalDateTime | Sim | Padrão SYSDATE |
| data_limite_retirada | DATE | LocalDateTime | Sim | CHECK ≥ data_cadastro |
| url_foto | VARCHAR2(255) | String | Não | Só o link (decisão 9) |
| precisa_transporte | CHAR(1) | boolean | Sim | Padrão 'N' |
| status | VARCHAR2(15) | StatusLote | Sim | Padrão DISPONIVEL; CHECK com os 4 status (decisão 4) |

Por que esses campos:

- **condicao** separa o que dá para guardar do que precisa ser consumido logo; ajuda o beneficiário a escolher.
- **modalidade + preco** cobrem os dois caminhos do alimento (doação e venda com desconto) sem criar duas classes.
- **valor_estimado** é diferente de **preco**: o preço é quanto o beneficiário paga na venda com desconto; o valor estimado é quanto o alimento vale no mercado, e existe também nas doações. No DML, preencher em todos os lotes de supermercado.
- **data_limite_retirada** é o que faz o lote passar para EXPIRADO, e também é a variável-chave da regressão logística ("o lote foi retirado a tempo?").
- **precisa_transporte** já deixa preparado o caminho da logística compartilhada, que é Could no MVP.

### Transições de status do lote

Implementadas no BO. A mudança de status e o registro da reserva acontecem na mesma transação.

| De | Para | Quando |
| --- | --- | --- |
| DISPONIVEL | RESERVADO | Uma reserva ATIVA é criada |
| RESERVADO | DISPONIVEL | A reserva é CANCELADA |
| RESERVADO | RETIRADO | A retirada é confirmada (a reserva vira RETIRADA) |
| DISPONIVEL | EXPIRADO | Passou de `data_limite_retirada` sem reserva |
| RESERVADO | EXPIRADO | Passou de `data_limite_retirada` sem retirada |

Um lote EXPIRADO pode receber uma Destinacao do tipo COMPOSTAGEM: o que não serve mais para consumo vira adubo.

## Beneficiario (T_RP_BENEFICIARIO)

Quem recebe ou compra o alimento. Inclui nosso cidadão principal: pessoas e famílias de baixa renda.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_beneficiario | NUMBER (IDENTITY) | Long | Sim | PK |
| nome | VARCHAR2(100) | String | Sim | Nome da pessoa, família ou instituição (sempre simulado) |
| tipo | VARCHAR2(20) | TipoBeneficiario | Sim | CHECK: PESSOA, FAMILIA, INSTITUICAO, PEQUENO_NEGOCIO |
| qtd_pessoas | NUMBER(4) | Integer | Não | CHECK > 0. PESSOA = 1; família = membros; instituição = pessoas atendidas |
| telefone | VARCHAR2(15) | String | Não | Opcional (decisão 12) |
| ativo | CHAR(1) | boolean | Sim | Padrão 'S'; exclusão lógica |
| id_endereco | NUMBER | Endereco | Não | FK opcional (decisão 12) |

Por que esses campos: **qtd_pessoas** permite estimar quantas pessoas cada lote alimentou, o que reforça o pitch e o indicador secundário (refeições). Não há CPF nem nenhum documento: o combinado da GS proíbe dados pessoais reais, e exigir documento afastaria justamente quem mais precisa.

## PontoDistribuicao (T_RP_PONTO_DISTRIBUICAO)

Local com a placa Rede de Prato onde o alimento é retirado: cozinha solidária, ONG ou associação. É o que garante acesso a quem não tem celular.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_ponto | NUMBER (IDENTITY) | Long | Sim | PK |
| nome | VARCHAR2(100) | String | Sim | |
| tipo | VARCHAR2(20) | TipoPonto | Sim | CHECK: COZINHA_SOLIDARIA, ONG, ASSOCIACAO |
| dias_funcionamento | VARCHAR2(50) | String | Sim | Ex.: "SEG a SEX" |
| horario_abertura | VARCHAR2(5) | LocalTime | Sim | Formato HH:MM; o Oracle não tem tipo só de hora |
| horario_fechamento | VARCHAR2(5) | LocalTime | Sim | Formato HH:MM |
| telefone | VARCHAR2(15) | String | Não | |
| ativo | CHAR(1) | boolean | Sim | Padrão 'S'; exclusão lógica |
| id_endereco | NUMBER | Endereco | Sim | FK; o ponto precisa de endereço fixo para a placa |

Por que os horários ficam aqui: eles aparecem na placa e no mapa, e é por eles que a pessoa sem internet sabe quando ir buscar o alimento.

## Reserva (T_RP_RESERVA)

Liga um lote a quem vai buscá-lo e ao ponto onde será retirado.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_reserva | NUMBER (IDENTITY) | Long | Sim | PK |
| id_lote | NUMBER | Lote | Sim | FK |
| id_beneficiario | NUMBER | Beneficiario | Sim | FK |
| id_ponto | NUMBER | PontoDistribuicao | Não | FK opcional; vazio quando a retirada é no local do ofertante |
| data_reserva | DATE | LocalDateTime | Sim | Padrão SYSDATE |
| data_retirada | DATE | LocalDateTime | Não | Preenchida na confirmação da retirada |
| status | VARCHAR2(15) | StatusReserva | Sim | Padrão ATIVA; CHECK: ATIVA, RETIRADA, CANCELADA |

Por que é uma classe própria: guardar reservas canceladas mostra o histórico. A regra "só uma reserva ATIVA por lote" fica no BO e num índice único no Oracle (decisão 3). O ponto é opcional porque a retirada também pode ser feita direto na banca ou no supermercado.

## Destinacao (T_RP_DESTINACAO)

Registra para onde o alimento foi no fim: consumo ou compostagem.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_destinacao | NUMBER (IDENTITY) | Long | Sim | PK |
| id_lote | NUMBER | Lote | Sim | FK; um lote pode ter vários destinos |
| tipo | VARCHAR2(15) | TipoDestinacao | Sim | CHECK: CONSUMO, COMPOSTAGEM |
| peso_kg | NUMBER(8,2) | BigDecimal | Sim | CHECK > 0; peso efetivamente aproveitado |
| data_destinacao | DATE | LocalDateTime | Sim | |

Por que é uma classe própria: é a fonte do indicador de impacto. Um lote pode ter mais de um destino (parte consumida e parte compostada); a soma dos pesos não pode passar do peso do lote (regra no BO). O painel soma `peso_kg` daqui, então os kg salvos são sempre calculados por consulta, nunca digitados à mão, e podem ser separados por tipo.

## Alerta (T_RP_ALERTA)

Registra cada aviso enviado sobre um lote: novo excedente ou xepa.

| Campo | Tipo Oracle | Tipo Java | Obrigatório | Observação |
| --- | --- | --- | --- | --- |
| id_alerta | NUMBER (IDENTITY) | Long | Sim | PK |
| id_lote | NUMBER | Lote | Sim | FK |
| tipo | VARCHAR2(15) | TipoAlerta | Sim | CHECK: NOVO_LOTE, XEPA |
| canal | VARCHAR2(15) | CanalAlerta | Sim | CHECK: APP, WHATSAPP (WhatsApp simulado no protótipo) |
| data_envio | DATE | LocalDateTime | Sim | Padrão SYSDATE |

Por que é uma classe própria: o alerta de xepa é Must no MVP, e gravá-lo deixa a funcionalidade visível no CRUD e no dashboard.

## Enums (listas fixas)

Cada lista fixa é um CHECK no Oracle e um enum no Java, com os mesmos valores escritos igual nos dois lados.

| Enum (Java) | Usado em | Valores |
| --- | --- | --- |
| TipoOfertante | Ofertante.tipo | SUPERMERCADO, FEIRANTE |
| CondicaoLote | Lote.condicao | BOA, MADURA, CONSUMO_IMEDIATO |
| ModalidadeLote | Lote.modalidade | DOACAO, VENDA |
| StatusLote | Lote.status | DISPONIVEL, RESERVADO, RETIRADO, EXPIRADO |
| TipoBeneficiario | Beneficiario.tipo | PESSOA, FAMILIA, INSTITUICAO, PEQUENO_NEGOCIO |
| TipoPonto | PontoDistribuicao.tipo | COZINHA_SOLIDARIA, ONG, ASSOCIACAO |
| StatusReserva | Reserva.status | ATIVA, RETIRADA, CANCELADA |
| TipoDestinacao | Destinacao.tipo | CONSUMO, COMPOSTAGEM |
| TipoAlerta | Alerta.tipo | NOVO_LOTE, XEPA |
| CanalAlerta | Alerta.canal | APP, WHATSAPP |

Por que enum e não tabela: são valores que só mudam se o sistema mudar. Como tabela, cada um exigiria mais um CRUD no Java e a justificativa de ter menos de 5 linhas no DML.

## Relacionamentos e cardinalidades

| De | Para | Cardinalidade | FK em | Por quê |
| --- | --- | --- | --- | --- |
| Ofertante | Lote | 1 : 0..N | Lote.id_ofertante | Um ofertante registra vários lotes ao longo do tempo; todo lote tem dono |
| CategoriaAlimento | Lote | 1 : 0..N | Lote.id_categoria | Todo lote pertence a uma categoria |
| Lote | Reserva | 1 : 0..N | Reserva.id_lote | Guarda o histórico de canceladas; só uma ATIVA por vez (BO + índice único) |
| Beneficiario | Reserva | 1 : 0..N | Reserva.id_beneficiario | Um beneficiário pode reservar vários lotes |
| PontoDistribuicao | Reserva | 0..1 : 0..N | Reserva.id_ponto (opcional) | A retirada pode ser no ponto ou direto no local do ofertante |
| Lote | Destinacao | 1 : 0..N | Destinacao.id_lote | Um lote pode ter parte consumida e parte compostada; a soma não passa do peso do lote |
| Lote | Alerta | 1 : 0..N | Alerta.id_lote | Um lote pode gerar o aviso de novo lote e depois o de xepa |
| Endereco | Ofertante | 1 : 0..N | Ofertante.id_endereco | Todo ofertante tem endereço; um endereço pode ser compartilhado (ex.: bancas da mesma feira) |
| Endereco | PontoDistribuicao | 1 : 0..N | PontoDistribuicao.id_endereco | O ponto precisa de endereço fixo para a placa |
| Endereco | Beneficiario | 0..1 : 0..N | Beneficiario.id_endereco (opcional) | Endereço não é obrigatório para o beneficiário (decisão 12) |

No Java, cada FK vira um atributo do tipo da outra classe (ex.: `Lote` tem um `Ofertante`), e não um `Long` solto. Isso deixa o diagrama de classes igual ao DER.

## Restrições extras no Oracle

Além das PKs, FKs e CHECKs de cada tabela, o script DDL inclui:

```sql
-- Só uma reserva ATIVA por lote (decisão 3)
CREATE UNIQUE INDEX UX_RP_RESERVA_ATIVA_LOTE
  ON T_RP_RESERVA (CASE WHEN status = 'ATIVA' THEN id_lote END);

-- Selo e data andam juntos (decisão 7)
ALTER TABLE T_RP_OFERTANTE ADD CONSTRAINT CK_RP_OFERTANTE_SELO
  CHECK ( (possui_selo = 'S' AND data_selo IS NOT NULL)
       OR (possui_selo = 'N' AND data_selo IS NULL) );
```

No Java, a camada de exceções converte o erro de chave duplicada do Oracle (ORA-00001) em resposta 409.

## Regras de exclusão (decisão 13)

| Tabela | Pode excluir quando |
| --- | --- |
| Lote | Não tem reserva nem destinação. Os alertas do lote são apagados junto (ver docs/decisoes.md item 6: reservas CANCELADAS também são apagadas junto) |
| CategoriaAlimento | Nenhum lote usa a categoria |
| Endereco | Nenhum ofertante, ponto ou beneficiário usa o endereço |
| Reserva | Só quando está CANCELADA. Uma reserva ATIVA é cancelada por PUT, o que devolve o lote para DISPONIVEL |
| Destinacao | Sempre (corrige lançamento errado) |
| Alerta | Sempre |
| Ofertante, Beneficiario, PontoDistribuicao | Nunca físico: exclusão lógica (`ativo` = 'N') |

## Observações de implementação

- **Horários:** no DAO, converter com `LocalTime.parse` ao ler e `toString` ao gravar; validar o formato HH:MM no BO.
- **API:** usar DTOs de resposta para não devolver JSON enorme com objetos aninhados (ex.: Lote → Ofertante → Endereco).
- **Volume de dados:** o notebook de Data Science lê o próprio CSV, então as centenas de lotes ficam no CSV. O DML precisa só do mínimo de 5 linhas por tabela (10 a 20 lotes), de preferência tirados do mesmo CSV, para os dados baterem.
- **Pontos e instituições:** PontoDistribuicao e Beneficiario do tipo INSTITUICAO são cadastros independentes no protótipo.

## Fica para depois

Alerta com destinatário, tipo DESCARTE em Destinacao, latitude e longitude, e preço original ou percentual de desconto no Lote. Nenhum é necessário para o MVP.
