# 05 — Beneficiários · Design

## Entity `Beneficiario` → `T_RP_BENEFICIARIO`

| Field | Column | Type |
| --- | --- | --- |
| idBeneficiario | id_beneficiario | NUMBER identity PK |
| nome | nome | VARCHAR2(100) not null |
| tipo | tipo | VARCHAR2(20) not null, `TipoBeneficiario` |
| qtdPessoas | qtd_pessoas | NUMBER(4), Integer |
| telefone | telefone | VARCHAR2(15) |
| ativo | ativo | CHAR(1) not null S/N |
| endereco | id_endereco | NUMBER FK, optional (`@ManyToOne LAZY`) |

## Endpoints

| Method | Path | Request | Response | Status |
| --- | --- | --- | --- | --- |
| GET | /api/beneficiarios?tipo=&ativo= | — | `[BeneficiarioResponse]` | 200 |
| GET | /api/beneficiarios/{id} | — | BeneficiarioResponse | 200, 404 |
| POST | /api/beneficiarios | BeneficiarioRequest | BeneficiarioResponse | 201, 400, 404 |
| PUT | /api/beneficiarios/{id} | BeneficiarioRequest (+ativo?) | BeneficiarioResponse | 200, 400, 404 |
| DELETE | /api/beneficiarios/{id} | — | — (logical) | 204, 404 |

```json
// BeneficiarioRequest
{ "nome": "Rosana", "tipo": "FAMILIA", "qtdPessoas": 4, "telefone": "11900000011", "idEndereco": null, "ativo": null }
// BeneficiarioResponse
{ "idBeneficiario": 1, "nome": "Rosana", "tipo": "FAMILIA", "qtdPessoas": 4, "telefone": "11900000011",
  "ativo": true, "idEndereco": null, "logradouro": null, "numero": null, "bairro": null }
```

## Layers
- `BeneficiarioDAO.filtrar(tipo, ativo)` (left join fetch endereço, order by nome); `existsByEndereco_IdEndereco`.
- `BeneficiarioBO` (CRUD, `find`), `EnderecoBO.checkSemUso` also checks beneficiários.
