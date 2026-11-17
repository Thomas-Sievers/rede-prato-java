# Rede de Prato — DER (modelo do banco)

> O DER oficial é o diagrama do **Oracle Data Modeler** feito pela Mariana Souza França (Database Design), junto com o DDL em `db/`. Este diagrama em Mermaid foi feito a partir de `docs/classes.md` e das anotações das entidades Java, para conferir que o código bate com o banco.

```mermaid
erDiagram
    T_RP_ENDERECO {
        NUMBER id_endereco PK
        VARCHAR2_100 logradouro "NOT NULL"
        VARCHAR2_10 numero
        VARCHAR2_50 complemento
        VARCHAR2_60 bairro "NOT NULL"
        VARCHAR2_60 cidade "NOT NULL"
        CHAR_2 uf "NOT NULL"
        CHAR_8 cep "NOT NULL"
    }
    T_RP_OFERTANTE {
        NUMBER id_ofertante PK
        VARCHAR2_100 nome "NOT NULL"
        VARCHAR2_20 tipo "CHECK SUPERMERCADO, FEIRANTE"
        VARCHAR2_15 telefone "NOT NULL"
        VARCHAR2_100 email
        CHAR_1 possui_selo "S/N, default N"
        DATE data_selo "CK_RP_OFERTANTE_SELO"
        CHAR_1 ativo "S/N, default S"
        NUMBER id_endereco FK "NOT NULL"
    }
    T_RP_CATEGORIA_ALIMENTO {
        NUMBER id_categoria PK
        VARCHAR2_50 nome "UNIQUE"
        VARCHAR2_200 descricao
    }
    T_RP_LOTE {
        NUMBER id_lote PK
        NUMBER id_ofertante FK "NOT NULL"
        NUMBER id_categoria FK "NOT NULL"
        VARCHAR2_200 descricao "NOT NULL"
        NUMBER_8_2 peso_kg "CHECK > 0"
        VARCHAR2_20 condicao "CHECK BOA, MADURA, CONSUMO_IMEDIATO"
        VARCHAR2_15 modalidade "CHECK DOACAO, VENDA"
        NUMBER_10_2 preco "null na doacao, > 0 na venda"
        NUMBER_10_2 valor_estimado "CHECK >= 0"
        DATE data_cadastro "default SYSDATE"
        DATE data_limite_retirada "CHECK >= data_cadastro"
        VARCHAR2_255 url_foto
        CHAR_1 precisa_transporte "S/N, default N"
        VARCHAR2_15 status "CHECK 4 status, default DISPONIVEL"
    }
    T_RP_BENEFICIARIO {
        NUMBER id_beneficiario PK
        VARCHAR2_100 nome "NOT NULL"
        VARCHAR2_20 tipo "CHECK PESSOA, FAMILIA, INSTITUICAO, PEQUENO_NEGOCIO"
        NUMBER_4 qtd_pessoas "CHECK > 0"
        VARCHAR2_15 telefone
        CHAR_1 ativo "S/N, default S"
        NUMBER id_endereco FK "opcional"
    }
    T_RP_PONTO_DISTRIBUICAO {
        NUMBER id_ponto PK
        VARCHAR2_100 nome "NOT NULL"
        VARCHAR2_20 tipo "CHECK COZINHA_SOLIDARIA, ONG, ASSOCIACAO"
        VARCHAR2_50 dias_funcionamento "NOT NULL"
        VARCHAR2_5 horario_abertura "HH:MM"
        VARCHAR2_5 horario_fechamento "HH:MM"
        VARCHAR2_15 telefone
        CHAR_1 ativo "S/N, default S"
        NUMBER id_endereco FK "NOT NULL"
    }
    T_RP_RESERVA {
        NUMBER id_reserva PK "numero de retirada"
        NUMBER id_lote FK "NOT NULL"
        NUMBER id_beneficiario FK "NOT NULL"
        NUMBER id_ponto FK "opcional"
        DATE data_reserva "default SYSDATE"
        DATE data_retirada
        VARCHAR2_15 status "CHECK ATIVA, RETIRADA, CANCELADA"
    }
    T_RP_DESTINACAO {
        NUMBER id_destinacao PK
        NUMBER id_lote FK "NOT NULL"
        VARCHAR2_15 tipo "CHECK CONSUMO, COMPOSTAGEM"
        NUMBER_8_2 peso_kg "CHECK > 0"
        DATE data_destinacao "NOT NULL"
    }
    T_RP_ALERTA {
        NUMBER id_alerta PK
        NUMBER id_lote FK "NOT NULL"
        VARCHAR2_15 tipo "CHECK NOVO_LOTE, XEPA"
        VARCHAR2_15 canal "CHECK APP, WHATSAPP"
        DATE data_envio "default SYSDATE"
    }

    T_RP_ENDERECO ||--o{ T_RP_OFERTANTE : "endereco do ofertante"
    T_RP_ENDERECO |o--o{ T_RP_BENEFICIARIO : "endereco opcional"
    T_RP_ENDERECO ||--o{ T_RP_PONTO_DISTRIBUICAO : "endereco da placa"
    T_RP_OFERTANTE ||--o{ T_RP_LOTE : "registra"
    T_RP_CATEGORIA_ALIMENTO ||--o{ T_RP_LOTE : "classifica"
    T_RP_LOTE ||--o{ T_RP_RESERVA : "e reservado"
    T_RP_BENEFICIARIO ||--o{ T_RP_RESERVA : "reserva"
    T_RP_PONTO_DISTRIBUICAO |o--o{ T_RP_RESERVA : "local de retirada"
    T_RP_LOTE ||--o{ T_RP_DESTINACAO : "vai para"
    T_RP_LOTE ||--o{ T_RP_ALERTA : "gera aviso"
```

(Mermaid não aceita parênteses nem vírgulas no tipo: `VARCHAR2_100` = `VARCHAR2(100)`, `NUMBER_8_2` = `NUMBER(8,2)`, `CHAR_1` = `CHAR(1)`.)

## Restrições extras (do DDL)

```sql
-- Só uma reserva ATIVA por lote (decisão 3) → ORA-00001 vira 409 na API
CREATE UNIQUE INDEX UX_RP_RESERVA_ATIVA_LOTE
  ON T_RP_RESERVA (CASE WHEN status = 'ATIVA' THEN id_lote END);

-- Selo e data andam juntos (decisão 7)
ALTER TABLE T_RP_OFERTANTE ADD CONSTRAINT CK_RP_OFERTANTE_SELO
  CHECK ( (possui_selo = 'S' AND data_selo IS NOT NULL) OR (possui_selo = 'N' AND data_selo IS NULL) );
```

## Cardinalidades

| De | Para | Cardinalidade | FK |
| --- | --- | --- | --- |
| Endereço | Ofertante | 1 : 0..N | `T_RP_OFERTANTE.id_endereco` |
| Endereço | Beneficiário | 0..1 : 0..N | `T_RP_BENEFICIARIO.id_endereco` (opcional) |
| Endereço | Ponto de distribuição | 1 : 0..N | `T_RP_PONTO_DISTRIBUICAO.id_endereco` |
| Ofertante | Lote | 1 : 0..N | `T_RP_LOTE.id_ofertante` |
| Categoria | Lote | 1 : 0..N | `T_RP_LOTE.id_categoria` |
| Lote | Reserva | 1 : 0..N (só 1 ATIVA) | `T_RP_RESERVA.id_lote` |
| Beneficiário | Reserva | 1 : 0..N | `T_RP_RESERVA.id_beneficiario` |
| Ponto | Reserva | 0..1 : 0..N | `T_RP_RESERVA.id_ponto` (opcional) |
| Lote | Destinação | 1 : 0..N | `T_RP_DESTINACAO.id_lote` |
| Lote | Alerta | 1 : 0..N | `T_RP_ALERTA.id_lote` |
