# 06 — Pontos de distribuição · Design

## Entity `PontoDistribuicao` → `T_RP_PONTO_DISTRIBUICAO`

| Field | Column | Type |
| --- | --- | --- |
| idPonto | id_ponto | NUMBER identity PK |
| nome | nome | VARCHAR2(100) not null |
| tipo | tipo | VARCHAR2(20) not null, `TipoPonto` |
| diasFuncionamento | dias_funcionamento | VARCHAR2(50) not null |
| horarioAbertura | horario_abertura | VARCHAR2(5) not null, `LocalTime` + `LocalTimeHHMMConverter` |
| horarioFechamento | horario_fechamento | VARCHAR2(5) not null, idem |
| telefone | telefone | VARCHAR2(15) |
| ativo | ativo | CHAR(1) not null S/N |
| endereco | id_endereco | NUMBER FK not null |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/pontos?ativo=&bairro= | — | `[PontoResponse]` | 200 |
| GET | /api/pontos/{id} | — | PontoResponse | 200, 404 |
| POST | /api/pontos | PontoRequest | PontoResponse | 201, 400, 404 |
| PUT | /api/pontos/{id} | PontoRequest (+ativo?) | PontoResponse | 200, 400, 404 |
| DELETE | /api/pontos/{id} | — | — (logical) | 204, 404 |

```json
// PontoRequest
{ "nome": "Ponto Paraíso", "tipo": "ASSOCIACAO", "diasFuncionamento": "TER e QUI",
  "horarioAbertura": "16:00", "horarioFechamento": "18:00", "telefone": null, "idEndereco": 3, "ativo": null }
// PontoResponse
{ "idPonto": 1, "nome": "Ponto Paraíso", "tipo": "ASSOCIACAO", "diasFuncionamento": "TER e QUI",
  "horarioAbertura": "16:00", "horarioFechamento": "18:00", "telefone": null, "ativo": true,
  "idEndereco": 3, "logradouro": "Rua Tutóia", "numero": "300", "complemento": null,
  "bairro": "Paraíso", "cidade": "São Paulo", "uf": "SP", "cep": "04007000" }
```
Horários travel as `"HH:MM"` strings in both directions; the BO parses them strictly.

## Layers
- `PontoDistribuicaoDAO.filtrar(ativo, bairro)`, `existsByEndereco_IdEndereco`.
- `PontoDistribuicaoBO` (CRUD, `find`), `EnderecoBO.checkSemUso` also checks pontos.
