-- ===================================================================
-- REDE DE PRATO - SCRIPT DML (ORACLE SQL)
-- Dados fictícios de exemplo alinhados às classes do backend.
-- Há no mínimo 5 registros em cada tabela.
-- Execute após o script DDL.
-- ===================================================================

-- ENDERECOS
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (1, 'Rua Manuel da Nobrega', '850', 'Banca 12', 'Paraiso', 'Sao Paulo', 'SP', '04001000');
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (2, 'Rua Abilio Soares', '420', NULL, 'Paraiso', 'Sao Paulo', 'SP', '04005000');
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (3, 'Rua Domingos de Morais', '1250', 'Sala 2', 'Vila Mariana', 'Sao Paulo', 'SP', '04010000');
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (4, 'Rua Pires da Mota', '100', NULL, 'Aclimacao', 'Sao Paulo', 'SP', '01529000');
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (5, 'Rua Galvao Bueno', '350', NULL, 'Liberdade', 'Sao Paulo', 'SP', '01506000');
INSERT INTO T_RP_ENDERECO (id_endereco, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES (6, 'Rua Vergueiro', '1750', 'Casa 4', 'Paraiso', 'Sao Paulo', 'SP', '04102000');

-- OFERTANTES
INSERT INTO T_RP_OFERTANTE (id_ofertante, nome, tipo, telefone, email, possui_selo, data_selo, ativo, id_endereco) VALUES (1, 'Feira Verde Paraiso', 'FEIRANTE', '11990000001', 'feiraverde@exemplo.com', 'S', DATE '2026-09-15', 'S', 1);
INSERT INTO T_RP_OFERTANTE (id_ofertante, nome, tipo, telefone, email, possui_selo, data_selo, ativo, id_endereco) VALUES (2, 'Supermercado Yaya', 'SUPERMERCADO', '11990000002', 'contato@yaya.com', 'S', DATE '2026-08-20', 'S', 2);
INSERT INTO T_RP_OFERTANTE (id_ofertante, nome, tipo, telefone, email, possui_selo, data_selo, ativo, id_endereco) VALUES (3, 'Hortifruti Boa Colheita', 'SUPERMERCADO', '11990000003', 'contato@boacolheita.com', 'N', NULL, 'S', 3);
INSERT INTO T_RP_OFERTANTE (id_ofertante, nome, tipo, telefone, email, possui_selo, data_selo, ativo, id_endereco) VALUES (4, 'Feira da Aclimacao', 'FEIRANTE', '11990000004', NULL, 'S', DATE '2026-10-01', 'S', 4);
INSERT INTO T_RP_OFERTANTE (id_ofertante, nome, tipo, telefone, email, possui_selo, data_selo, ativo, id_endereco) VALUES (5, 'Mercado Economico', 'SUPERMERCADO', '11990000005', 'contato@economico.com', 'N', NULL, 'S', 5);

-- CATEGORIAS DE ALIMENTO
INSERT INTO T_RP_CATEGORIA_ALIMENTO (id_categoria, nome, descricao) VALUES (1, 'FRUTAS', 'Frutas frescas proprias para consumo.');
INSERT INTO T_RP_CATEGORIA_ALIMENTO (id_categoria, nome, descricao) VALUES (2, 'VERDURAS', 'Folhas e verduras frescas.');
INSERT INTO T_RP_CATEGORIA_ALIMENTO (id_categoria, nome, descricao) VALUES (3, 'LEGUMES', 'Legumes e hortalicas.');
INSERT INTO T_RP_CATEGORIA_ALIMENTO (id_categoria, nome, descricao) VALUES (4, 'PADARIA', 'Paes e produtos de padaria embalados.');
INSERT INTO T_RP_CATEGORIA_ALIMENTO (id_categoria, nome, descricao) VALUES (5, 'LATICINIOS', 'Produtos lacteos proximos do vencimento.');

-- BENEFICIARIOS
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (1, 'Familia Ana Souza', 'FAMILIA', 4, '11991111111', 'S', 6);
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (2, 'Bruno Lima', 'PESSOA', 1, '11992222222', 'S', NULL);
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (3, 'Familia Carla Mendes', 'FAMILIA', 3, '11993333333', 'S', 4);
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (4, 'Cozinha Solidaria Esperanca', 'INSTITUICAO', 30, '11994444444', 'S', 3);
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (5, 'Mercadinho Popular', 'PEQUENO_NEGOCIO', 6, '11995555555', 'S', 5);
-- Beneficiaria ativa sem reserva: evidencia o LEFT OUTER JOIN da consulta 3.
INSERT INTO T_RP_BENEFICIARIO (id_beneficiario, nome, tipo, qtd_pessoas, telefone, ativo, id_endereco) VALUES (6, 'Elisa Ferreira', 'PESSOA', 1, NULL, 'S', NULL);

-- PONTOS DE DISTRIBUICAO
INSERT INTO T_RP_PONTO_DISTRIBUICAO (id_ponto, nome, tipo, dias_funcionamento, horario_abertura, horario_fechamento, telefone, ativo, id_endereco) VALUES (1, 'Cozinha Paraiso Solidario', 'COZINHA_SOLIDARIA', 'SEG a SEX', '09:00', '17:00', '11996666661', 'S', 6);
INSERT INTO T_RP_PONTO_DISTRIBUICAO (id_ponto, nome, tipo, dias_funcionamento, horario_abertura, horario_fechamento, telefone, ativo, id_endereco) VALUES (2, 'ONG Maos Unidas', 'ONG', 'SEG a SAB', '10:00', '18:00', '11996666662', 'S', 3);
INSERT INTO T_RP_PONTO_DISTRIBUICAO (id_ponto, nome, tipo, dias_funcionamento, horario_abertura, horario_fechamento, telefone, ativo, id_endereco) VALUES (3, 'Associacao Aclimacao Solidaria', 'ASSOCIACAO', 'QUA e SEX', '14:00', '19:00', '11996666663', 'S', 4);
INSERT INTO T_RP_PONTO_DISTRIBUICAO (id_ponto, nome, tipo, dias_funcionamento, horario_abertura, horario_fechamento, telefone, ativo, id_endereco) VALUES (4, 'Ponto Vila Mariana', 'ONG', 'TER e QUI', '13:00', '18:00', '11996666664', 'S', 3);
INSERT INTO T_RP_PONTO_DISTRIBUICAO (id_ponto, nome, tipo, dias_funcionamento, horario_abertura, horario_fechamento, telefone, ativo, id_endereco) VALUES (5, 'Ponto Liberdade Alimenta', 'ASSOCIACAO', 'SEG a SEX', '11:00', '16:00', '11996666665', 'S', 5);

-- LOTES DE ALIMENTOS
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (1, 1, 1, 'Caixa de bananas maduras', 12.00, 'MADURA', 'DOACAO', NULL, 36.00, DATE '2026-11-10', SYSDATE + 30, 'https://exemplo.com/lotes/banana.jpg', 'N', 'RETIRADO');
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (2, 2, 5, 'Leite UHT proximo do vencimento', 20.00, 'CONSUMO_IMEDIATO', 'VENDA', 2.50, 100.00, DATE '2026-11-10', SYSDATE + 30, NULL, 'N', 'RETIRADO');
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (3, 3, 3, 'Caixa de cenouras fora do padrao estetico', 10.00, 'BOA', 'DOACAO', NULL, 45.00, DATE '2026-11-11', SYSDATE + 30, NULL, 'S', 'RETIRADO');
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (4, 4, 2, 'Folhas e couve do fim da feira', 8.00, 'CONSUMO_IMEDIATO', 'DOACAO', NULL, 30.00, DATE '2026-11-12', SYSDATE + 30, NULL, 'N', 'RETIRADO');
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (5, 5, 4, 'Paes embalados do dia', 15.00, 'BOA', 'VENDA', 1.00, 45.00, DATE '2026-11-12', SYSDATE + 30, NULL, 'N', 'RESERVADO');
INSERT INTO T_RP_LOTE (id_lote, id_ofertante, id_categoria, descricao, peso_kg, condicao, modalidade, preco, valor_estimado, data_cadastro, data_limite_retirada, url_foto, precisa_transporte, status) VALUES (6, 2, 1, 'Morangos sem condicao de consumo', 7.00, 'CONSUMO_IMEDIATO', 'DOACAO', NULL, 28.00, DATE '2026-11-08', SYSDATE + 30, NULL, 'N', 'EXPIRADO');

-- RESERVAS: somente uma reserva ATIVA por lote, conforme indice unico condicional.
INSERT INTO T_RP_RESERVA (id_reserva, id_lote, id_beneficiario, id_ponto, data_reserva, data_retirada, status) VALUES (1, 1, 1, NULL, DATE '2026-11-10', DATE '2026-11-10', 'RETIRADA');
INSERT INTO T_RP_RESERVA (id_reserva, id_lote, id_beneficiario, id_ponto, data_reserva, data_retirada, status) VALUES (2, 2, 4, 2, DATE '2026-11-10', DATE '2026-11-11', 'RETIRADA');
INSERT INTO T_RP_RESERVA (id_reserva, id_lote, id_beneficiario, id_ponto, data_reserva, data_retirada, status) VALUES (3, 3, 2, 4, DATE '2026-11-11', DATE '2026-11-11', 'RETIRADA');
INSERT INTO T_RP_RESERVA (id_reserva, id_lote, id_beneficiario, id_ponto, data_reserva, data_retirada, status) VALUES (4, 4, 3, 3, DATE '2026-11-12', DATE '2026-11-12', 'RETIRADA');
INSERT INTO T_RP_RESERVA (id_reserva, id_lote, id_beneficiario, id_ponto, data_reserva, data_retirada, status) VALUES (5, 5, 5, 5, DATE '2026-11-12', NULL, 'ATIVA');

-- DESTINACOES: um lote pode possuir mais de uma destinacao; a soma dos pesos
-- nao pode ultrapassar o peso do lote (regra validada pelo backend).
INSERT INTO T_RP_DESTINACAO (id_destinacao, id_lote, tipo, peso_kg, data_destinacao) VALUES (1, 1, 'CONSUMO', 12.00, DATE '2026-11-10');
INSERT INTO T_RP_DESTINACAO (id_destinacao, id_lote, tipo, peso_kg, data_destinacao) VALUES (2, 2, 'CONSUMO', 20.00, DATE '2026-11-11');
INSERT INTO T_RP_DESTINACAO (id_destinacao, id_lote, tipo, peso_kg, data_destinacao) VALUES (3, 3, 'CONSUMO', 10.00, DATE '2026-11-11');
INSERT INTO T_RP_DESTINACAO (id_destinacao, id_lote, tipo, peso_kg, data_destinacao) VALUES (4, 4, 'CONSUMO', 8.00, DATE '2026-11-12');
INSERT INTO T_RP_DESTINACAO (id_destinacao, id_lote, tipo, peso_kg, data_destinacao) VALUES (5, 6, 'COMPOSTAGEM', 7.00, DATE '2026-11-10');

-- ALERTAS DE NOVO LOTE E XEPA
INSERT INTO T_RP_ALERTA (id_alerta, id_lote, tipo, canal, data_envio) VALUES (1, 1, 'NOVO_LOTE', 'WHATSAPP', DATE '2026-11-10');
INSERT INTO T_RP_ALERTA (id_alerta, id_lote, tipo, canal, data_envio) VALUES (2, 2, 'NOVO_LOTE', 'APP', DATE '2026-11-10');
INSERT INTO T_RP_ALERTA (id_alerta, id_lote, tipo, canal, data_envio) VALUES (3, 3, 'XEPA', 'WHATSAPP', DATE '2026-11-11');
INSERT INTO T_RP_ALERTA (id_alerta, id_lote, tipo, canal, data_envio) VALUES (4, 4, 'XEPA', 'APP', DATE '2026-11-12');
INSERT INTO T_RP_ALERTA (id_alerta, id_lote, tipo, canal, data_envio) VALUES (5, 5, 'NOVO_LOTE', 'WHATSAPP', DATE '2026-11-12');

COMMIT;

ALTER TABLE T_RP_ENDERECO MODIFY id_endereco GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_OFERTANTE MODIFY id_ofertante GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_CATEGORIA_ALIMENTO MODIFY id_categoria GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_BENEFICIARIO MODIFY id_beneficiario GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_PONTO_DISTRIBUICAO MODIFY id_ponto GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_LOTE MODIFY id_lote GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_RESERVA MODIFY id_reserva GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_DESTINACAO MODIFY id_destinacao GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE T_RP_ALERTA MODIFY id_alerta GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
