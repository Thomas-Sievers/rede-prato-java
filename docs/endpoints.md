# Rede de Prato — Tabela de endpoints (API REST)

Base: `https://rede-prato-java.onrender.com` (ou `http://localhost:8080`). Todos os caminhos começam com `/api`, recebem e devolvem JSON. Datas no formato `2026-11-17T18:00:00`, horários `"16:00"`, pesos em kg e valores em R$ como números. Enums escritos exatamente como no Java (`"DOACAO"`, `"DISPONIVEL"`).

**Padrão de respostas:** `POST` → `201 Created` + cabeçalho `Location` + corpo · `PUT` → `200 OK` + corpo · `DELETE` → `204 No Content` · `GET` de lista → `200` (lista vazia, nunca 404).

**Erros** (camada de exceções, corpo sempre igual):

```json
{ "timestamp": "2026-11-17T10:15:30-03:00", "status": 409, "error": "Conflict",
  "message": "Este lote já foi reservado por outra pessoa.", "path": "/api/reservas", "fields": null }
```

| Status | Quando |
| --- | --- |
| 400 | regra de negócio (`BusinessRuleException`), JSON mal formado, parâmetro com tipo errado, restrição do banco (CHECK) |
| 404 | recurso não encontrado (`NotFoundException`), rota inexistente |
| 405 | método HTTP não permitido na rota |
| 409 | conflito de estado (`ConflictException`), registro duplicado (ORA-00001), exclusão com registros ligados (ORA-02292) |
| 500 | erro inesperado (`"Erro interno. Tente novamente."`, sem detalhes técnicos) |

Total: **57 endpoints** em 11 controllers (GET 23 · POST 12 · PUT 12 · DELETE 10).

## Tabela completa

| # | Método | Caminho | Descrição | Exemplo de requisição | Sucesso | Erros |
| --- | --- | --- | --- | --- | --- | --- |
| **Saúde** | | | | | | |
| 1 | `GET` | `/api/health` | Verifica se a API está no ar (usado pelo Render) | — | 200 `{"status":"UP","app":"Rede de Prato","profile":"default","time":"..."}` | — |
| **Categorias de alimento** (HU19) | | | | | | |
| 2 | `GET` | `/api/categorias` | Lista as categorias em ordem de nome | — | 200 | — |
| 3 | `GET` | `/api/categorias/{id}` | Busca uma categoria | — | 200 | 404 |
| 4 | `POST` | `/api/categorias` | Cria categoria (nome único) | `{"nome":"Frutas","descricao":"Frutas frescas"}` | 201 | 400, 409 |
| 5 | `PUT` | `/api/categorias/{id}` | Altera categoria | `{"nome":"Frutas","descricao":"Frutas maduras"}` | 200 | 400, 404, 409 |
| 6 | `DELETE` | `/api/categorias/{id}` | Exclui categoria sem lotes | — | 204 | 404, 409 |
| **Endereços** | | | | | | |
| 7 | `GET` | `/api/enderecos` | Lista endereços (`?bairro=Paraíso`) | — | 200 | — |
| 8 | `GET` | `/api/enderecos/{id}` | Busca um endereço | — | 200 | 404 |
| 9 | `POST` | `/api/enderecos` | Cria endereço (CEP 8 dígitos, UF válida) | `{"logradouro":"Rua Tutóia","numero":"210","complemento":null,"bairro":"Paraíso","cidade":"São Paulo","uf":"SP","cep":"04007-000"}` | 201 | 400 |
| 10 | `PUT` | `/api/enderecos/{id}` | Altera endereço | mesmo corpo do POST | 200 | 400, 404 |
| 11 | `DELETE` | `/api/enderecos/{id}` | Exclui endereço que ninguém usa | — | 204 | 404, 409 |
| **Ofertantes** (HU01, HU18, HU20) | | | | | | |
| 12 | `GET` | `/api/ofertantes` | Lista ofertantes (`?tipo=FEIRANTE&ativo=true&bairro=Paraíso`; padrão: ativos) | — | 200 | 400 |
| 13 | `GET` | `/api/ofertantes/{id}` | Busca ofertante com endereço e selo | — | 200 | 404 |
| 14 | `POST` | `/api/ofertantes` | Cadastra supermercado ou feirante | `{"nome":"Banca do Seu Antônio","tipo":"FEIRANTE","telefone":"(11) 90000-0001","email":null,"idEndereco":1}` | 201 | 400, 404 |
| 15 | `PUT` | `/api/ofertantes/{id}` | Altera dados (e reativa com `"ativo":true`) | `{"nome":"Banca do Seu Antônio","tipo":"FEIRANTE","telefone":"11900000001","idEndereco":1,"ativo":true}` | 200 | 400, 404 |
| 16 | `DELETE` | `/api/ofertantes/{id}` | Exclusão lógica (`ativo = 'N'`) | — | 204 | 404 |
| 17 | `PUT` | `/api/ofertantes/{id}/selo` | Concede o Selo de Parceiro (data = hoje) | — | 200 | 400 (inativo), 404, 409 (já tem) |
| 18 | `DELETE` | `/api/ofertantes/{id}/selo` | Retira o selo | — | 204 | 404 |
| **Lotes** (HU02, HU03, HU09, HU14, HU21) | | | | | | |
| 19 | `GET` | `/api/lotes` | Lista lotes, mais novos primeiro (`?status=&idOfertante=&idCategoria=&modalidade=`) | — | 200 | 400 |
| 20 | `GET` | `/api/lotes/disponiveis` | Só lotes DISPONÍVEIS no prazo de ofertantes ativos, por prazo (`?idCategoria=&modalidade=&bairro=`); roda a expiração antes | — | 200 | 400 |
| 21 | `GET` | `/api/lotes/{id}` | Busca lote com ofertante, endereço e categoria | — | 200 | 404 |
| 22 | `POST` | `/api/lotes` | Registra sobra (status DISPONIVEL) e cria o alerta NOVO_LOTE | `{"idOfertante":1,"idCategoria":3,"descricao":"Tomates maduros","pesoKg":8.5,"condicao":"MADURA","modalidade":"DOACAO","preco":null,"valorEstimado":40.0,"dataLimiteRetirada":"2026-11-17T18:00:00","urlFoto":null,"precisaTransporte":false}` | 201 | 400, 404 |
| 23 | `PUT` | `/api/lotes/{id}` | Altera lote ainda DISPONÍVEL | mesmo corpo do POST | 200 | 400, 404, 409 |
| 24 | `DELETE` | `/api/lotes/{id}` | Cancela lote (apaga junto reservas canceladas e alertas) | — | 204 | 404, 409 (reserva ativa/retirada ou destino) |
| 25 | `POST` | `/api/lotes/expirar` | Roda a expiração agora (demo/admin) | — | 200 `{"expirados":2}` | — |
| 26 | `POST` | `/api/lotes/{id}/destinacoes` | Registra comida e adubo de um lote de uma vez | `{"consumoKg":7,"compostagemKg":1.5}` | 201 | 400, 404 |
| **Beneficiários** (HU07, HU18) | | | | | | |
| 27 | `GET` | `/api/beneficiarios` | Lista beneficiários (`?tipo=FAMILIA&ativo=true`; padrão: ativos) | — | 200 | 400 |
| 28 | `GET` | `/api/beneficiarios/{id}` | Busca beneficiário | — | 200 | 404 |
| 29 | `POST` | `/api/beneficiarios` | Cadastra sem documento (telefone e endereço opcionais) | `{"nome":"Rosana","tipo":"FAMILIA","qtdPessoas":4,"telefone":null,"idEndereco":null}` | 201 | 400, 404 |
| 30 | `PUT` | `/api/beneficiarios/{id}` | Altera (e reativa com `"ativo":true`) | mesmo corpo do POST | 200 | 400, 404 |
| 31 | `DELETE` | `/api/beneficiarios/{id}` | Exclusão lógica | — | 204 | 404 |
| **Pontos de distribuição** (HU16, HU17) | | | | | | |
| 32 | `GET` | `/api/pontos` | Lista pontos com placa (`?ativo=true&bairro=`) | — | 200 | — |
| 33 | `GET` | `/api/pontos/{id}` | Busca ponto | — | 200 | 404 |
| 34 | `POST` | `/api/pontos` | Cadastra ponto (horários HH:MM) | `{"nome":"Ponto Paraíso","tipo":"ASSOCIACAO","diasFuncionamento":"TER e QUI","horarioAbertura":"16:00","horarioFechamento":"18:00","telefone":null,"idEndereco":6}` | 201 | 400, 404 |
| 35 | `PUT` | `/api/pontos/{id}` | Altera ponto (e reativa) | mesmo corpo do POST | 200 | 400, 404 |
| 36 | `DELETE` | `/api/pontos/{id}` | Exclusão lógica | — | 204 | 404 |
| **Reservas** (HU10, HU11, HU12, HU13) | | | | | | |
| 37 | `GET` | `/api/reservas` | Lista reservas, mais novas primeiro (`?idBeneficiario=&idOfertante=&status=&idLote=`) | — | 200 | 400 |
| 38 | `GET` | `/api/reservas/{id}` | Confirmação: número de retirada, local, horário e prazo | — | 200 | 404 |
| 39 | `POST` | `/api/reservas` | Reserva o lote inteiro (lote → RESERVADO na mesma transação) | `{"idLote":7,"idBeneficiario":1,"idPonto":null}` | 201 | 400 (prazo, inativo), 404, 409 (já reservado) |
| 40 | `PUT` | `/api/reservas/{id}` | Troca o local de retirada de uma reserva ATIVA | `{"idPonto":2}` | 200 | 400, 404, 409 |
| 41 | `PUT` | `/api/reservas/{id}/cancelar` | Cancela reserva ATIVA (lote volta a DISPONÍVEL) | — | 200 | 404, 409 |
| 42 | `PUT` | `/api/reservas/{id}/retirada` | Confirma a retirada (reserva RETIRADA, lote RETIRADO) | — | 200 | 404, 409 |
| 43 | `DELETE` | `/api/reservas/{id}` | Exclui reserva CANCELADA | — | 204 | 404, 409 |
| **Destinações** (HU14, HU15) | | | | | | |
| 44 | `GET` | `/api/destinacoes` | Lista destinos (`?idLote=&tipo=CONSUMO`) | — | 200 | 400 |
| 45 | `GET` | `/api/destinacoes/{id}` | Busca destino | — | 200 | 404 |
| 46 | `POST` | `/api/destinacoes` | Registra consumo ou compostagem (soma ≤ peso do lote) | `{"idLote":7,"tipo":"CONSUMO","pesoKg":7.0,"dataDestinacao":null}` | 201 | 400, 404 |
| 47 | `PUT` | `/api/destinacoes/{id}` | Corrige destino | mesmo corpo do POST | 200 | 400, 404 |
| 48 | `DELETE` | `/api/destinacoes/{id}` | Exclui destino (sempre permitido) | — | 204 | 404 |
| **Alertas** (HU04, HU05) | | | | | | |
| 49 | `POST` | `/api/alertas/xepa` | Feirante avisa a xepa: um alerta XEPA por lote disponível | `{"idOfertante":1,"idsLotes":[],"novoPrazo":"2026-11-17T14:00:00","canal":"APP"}` | 201 `{"quantidade":2,"mensagem":"Xepa avisada para 2 lotes.","alertas":[...]}` | 400, 404 |
| 50 | `GET` | `/api/alertas/recentes` | Avisos das últimas horas de lotes ainda disponíveis (`?horas=24`) | — | 200 | 400 |
| 51 | `GET` | `/api/alertas` | Lista alertas (`?tipo=XEPA&idLote=`) | — | 200 | 400 |
| 52 | `GET` | `/api/alertas/{id}` | Busca alerta | — | 200 | 404 |
| 53 | `POST` | `/api/alertas` | Cria alerta manual | `{"idLote":7,"tipo":"NOVO_LOTE","canal":"WHATSAPP"}` | 201 | 400, 404 |
| 54 | `PUT` | `/api/alertas/{id}` | Altera tipo/canal | `{"tipo":"XEPA","canal":"APP"}` | 200 | 400, 404 |
| 55 | `DELETE` | `/api/alertas/{id}` | Exclui alerta ("Desfazer") | — | 204 | 404 |
| **Impacto** (HU15) | | | | | | |
| 56 | `GET` | `/api/impacto/ofertantes/{id}` | Painel do ofertante no mês (`?mes=2026-11`): kg, % comida, lotes, famílias, 4 semanas, selo | — | 200 | 400, 404 |
| 57 | `GET` | `/api/impacto/rede` | Totais da rede no mês + ranking de ofertantes (`?mes=2026-11`) | — | 200 | 400 |

## Exemplo de resposta — confirmação de reserva (`GET /api/reservas/{id}`)

```json
{ "idReserva": 12, "numeroRetirada": 12, "status": "ATIVA", "dataReserva": "2026-11-17T10:05:00", "dataRetirada": null,
  "idLote": 7, "descricaoLote": "Tomates maduros", "pesoKg": 8.5, "condicao": "MADURA", "modalidade": "DOACAO",
  "preco": null, "valorEstimado": 40.0, "pagamentoNaRetirada": false, "dataLimiteRetirada": "2026-11-17T18:00:00",
  "statusLote": "RESERVADO", "nomeCategoria": "Legumes", "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio",
  "telefoneOfertante": "11900000001", "enderecoOfertante": "Rua Manuel da Nóbrega, s/n - Feira - Banca 12 - Paraíso",
  "bairroOfertante": "Paraíso", "localRetirada": "PONTO", "idPonto": 1, "nomePonto": "Ponto Paraíso",
  "enderecoPonto": "Rua Desembargador Eliseu Guilherme, 88 - Salão da associação - Paraíso",
  "diasFuncionamento": "TER e QUI", "horarioAbertura": "16:00", "horarioFechamento": "18:00",
  "idBeneficiario": 1, "nomeBeneficiario": "Rosana", "tipoBeneficiario": "FAMILIA", "qtdPessoas": 4 }
```

## Exemplo de resposta — painel (`GET /api/impacto/ofertantes/1?mes=2026-11`)

```json
{ "idOfertante": 1, "nomeOfertante": "Banca do Seu Antônio", "mes": "2026-11",
  "totalKg": 26.00, "consumoKg": 23.00, "compostagemKg": 3.00, "percentualConsumo": 88.5,
  "lotesRetirados": 3, "beneficiariosAtendidos": 3, "pessoasAtendidas": 69,
  "porSemana": [ {"inicio":"2026-10-26","fim":"2026-11-01","kg":0.00}, {"inicio":"2026-11-02","fim":"2026-11-08","kg":5.00},
                 {"inicio":"2026-11-09","fim":"2026-11-15","kg":21.00}, {"inicio":"2026-11-16","fim":"2026-11-22","kg":0.00} ],
  "possuiSelo": true, "dataSelo": "2026-11-07" }
```
