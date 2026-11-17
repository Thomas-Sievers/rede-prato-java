# 11 — Seed data · Design

`config/DataSeeder` — `CommandLineRunner`, `@Profile("!oracle")`, `@ConditionalOnProperty(app.seed.enabled=true)`, `@Transactional run()`; skips when `categoriaDAO.count() > 0`.

| Table | Rows | Notes |
| --- | --- | --- |
| Categoria | 6 | Frutas, Verduras, Legumes, Padaria, Laticínios, Outros |
| Endereço | 10 | Paraíso / Vila Mariana, São Paulo-SP |
| Ofertante | 5 | Banca do Seu Antônio (FEIRANTE, selo 10 days ago), Banca da Dona Lúcia (FEIRANTE), Hortifrúti do Paraíso (SUPERMERCADO, selo 30 days ago), Mercado Bom Preço (SUPERMERCADO), Sacolão Vila Mariana (SUPERMERCADO, inactive) |
| Beneficiário | 6 | Rosana (FAMILIA 4), Seu Jorge (PESSOA 1), Família Souza (FAMILIA 5), Cozinha Solidária do Bairro (INSTITUICAO 60), Lanchonete da Esquina (PEQUENO_NEGOCIO 3), Maria (PESSOA 1, no phone/address) |
| Ponto | 3 | Ponto Paraíso (ASSOCIACAO, TER e QUI 16:00–18:00), Cozinha Solidária do Bairro (COZINHA_SOLIDARIA, SEG a SEX 11:00–14:00), ONG Prato Cheio (ONG, SAB 09:00–12:00) |
| Lote | 19 | 8 DISPONIVEL, 3 RESERVADO, 6 RETIRADO (1–20 days ago), 2 EXPIRADO |
| Reserva | 10 | 3 ATIVA, 6 RETIRADA, 1 CANCELADA |
| Destinação | 10 | CONSUMO/COMPOSTAGEM on the RETIRADO lots + 1 COMPOSTAGEM on an EXPIRADO lot |
| Alerta | 21 | 19 NOVO_LOTE + 2 XEPA |

Test: `DataSeederTest` (`@SpringBootTest`) checks the counts per status and idempotency. (Other integration tests add rows to the same H2 context, so counts are asserted with `>=` where needed.)
