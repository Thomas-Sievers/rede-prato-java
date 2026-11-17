-- ===================================================================
-- REDE DE PRATO - SCRIPT DQL (ORACLE SQL)
-- Consultas exigidas na atividade, alinhadas ao modelo do backend.
-- Execute após os scripts DDL e DML.
-- ===================================================================

-- 1) INNER JOIN ENTRE DUAS TABELAS
-- Lista cada lote com o nome e o tipo de seu ofertante.
-- O INNER JOIN retorna apenas lotes que possuem ofertante correspondente.
SELECT
    l.id_lote,
    l.descricao AS descricao_lote,
    l.peso_kg,
    l.modalidade,
    l.status AS status_lote,
    o.nome AS nome_ofertante,
    o.tipo AS tipo_ofertante
FROM T_RP_LOTE l
INNER JOIN T_RP_OFERTANTE o
    ON l.id_ofertante = o.id_ofertante
ORDER BY o.nome, l.id_lote;


-- 2) INNER JOIN ENTRE DUAS TABELAS COM FUNCOES DE GRUPO E GROUP BY
-- Mostra, para cada ofertante, a quantidade de lotes cadastrados e
-- o total de quilogramas disponibilizados pela plataforma.
SELECT
    o.id_ofertante,
    o.nome AS nome_ofertante,
    COUNT(l.id_lote) AS quantidade_lotes,
    SUM(l.peso_kg) AS total_kg_disponibilizados,
    ROUND(AVG(l.peso_kg), 2) AS media_kg_por_lote
FROM T_RP_OFERTANTE o
INNER JOIN T_RP_LOTE l
    ON o.id_ofertante = l.id_ofertante
GROUP BY
    o.id_ofertante,
    o.nome
ORDER BY total_kg_disponibilizados DESC;


-- 3) LEFT OUTER JOIN ENTRE DUAS TABELAS COM WHERE E ORDER BY
-- Lista os beneficiarios ativos, inclusive aqueles que ainda nao fizeram reserva.
-- O WHERE filtra os beneficiarios ativos; ORDER BY organiza por nome.
-- A beneficiaria Elisa Ferreira foi inserida sem reserva justamente para que
-- os campos da reserva aparecam nulos, demonstrando o LEFT OUTER JOIN.
SELECT
    b.id_beneficiario,
    b.nome AS nome_beneficiario,
    b.tipo AS tipo_beneficiario,
    b.qtd_pessoas,
    r.id_reserva,
    r.data_reserva,
    r.status AS status_reserva
FROM T_RP_BENEFICIARIO b
LEFT OUTER JOIN T_RP_RESERVA r
    ON b.id_beneficiario = r.id_beneficiario
WHERE b.ativo = 'S'
ORDER BY b.nome, r.data_reserva;