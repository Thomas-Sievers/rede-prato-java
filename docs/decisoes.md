<!-- Source: Claude Project doc "Rede de Prato — Java decisões" (17/11/2026). These decisions are FINAL: do not reopen them. -->

# Rede de Prato — Decisões do projeto Java (DDD)

17/11/2026 · Thomas Sievers

Decisões tomadas antes de começar o plano de implementação. O projeto Java é feito inteiro pelo Claude Code (rodando sozinho à noite); o Thomas revisa de manhã. Não mudar o banco da Mariana Souza França.

## Repositório

- Um repositório só para Java (backend Spring Boot + frontend estático servido pelo mesmo app). Separado do resto da equipe por causa da regra "commit depois do prazo zera a parte técnica".

## Banco de dados

- O DDL da Mariana vai para `db/` como ela entregar e é a fonte da verdade; se divergir do doc "Classes e relacionamentos", as entidades seguem o DDL dela.
- Perfil Oracle com `spring.jpa.hibernate.ddl-auto=validate`; as entidades declaram os tipos exatos (`columnDefinition`, ex.: CHAR(1), VARCHAR2(5)). Erros de validação são corrigidos de manhã pelo Thomas.
- Testes e desenvolvimento local em H2 com `create-drop`; o run noturno não depende do Oracle.
- "Só uma reserva ATIVA por lote" garantida no BO; se o índice único existir no DDL, o ORA-00001 vira 409 na camada de exceções.
- Conversores Java: S/N (CHAR(1)) ↔ boolean; HH:MM (VARCHAR2(5)) ↔ LocalTime.

## Inconsistências resolvidas no código (aceitas pequenas diferenças com os docs)

1. Alerta sem status: o fluxo "falha no envio → pendente" do UC04 não é implementado (WhatsApp é simulado).
2. Expiração (HU21, Should) entra no MVP: verificação ao listar/reservar + job agendado.
3. Cancelar reserva (HU12, Should) entra na feature de reserva (PUT devolve o lote para DISPONIVEL).
4. Destinação: lote RETIRADO aceita CONSUMO ou COMPOSTAGEM; lote EXPIRADO só COMPOSTAGEM; outros status → 400; soma dos kg não passa de `peso_kg`.
5. Sem login: cada tela tem um seletor "Você é:" (ofertante ou beneficiário). O número de retirada é o `id_reserva`.
6. Cancelar lote (HU03) = DELETE, sem status CANCELADO. O BO apaga junto as reservas CANCELADAS e os alertas do lote; recusa com 409 se houver reserva ATIVA/RETIRADA ou destinação.
