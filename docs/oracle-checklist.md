# Checklist — quando o DDL da Mariana chegar

1. Copiar o arquivo dela, sem alterar, para `db/ddl.sql` (e `db/dml.sql` se houver). Atualizar só `db/README.md`.
2. Comparar o DDL com a tabela abaixo (nome da tabela, coluna, tipo, NULL/NOT NULL). Se algo for diferente, **a entidade muda para seguir o DDL** (`docs/decisoes.md`) — anotar cada diferença em `DECISIONS.md`.
3. Rodar o DDL no schema Oracle (SQL Developer).
4. Localmente: `export ORACLE_URL=... ORACLE_USERNAME=... ORACLE_PASSWORD=...` e `./mvnw spring-boot:run`. Desde a D-16, o perfil `oracle` é o padrão, então não precisa de `SPRING_PROFILES_ACTIVE`.
5. Se o Hibernate acusar `Schema-validation: missing column / wrong column type`, corrigir o `@Column(columnDefinition=...)` da entidade indicada e repetir.
   - Se o DDL usar `NUMBER(10)` nas PKs/FKs em vez de `NUMBER`, trocar `columnDefinition = "NUMBER"` pelo tipo do DDL.
   - Se o DDL não usar IDENTITY (usar sequence + trigger), trocar `@GeneratedValue(strategy = IDENTITY)` por `@GeneratedValue(strategy = SEQUENCE, generator = ...)` + `@SequenceGenerator(sequenceName = "<SEQ_DO_DDL>", allocationSize = 1)`.
   - Se algum CHECK de enum tiver valores diferentes, ajustar o enum Java (os nomes precisam ser idênticos).
6. Testar no navegador: reservar um lote duas vezes deve dar 409 (índice `UX_RP_RESERVA_ATIVA_LOTE` + regra no BO).
7. No Render, criar/confirmar `ORACLE_URL`, `ORACLE_USERNAME` e `ORACLE_PASSWORD`. Sem elas, a aplicação não sobe, porque o padrão é Oracle (D-16). Para uma demo só no H2, usar `SPRING_PROFILES_ACTIVE=dev`. Conferir `/api/health` → `"profile":"oracle"`.
8. Rodar o teste de fumaça contra o Oracle: `java tools/smoke/OracleSmokeTest.java` (ver `docs/run.md`, "Teste com Oracle").
9. Com o Oracle vazio, as telas ficam sem dados: carregar o DML da Mariana (mínimo 5 linhas por tabela) ou cadastrar pela API.

## Campo da entidade → coluna esperada

| Entidade.campo (Java) | Tabela.coluna | Tipo Oracle | Nulo? |
| --- | --- | --- | --- |
| Endereco.idEndereco | T_RP_ENDERECO.id_endereco | NUMBER (IDENTITY, PK) | não |
| Endereco.logradouro | T_RP_ENDERECO.logradouro | VARCHAR2(100) | não |
| Endereco.numero | T_RP_ENDERECO.numero | VARCHAR2(10) | sim |
| Endereco.complemento | T_RP_ENDERECO.complemento | VARCHAR2(50) | sim |
| Endereco.bairro | T_RP_ENDERECO.bairro | VARCHAR2(60) | não |
| Endereco.cidade | T_RP_ENDERECO.cidade | VARCHAR2(60) | não |
| Endereco.uf | T_RP_ENDERECO.uf | CHAR(2) | não |
| Endereco.cep | T_RP_ENDERECO.cep | CHAR(8) | não |
| Ofertante.idOfertante | T_RP_OFERTANTE.id_ofertante | NUMBER (IDENTITY, PK) | não |
| Ofertante.nome | T_RP_OFERTANTE.nome | VARCHAR2(100) | não |
| Ofertante.tipo | T_RP_OFERTANTE.tipo | VARCHAR2(20) | não |
| Ofertante.telefone | T_RP_OFERTANTE.telefone | VARCHAR2(15) | não |
| Ofertante.email | T_RP_OFERTANTE.email | VARCHAR2(100) | sim |
| Ofertante.possuiSelo | T_RP_OFERTANTE.possui_selo | CHAR(1) 'S'/'N' | não |
| Ofertante.dataSelo | T_RP_OFERTANTE.data_selo | DATE | sim |
| Ofertante.ativo | T_RP_OFERTANTE.ativo | CHAR(1) 'S'/'N' | não |
| Ofertante.endereco | T_RP_OFERTANTE.id_endereco | NUMBER (FK) | não |
| CategoriaAlimento.idCategoria | T_RP_CATEGORIA_ALIMENTO.id_categoria | NUMBER (IDENTITY, PK) | não |
| CategoriaAlimento.nome | T_RP_CATEGORIA_ALIMENTO.nome | VARCHAR2(50) UNIQUE | não |
| CategoriaAlimento.descricao | T_RP_CATEGORIA_ALIMENTO.descricao | VARCHAR2(200) | sim |
| Lote.idLote | T_RP_LOTE.id_lote | NUMBER (IDENTITY, PK) | não |
| Lote.ofertante | T_RP_LOTE.id_ofertante | NUMBER (FK) | não |
| Lote.categoria | T_RP_LOTE.id_categoria | NUMBER (FK) | não |
| Lote.descricao | T_RP_LOTE.descricao | VARCHAR2(200) | não |
| Lote.pesoKg | T_RP_LOTE.peso_kg | NUMBER(8,2) | não |
| Lote.condicao | T_RP_LOTE.condicao | VARCHAR2(20) | não |
| Lote.modalidade | T_RP_LOTE.modalidade | VARCHAR2(15) | não |
| Lote.preco | T_RP_LOTE.preco | NUMBER(10,2) | sim |
| Lote.valorEstimado | T_RP_LOTE.valor_estimado | NUMBER(10,2) | sim |
| Lote.dataCadastro | T_RP_LOTE.data_cadastro | DATE | não |
| Lote.dataLimiteRetirada | T_RP_LOTE.data_limite_retirada | DATE | não |
| Lote.urlFoto | T_RP_LOTE.url_foto | VARCHAR2(255) | sim |
| Lote.precisaTransporte | T_RP_LOTE.precisa_transporte | CHAR(1) 'S'/'N' | não |
| Lote.status | T_RP_LOTE.status | VARCHAR2(15) | não |
| Beneficiario.idBeneficiario | T_RP_BENEFICIARIO.id_beneficiario | NUMBER (IDENTITY, PK) | não |
| Beneficiario.nome | T_RP_BENEFICIARIO.nome | VARCHAR2(100) | não |
| Beneficiario.tipo | T_RP_BENEFICIARIO.tipo | VARCHAR2(20) | não |
| Beneficiario.qtdPessoas | T_RP_BENEFICIARIO.qtd_pessoas | NUMBER(4) | sim |
| Beneficiario.telefone | T_RP_BENEFICIARIO.telefone | VARCHAR2(15) | sim |
| Beneficiario.ativo | T_RP_BENEFICIARIO.ativo | CHAR(1) 'S'/'N' | não |
| Beneficiario.endereco | T_RP_BENEFICIARIO.id_endereco | NUMBER (FK) | sim |
| PontoDistribuicao.idPonto | T_RP_PONTO_DISTRIBUICAO.id_ponto | NUMBER (IDENTITY, PK) | não |
| PontoDistribuicao.nome | T_RP_PONTO_DISTRIBUICAO.nome | VARCHAR2(100) | não |
| PontoDistribuicao.tipo | T_RP_PONTO_DISTRIBUICAO.tipo | VARCHAR2(20) | não |
| PontoDistribuicao.diasFuncionamento | T_RP_PONTO_DISTRIBUICAO.dias_funcionamento | VARCHAR2(50) | não |
| PontoDistribuicao.horarioAbertura | T_RP_PONTO_DISTRIBUICAO.horario_abertura | VARCHAR2(5) "HH:MM" | não |
| PontoDistribuicao.horarioFechamento | T_RP_PONTO_DISTRIBUICAO.horario_fechamento | VARCHAR2(5) "HH:MM" | não |
| PontoDistribuicao.telefone | T_RP_PONTO_DISTRIBUICAO.telefone | VARCHAR2(15) | sim |
| PontoDistribuicao.ativo | T_RP_PONTO_DISTRIBUICAO.ativo | CHAR(1) 'S'/'N' | não |
| PontoDistribuicao.endereco | T_RP_PONTO_DISTRIBUICAO.id_endereco | NUMBER (FK) | não |
| Reserva.idReserva | T_RP_RESERVA.id_reserva | NUMBER (IDENTITY, PK) | não |
| Reserva.lote | T_RP_RESERVA.id_lote | NUMBER (FK) | não |
| Reserva.beneficiario | T_RP_RESERVA.id_beneficiario | NUMBER (FK) | não |
| Reserva.ponto | T_RP_RESERVA.id_ponto | NUMBER (FK) | sim |
| Reserva.dataReserva | T_RP_RESERVA.data_reserva | DATE | não |
| Reserva.dataRetirada | T_RP_RESERVA.data_retirada | DATE | sim |
| Reserva.status | T_RP_RESERVA.status | VARCHAR2(15) | não |
| Destinacao.idDestinacao | T_RP_DESTINACAO.id_destinacao | NUMBER (IDENTITY, PK) | não |
| Destinacao.lote | T_RP_DESTINACAO.id_lote | NUMBER (FK) | não |
| Destinacao.tipo | T_RP_DESTINACAO.tipo | VARCHAR2(15) | não |
| Destinacao.pesoKg | T_RP_DESTINACAO.peso_kg | NUMBER(8,2) | não |
| Destinacao.dataDestinacao | T_RP_DESTINACAO.data_destinacao | DATE | não |
| Alerta.idAlerta | T_RP_ALERTA.id_alerta | NUMBER (IDENTITY, PK) | não |
| Alerta.lote | T_RP_ALERTA.id_lote | NUMBER (FK) | não |
| Alerta.tipo | T_RP_ALERTA.tipo | VARCHAR2(15) | não |
| Alerta.canal | T_RP_ALERTA.canal | VARCHAR2(15) | não |
| Alerta.dataEnvio | T_RP_ALERTA.data_envio | DATE | não |
