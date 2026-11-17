# 18 — Extras · Design

(a) Dependency `org.springdoc:springdoc-openapi-starter-webmvc-ui` (3.x line, Boot 4 compatible). Not covered by the H2 tests beyond context load + `/v3/api-docs` 200.

(b) `RelatorioController` `GET /api/relatorios/ofertantes/{id}/mensal?mes=` → `RelatorioBO.mensal(id, mes)` using `DestinacaoDAO.doOfertanteNoPeriodo(idOfertante, inicio, fim)` (join fetch lote + categoria). Grouping per lote in the BO.

```json
{ "idOfertante": 3, "nomeOfertante": "Hortifrúti do Paraíso", "mes": "2026-11",
  "itens": [ { "idLote": 9, "descricao": "Maçãs", "categoria": "Frutas", "data": "2026-10-28T19:00:00",
               "pesoKg": 7.0, "valorEstimado": 30.0, "consumoKg": 7.0, "compostagemKg": 0.0 } ],
  "totalKg": 7.0, "consumoKg": 7.0, "compostagemKg": 0.0, "valorEstimadoTotal": 30.0 }
```
`data` = latest destinação date of that lote in the month.

(c) `LoteDAO.filtrar` gains `precisaTransporte` (Boolean, nullable); `LoteController` `?precisaTransporte=`.

(d) `index.html` section `#onde` filled from `GET /api/pontos?ativo=true`.
