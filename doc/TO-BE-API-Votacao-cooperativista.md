# TO-BE-API Votação Cooperativista

> Por | Status

## Nome da Aplicação
TO-BE-API Votação Cooperativista

---

## Arquitetura

### Objetivo

> A aplicação é uma API REST em Java/Spring Boot para gestão de pautas, sessões de votação e registro de votos de associados em uma cooperativa. O fluxo principal permite cadastrar pautas, abrir sessões de votação com duração configurável, receber votos dos associados, validar regras de negócio e consultar o resultado da votação. A API também expõe um conjunto de endpoints específicos para navegação mobile, com telas de formulário e seleção, seguindo um protocolo de interação baseado em ações e callbacks.

**Propósito da Aplicação:**
- Gerenciamento de pautas e sessões de votação em ambiente cooperativista.
- Registro e validação de votos com regras de integridade e unicidade.
- Consulta do resultado da votação de forma agregada por pauta.
- Exposição de endpoints REST para integração com frontends e fluxos mobile.
- Suporte à navegação por telas de formulário/seleção para experiência mobile.

**Tecnologia:**
- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Spring Actuator
- PostgreSQL 16
- Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ
- Testcontainers para integração com PostgreSQL
- Docker Compose
- Maven Wrapper

**Componentes Identificados:**
- Monólito modular em Spring Boot
- Controllers REST: 5
- Repositories JPA: 3
- Entidades de domínio: 3 principais
- Serviços de negócio: 5+
- DTOs e payloads específicos: 10+
- Fluxo mobile com telas e callbacks configuráveis
- Integração externa opcional com serviço de validação de usuário (UserInfo)

**Funcionalidades Identificadas:**
- Cadastro de pautas
- Abertura de sessão de votação por pauta
- Registro de voto com validação de regras de negócio
- Garantia de um voto por associado por pauta
- Consulta de resultado da votação
- Fluxo mobile com telas de formulário e seleção
- Validação de sessão ativa/inativa
- Persistência relacional com Flyway e PostgreSQL
- Logs estruturados por nível da aplicação
- Health checks via Spring Actuator
- Documentação de API via Swagger/OpenAPI

---

## Arquitetura

### Arquitetura Atual (TO-BE)

**Diagrama Draw.io (C1 - Contexto):** (veja c1-context.drawio)
**Diagrama Draw.io (C2 - Containers):** (veja c2-container.drawio)
**Diagrama Draw.io (C3 - Componentes):** (veja c3-component.drawio)
**Diagrama Draw.io (C4 - Deployment):** (veja c4-deployment.drawio)

---

### Desenho da infraestrutura

| Camada | Componente | Quantidade |
|--------|------------|------------|
| Aplicação | Spring Boot API | 1 |
| Persistência | PostgreSQL | 1 |
| Migração de schema | Flyway | 1 |
| API / Documentação | Swagger UI / OpenAPI | 1 |
| Observabilidade | Spring Actuator / health endpoints | 1 |
| Integração externa | UserInfo service (opcional) | 1 |
| Ambiente local | Docker Compose | 1 |

---

## 📋 Análise Detalhada dos Artefatos

### Endpoints REST

| Método | URL | Responsável/controller | Descrição |
|--------|-----|------------------------|-----------|
| POST | /api/v1/pautas | PautaController | Cria uma pauta |
| GET | /api/v1/pautas/{pautaId} | PautaController | Consulta uma pauta |
| POST | /api/v1/pautas/{pautaId}/sessao | SessaoController | Abre sessão de votação |
| POST | /api/v1/pautas/{pautaId}/votos | VotoController | Registra voto |
| GET | /api/v1/pautas/{pautaId}/resultado | ResultadoController | Consulta resultado da votação |
| POST | /api/v1/mobile/inicio | MobileController | Retorna fluxo inicial para mobile |
| POST | /api/v1/mobile/pautas/nova | MobileController | Cria pauta via formulário mobile |
| POST | /api/v1/mobile/pautas/{pautaId}/acoes | MobileController | Retorna ações do fluxo mobile |
| POST | /api/v1/mobile/pautas/{pautaId}/abrir-sessao/formulario | MobileController | Abre sessão via formulário mobile |
| POST | /api/v1/mobile/pautas/{pautaId}/votar/formulario | MobileController | Registra voto via formulário mobile |
| POST | /api/v1/mobile/pautas/{pautaId}/resultado | MobileController | Retorna resultado para fluxo mobile |

### Models / DTOs

| Nome | Campos Principais | Observação |
|------|-------------------|------------|
| Pauta | id, descricao, dataCriacao | Entidade de domínio |
| SessaoVotacao | id, pauta, inicio, fim | Sessão de votação |
| Voto | id, pauta, associadoId, tipo, dataHora | Voto do associado |
| PautaRequest / PautaResponse | descricao, id, dataCriacao | DTO de entrada/saída |
| VotoRequest | associadoId, tipo | Payload de voto |
| MobileScreen / FormularioScreen / SelecaoScreen | estrutura flexível de telas | Fluxo mobile com elementos e ações |
| CallbackProperties | base-url | Configuração de URL de callback |

### Repositories

| Nome | Entidade | Tipo | Responsabilidade |
|------|----------|------|------------------|
| PautaRepository | Pauta | JPA | Persistência de pautas |
| SessaoVotacaoRepository | SessaoVotacao | JPA | Persistência de sessões de votação |
| VotoRepository | Voto | JPA | Persistência e consultas de votos |

### Services

| Nome | Descrição |
|------|-----------|
| PautaService | Cadastro, consulta e regras de pauta |
| SessaoService | Abertura de sessão, validação de duração e período |
| VotoService | Registro de votos, regras de integridade e unicidade |
| ResultadoService | Cálculo do resultado da votação |
| MobileScreenService | Geração do fluxo de telas mobile e callbacks |
| UserInfoService | Validação externa opcional do associado |

### Integrações Externas

| Sistema | Tipo | Descrição / URL de integração |
|---------|------|-------------------------------|
| PostgreSQL | Banco relacional | Persistência principal da aplicação |
| Flyway | Migração de schema | Versionamento do banco |
| UserInfo service | REST (opcional) | Validação do associado via `GET /users/{cpf}` |
| Callback URL base | Configuração local | `app.callback.base-url` |

---

### Monitoramento & Observabilidade

- Health check via Spring Actuator
- Endpoints de monitoramento: `health`, `info`
- Logs de aplicação em nível `INFO` e `DEBUG`
- Documentação de API via Swagger/OpenAPI
- Observabilidade local com Docker Compose e Actuator

---

### Referências a Outros Sistemas

- UserInfo service - validação de associado / CPF
- PostgreSQL - armazenamento principal de pautas, sessões e votos
- Mobile front-end / telas de formulário e seleção - acesso via endpoints REST
- Callback base URL - integração de retorno para aplicações externas

---

### Links para Projetos

- Modernização Java/Spring Boot
- Observabilidade/Actuator
- Infra local com Docker Compose
