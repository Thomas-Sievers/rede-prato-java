# Rede de Prato — Diagrama de classes (código Java)

Gerado a partir das classes em `src/main/java/br/com/fiap/redeprato`. Os atributos e tipos são exatamente os do código. Cada chave estrangeira é um atributo do tipo da outra classe (não um `Long` solto), então o diagrama corresponde ao DER (`docs/der.md`).

## 1. Modelo (pacote `entity` e `entity.enums`)

```mermaid
classDiagram
    direction LR

    class Endereco {
        -Long idEndereco
        -String logradouro
        -String numero
        -String complemento
        -String bairro
        -String cidade
        -String uf
        -String cep
    }

    class Ofertante {
        -Long idOfertante
        -String nome
        -TipoOfertante tipo
        -String telefone
        -String email
        -boolean possuiSelo
        -LocalDate dataSelo
        -boolean ativo
        -Endereco endereco
    }

    class CategoriaAlimento {
        -Long idCategoria
        -String nome
        -String descricao
    }

    class Lote {
        -Long idLote
        -Ofertante ofertante
        -CategoriaAlimento categoria
        -String descricao
        -BigDecimal pesoKg
        -CondicaoLote condicao
        -ModalidadeLote modalidade
        -BigDecimal preco
        -BigDecimal valorEstimado
        -LocalDateTime dataCadastro
        -LocalDateTime dataLimiteRetirada
        -String urlFoto
        -boolean precisaTransporte
        -StatusLote status
    }

    class Beneficiario {
        -Long idBeneficiario
        -String nome
        -TipoBeneficiario tipo
        -Integer qtdPessoas
        -String telefone
        -boolean ativo
        -Endereco endereco
    }

    class PontoDistribuicao {
        -Long idPonto
        -String nome
        -TipoPonto tipo
        -String diasFuncionamento
        -LocalTime horarioAbertura
        -LocalTime horarioFechamento
        -String telefone
        -boolean ativo
        -Endereco endereco
    }

    class Reserva {
        -Long idReserva
        -Lote lote
        -Beneficiario beneficiario
        -PontoDistribuicao ponto
        -LocalDateTime dataReserva
        -LocalDateTime dataRetirada
        -StatusReserva status
    }

    class Destinacao {
        -Long idDestinacao
        -Lote lote
        -TipoDestinacao tipo
        -BigDecimal pesoKg
        -LocalDateTime dataDestinacao
    }

    class Alerta {
        -Long idAlerta
        -Lote lote
        -TipoAlerta tipo
        -CanalAlerta canal
        -LocalDateTime dataEnvio
    }

    class TipoOfertante {
        <<enumeration>>
        SUPERMERCADO
        FEIRANTE
    }
    class CondicaoLote {
        <<enumeration>>
        BOA
        MADURA
        CONSUMO_IMEDIATO
    }
    class ModalidadeLote {
        <<enumeration>>
        DOACAO
        VENDA
    }
    class StatusLote {
        <<enumeration>>
        DISPONIVEL
        RESERVADO
        RETIRADO
        EXPIRADO
    }
    class TipoBeneficiario {
        <<enumeration>>
        PESSOA
        FAMILIA
        INSTITUICAO
        PEQUENO_NEGOCIO
    }
    class TipoPonto {
        <<enumeration>>
        COZINHA_SOLIDARIA
        ONG
        ASSOCIACAO
    }
    class StatusReserva {
        <<enumeration>>
        ATIVA
        RETIRADA
        CANCELADA
    }
    class TipoDestinacao {
        <<enumeration>>
        CONSUMO
        COMPOSTAGEM
    }
    class TipoAlerta {
        <<enumeration>>
        NOVO_LOTE
        XEPA
    }
    class CanalAlerta {
        <<enumeration>>
        APP
        WHATSAPP
    }

    Endereco "1" <-- "0..*" Ofertante : endereco
    Endereco "0..1" <-- "0..*" Beneficiario : endereco
    Endereco "1" <-- "0..*" PontoDistribuicao : endereco
    Ofertante "1" <-- "0..*" Lote : ofertante
    CategoriaAlimento "1" <-- "0..*" Lote : categoria
    Lote "1" <-- "0..*" Reserva : lote
    Beneficiario "1" <-- "0..*" Reserva : beneficiario
    PontoDistribuicao "0..1" <-- "0..*" Reserva : ponto
    Lote "1" <-- "0..*" Destinacao : lote
    Lote "1" <-- "0..*" Alerta : lote

    Ofertante ..> TipoOfertante
    Lote ..> CondicaoLote
    Lote ..> ModalidadeLote
    Lote ..> StatusLote
    Beneficiario ..> TipoBeneficiario
    PontoDistribuicao ..> TipoPonto
    Reserva ..> StatusReserva
    Destinacao ..> TipoDestinacao
    Alerta ..> TipoAlerta
    Alerta ..> CanalAlerta
```

Observações de mapeamento:
- `boolean` ↔ `CHAR(1)` 'S'/'N' via `converter.BooleanSNConverter`; `LocalTime` ↔ `VARCHAR2(5)` "HH:MM" via `converter.LocalTimeHHMMConverter`.
- Todos os relacionamentos são `@ManyToOne(fetch = LAZY)` (navegação do filho para o pai); listas de filhos são consultadas pelos DAOs.
- Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` em todas as entidades.

## 2. Camadas (Controller → BO → DAO → Entity) e exceções

```mermaid
classDiagram
    direction TB

    class LoteController {
        +listar(status, idOfertante, idCategoria, modalidade) List~LoteDTO.Response~
        +disponiveis(idCategoria, modalidade, bairro) List~LoteDTO.Response~
        +buscar(id) LoteDTO.Response
        +criar(LoteDTO.Request) ResponseEntity
        +atualizar(id, LoteDTO.Request) LoteDTO.Response
        +excluir(id) ResponseEntity
        +expirar() LoteDTO.ExpiracaoResponse
        +registrarDestinos(id, DestinacaoDTO.LoteRequest) ResponseEntity
    }
    class ReservaController {
        +listar(...) List~ReservaDTO.Response~
        +buscar(id) ReservaDTO.Response
        +criar(ReservaDTO.Request) ResponseEntity
        +atualizar(id, ReservaDTO.UpdateRequest) ReservaDTO.Response
        +cancelar(id) ReservaDTO.Response
        +confirmarRetirada(id) ReservaDTO.Response
        +excluir(id) ResponseEntity
    }
    class LoteBO {
        +listar(...) List
        +listarDisponiveis(...) List
        +criar(LoteDTO.Request) LoteDTO.Response
        +atualizar(id, LoteDTO.Request) LoteDTO.Response
        +excluir(id)
        +expirarVencidos() int
        +find(id) Lote
        ~validate(Lote)
    }
    class ReservaBO {
        +criar(ReservaDTO.Request) ReservaDTO.Response
        +cancelar(id) ReservaDTO.Response
        +confirmarRetirada(id) ReservaDTO.Response
        +atualizar(id, UpdateRequest) ReservaDTO.Response
        +excluir(id)
        +find(id) Reserva
    }
    class LoteDAO {
        <<interface>>
        +filtrar(...) List~Lote~
        +disponiveis(agora, ...) List~Lote~
        +findByStatusInAndDataLimiteRetiradaBefore(...) List~Lote~
        +existsByCategoria_IdCategoria(id) boolean
    }
    class ReservaDAO {
        <<interface>>
        +filtrar(...) List~Reserva~
        +existsByLote_IdLoteAndStatus(id, status) boolean
        +contarLotesRetirados(...) long
        +beneficiariosAtendidos(...) List~Beneficiario~
    }
    class JpaRepository~T, Long~ {
        <<interface>>
    }
    class Lote
    class Reserva

    LoteController --> LoteBO
    LoteController --> DestinacaoBO
    ReservaController --> ReservaBO
    ReservaBO --> LoteBO
    ReservaBO --> ReservaDAO
    ReservaBO --> LoteDAO
    ReservaBO --> BeneficiarioBO
    ReservaBO --> PontoDistribuicaoBO
    LoteBO --> LoteDAO
    LoteBO --> ReservaDAO
    LoteBO --> DestinacaoDAO
    LoteBO --> AlertaDAO
    LoteBO --> OfertanteBO
    LoteBO --> CategoriaAlimentoBO
    LoteDAO --|> JpaRepository
    ReservaDAO --|> JpaRepository
    LoteDAO ..> Lote
    ReservaDAO ..> Reserva

    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        +notFound(NotFoundException) 404
        +business(BusinessRuleException) 400
        +conflict(ConflictException) 409
        +integrity(DataIntegrityViolationException) 409/400
        +unexpected(Exception) 500
    }
    class ApiError {
        <<record>>
        OffsetDateTime timestamp
        int status
        String error
        String message
        String path
        Map fields
    }
    class NotFoundException
    class BusinessRuleException
    class ConflictException
    class RuntimeException
    NotFoundException --|> RuntimeException
    BusinessRuleException --|> RuntimeException
    ConflictException --|> RuntimeException
    GlobalExceptionHandler ..> ApiError
    LoteBO ..> BusinessRuleException : lança
    LoteBO ..> ConflictException : lança
    LoteBO ..> NotFoundException : lança
```

O mesmo padrão vale para os outros recursos:

| Controller (`/api/...`) | BO | DAO | Entidade |
| --- | --- | --- | --- |
| `CategoriaController` (`/categorias`) | `CategoriaAlimentoBO` | `CategoriaAlimentoDAO` | `CategoriaAlimento` |
| `EnderecoController` (`/enderecos`) | `EnderecoBO` | `EnderecoDAO` | `Endereco` |
| `OfertanteController` (`/ofertantes`) | `OfertanteBO` | `OfertanteDAO` | `Ofertante` |
| `LoteController` (`/lotes`) | `LoteBO`, `DestinacaoBO` | `LoteDAO` | `Lote` |
| `BeneficiarioController` (`/beneficiarios`) | `BeneficiarioBO` | `BeneficiarioDAO` | `Beneficiario` |
| `PontoController` (`/pontos`) | `PontoDistribuicaoBO` | `PontoDistribuicaoDAO` | `PontoDistribuicao` |
| `ReservaController` (`/reservas`) | `ReservaBO` | `ReservaDAO` | `Reserva` |
| `DestinacaoController` (`/destinacoes`) | `DestinacaoBO` | `DestinacaoDAO` | `Destinacao` |
| `AlertaController` (`/alertas`) | `AlertaBO` | `AlertaDAO` | `Alerta` |
| `ImpactoController` (`/impacto`) | `ImpactoBO` | `DestinacaoDAO`, `ReservaDAO` | (consultas agregadas) |
| `RelatorioController` (`/relatorios`) | `RelatorioBO` | `DestinacaoDAO` | (relatório mensal) |
| `HealthController` (`/health`) | — | — | — |

Apoio: `dto` (records `XxxDTO.Request` / `XxxDTO.Response`), `converter` (S/N e HH:MM), `config` (`ClockConfig`, `WebConfig`, `ExpiracaoJob`, `DataSeeder`).
