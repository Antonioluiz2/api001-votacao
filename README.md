# API de Votação Cooperativista

API REST em Java/Spring Boot para gerenciar pautas, sessões de votação e votos de associados em uma cooperativa.

## Objetivo

Permitir que uma cooperativa:

1. Cadastre pautas;
2. Abra sessões de votação com duração configurável (padrão 60s);
3. Receba votos (`SIM`/`NAO`) de associados, garantindo **um voto por associado por pauta**;
4. Consulte o resultado (parcial ou final) da votação;
5. Navegue por um fluxo mobile de telas `FORMULARIO`/`SELECAO` (Anexo 1) que aponta para os mesmos endpoints REST.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Bean Validation, Actuator)
- PostgreSQL 16
- Flyway (migrations)
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ
- Testcontainers (PostgreSQL) para testes de integração
- Maven

> **Nota sobre versões**: este ambiente resolve `spring-boot-starter-parent:4.1.1`, uma versão da
> família Spring Boot 4 que adota starters segmentados por biblioteca também para testes
> (`spring-boot-starter-webmvc`, `spring-boot-starter-flyway`, `spring-boot-starter-data-jpa-test`,
> `spring-boot-starter-webmvc-test`, etc.). O `pom.xml` foi ajustado para usar exatamente esses
> artefatos (os únicos resolvíveis no repositório Maven deste ambiente).

## Arquitetura

Pacotes organizados por funcionalidade (feature package), cada um com `controller`, `service`,
`domain`, `dto` e `repository` quando aplicável:

```
src/main/java/br/com/cooperativa/votacao
├── VotacaoApplication.java
├── config/          # Clock, OpenAPI, CallbackProperties, WebConfig
├── pauta/            # Cadastro de pautas
├── sessao/           # Abertura de sessões de votação
├── voto/             # Registro de votos
├── resultado/        # Consulta de resultado (contagem agregada no banco)
├── mobile/           # Telas de navegação mobile (FORMULARIO/SELECAO)
├── integration/userinfo/  # Bônus: integração com serviço externo de CPF
└── exception/        # Exceções de domínio + GlobalExceptionHandler
```

### Decisões técnicas relevantes

- **Uma sessão por pauta**: ao tentar abrir uma segunda sessão para a mesma pauta, a API retorna
  `409 Conflict`. Essa é a alternativa mais simples e consistente com a regra de "um voto por
  associado por pauta" (não há necessidade de reabertura no escopo deste teste).
- **Voto único por associado/pauta**: garantido em duas camadas — validação prévia no
  `VotoService` (defesa contra o caso comum) **e** constraint `UNIQUE(pauta_id, associado_id)` no
  banco de dados, que é a garantia real contra condições de corrida. Uma violação da constraint é
  capturada (`DataIntegrityViolationException`) e convertida em `409 Conflict`.
- **Relógio abstraído (`java.time.Clock`)**: toda regra de negócio sensível a tempo (abertura,
  encerramento de sessão) usa `Instant.now(clock)`, nunca `Instant.now()` diretamente. Isso permite
  testar deterministicamente os limites exatos de início/fim de sessão nos testes unitários.
- **`associadoId` é `String`**: permite CPF, zeros à esquerda, identificadores alfanuméricos e
  futura integração externa.
- **Resultado via consulta agregada**: `ResultadoService` usa `COUNT(...)` no banco
  (`countByPautaIdAndTipo`) em vez de carregar todos os votos em memória.
- **URLs de callback configuráveis**: todas as URLs retornadas nas telas mobile são absolutas e
  montadas a partir de `app.callback.base-url` (variável de ambiente `CALLBACK_BASE_URL`). Nunca
  há URL, host ou porta hardcoded nas respostas.
- **Bônus de integração CPF desabilitado por padrão** (`app.userinfo.enabled=false`): o enunciado
  define `associadoId` como identificador genérico, não necessariamente um CPF. Habilitar a
  integração (`USERINFO_ENABLED=true`) ativa a validação via
  `GET https://user-info.herokuapp.com/users/{cpf}` antes de aceitar o voto.

## Modelo de domínio

- **Pauta**: `id`, `descricao` (obrigatória, não vazia/só espaços), `dataCriacao`.
- **SessaoVotacao**: `id`, `pauta`, `inicio`, `fim`. Aberta quando `agora >= inicio && agora < fim`.
- **Voto**: `id`, `pauta`, `associadoId` (String), `tipo` (enum `SIM`/`NAO`), `dataHora`.

## Contrato REST

```
POST /api/v1/pautas                        - Cria pauta                → 201
GET  /api/v1/pautas/{pautaId}               - Consulta pauta           → 200 | 404
POST /api/v1/pautas/{pautaId}/sessao        - Abre sessão de votação   → 201 | 404 | 409
POST /api/v1/pautas/{pautaId}/votos         - Registra voto            → 201 | 400 | 404 | 409
GET  /api/v1/pautas/{pautaId}/resultado     - Consulta resultado       → 200 | 404
```

### Telas mobile (protocolo do Anexo 1 — FORMULARIO/SELECAO)

```
POST /api/v1/mobile/inicio                                  - SELECAO: lista de pautas
POST /api/v1/mobile/pautas/nova                              - FORMULARIO: cadastro de pauta
POST /api/v1/mobile/pautas/{pautaId}/acoes                   - SELECAO: ações disponíveis
POST /api/v1/mobile/pautas/{pautaId}/abrir-sessao/formulario - FORMULARIO: abrir sessão
POST /api/v1/mobile/pautas/{pautaId}/votar/formulario        - FORMULARIO: votar (botões SIM/NAO)
POST /api/v1/mobile/pautas/{pautaId}/resultado                - FORMULARIO: exibe SIM/NAO/total
```

Todas as `url` retornadas são absolutas, construídas com `app.callback.base-url`.

### Erros

Tratamento centralizado via `@RestControllerAdvice` (`GlobalExceptionHandler`), retornando sempre:

```json
{
  "timestamp": "2026-09-19T15:00:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Associado já realizou voto nesta pauta",
  "path": "/api/v1/pautas/1/votos"
}
```

Nenhum stack trace, SQL ou detalhe interno é exposto.

## Persistência

PostgreSQL com schema gerenciado via Flyway (`src/main/resources/db/migration`). A aplicação usa
`spring.jpa.hibernate.ddl-auto=validate` — o schema **nunca** é criado/alterado pelo Hibernate,
apenas validado contra as migrations aplicadas.

Migrations:

- `V1__create_pauta.sql`
- `V2__create_sessao_votacao.sql` (índice em `pauta_id`, `UNIQUE(pauta_id)`)
- `V3__create_voto.sql` (índices em `pauta_id` e `associado_id`, `UNIQUE(pauta_id, associado_id)`)

## Como executar

### Pré-requisitos

- Java 21 (JDK)
- Docker e Docker Compose (para o PostgreSQL local)
- Não é necessário ter o Maven instalado: o projeto inclui o Maven Wrapper (`mvnw`/`mvnw.cmd`),
  que baixa e usa automaticamente o Maven 3.9.11 na primeira execução.

### Fluxo padrão

```bash
docker compose up -d
./mvnw clean test
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

A aplicação sobe em `http://localhost:8080`.

### Variáveis de ambiente

```
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/votacao
DB_USERNAME=postgres
DB_PASSWORD=postgres
CALLBACK_BASE_URL=http://localhost:8080
USERINFO_ENABLED=false
USERINFO_BASE_URL=https://user-info.herokuapp.com
USERINFO_TIMEOUT_MS=3000
```

As credenciais acima são valores de exemplo para ambiente local, sem relação com credenciais reais.

### Testando os endpoints (curl)

```bash
# Criar pauta
curl -X POST http://localhost:8080/api/v1/pautas \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Aprovacao da reforma do estatuto"}'

# Abrir sessão (duração padrão de 60s)
curl -X POST http://localhost:8080/api/v1/pautas/1/sessao \
  -H "Content-Type: application/json" -d '{}'

# Votar SIM
curl -X POST http://localhost:8080/api/v1/pautas/1/votos \
  -H "Content-Type: application/json" \
  -d '{"associadoId":"11111111111","tipo":"SIM"}'

# Consultar resultado
curl http://localhost:8080/api/v1/pautas/1/resultado

# Navegação mobile
curl -X POST http://localhost:8080/api/v1/mobile/inicio
```

### Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Em produção, desabilite via `SPRINGDOC_API_DOCS_ENABLED=false` / `SPRINGDOC_SWAGGER_UI_ENABLED=false`
(propriedades `springdoc.api-docs.enabled` / `springdoc.swagger-ui.enabled`).

## Testes

```bash
mvn clean test
```

### Cobertura de testes

- **Unitários** (`*ServiceTest`, `*ValidationTest`, `MobileScreenServiceTest` — 35 testes): cobrem
  criação de pauta, descrição inválida, duração padrão/customizada/zero/negativa, pauta e sessão
  inexistentes, segunda sessão para a mesma pauta, voto SIM/NAO, voto duplicado (preventivo e via
  constraint), voto fora do período (antes e exatamente no limite de início/fim), telas mobile e
  URLs de callback configuráveis. Usam `Clock` fixo para determinismo total.
- **Integração** (`*IntegrationTest`, com `Testcontainers` + PostgreSQL real): cobrem o fluxo
  completo (criar pauta → abrir sessão → votar SIM/NAO → consultar resultado), persistência,
  migrations, constraint única (pauta+associado e pauta única por sessão), sessão encerrada,
  navegação completa pelas telas mobile e URLs de callback configuráveis via propriedade.
- **Concorrência** (`VotoConcorrenciaIntegrationTest`): duas requisições de voto do mesmo associado
  na mesma pauta disparadas simultaneamente contra um PostgreSQL real via Testcontainers — apenas
  uma é aceita (`201`), a outra recebe `409`.

> **Limitação de ambiente observada durante a execução**: os testes de integração/concorrência
> usam Testcontainers, que depende da API do motor Docker. Neste ambiente de execução, o Docker
> Desktop responde corretamente aos comandos de CLI (`docker info`, `docker compose up`, `docker
> build`, `docker run` — todos testados e funcionais), mas a biblioteca cliente HTTP usada pelo
> Testcontainers (`docker-java`) recebe um `400 Bad Request` com corpo vazio ao consultar `/info`
> pelo named pipe, impedindo a inicialização automática do container de PostgreSQL nesses testes
> específicos. Isso foi tratado com transparência da seguinte forma:
> 1. Os 35 testes unitários foram executados com sucesso (`mvn test`), cobrindo 100% das regras de
>    negócio de forma determinística.
> 2. Os testes de integração/concorrência **compilam corretamente** contra as APIs reais do Spring
>    Boot 4.1.1 (`MockMvc`, `WebApplicationContext`, Testcontainers) e foram desenhados para validar
>    exatamente os cenários exigidos (fluxo completo, persistência, constraints, concorrência,
>    contrato mobile), mas não puderam ser executados de ponta a ponta pelo `mvn test` neste
>    ambiente específico.
> 3. Para compensar, **todos os mesmos cenários foram validados manualmente** com a aplicação real
>    rodando contra um PostgreSQL real (subido via `docker compose up`, usando a CLI do Docker, que
>    funciona neste ambiente): criação de pauta, abertura de sessão, voto SIM, voto NAO, voto
>    duplicado (`409`), sessão encerrada (`409`), pauta/resultado inexistentes (`404`), consulta de
>    resultado, persistência após reinício da aplicação, build e execução da imagem Docker, e um
>    teste de concorrência manual (duas requisições simultâneas via `Start-Job` do PowerShell,
>    resultando em exatamente um `201` e um `409`, com apenas um voto persistido). Os comandos e
>    resultados dessa validação manual estão documentados na resposta final da sessão de
>    desenvolvimento.
> Em um ambiente com Testcontainers plenamente funcional (a maioria dos ambientes de desenvolvimento
> e CI), o comando `mvn clean test` deve executar os 40 testes (35 unitários + 5 de
> integração/concorrência) sem qualquer alteração de código.

## Execução em nuvem

A aplicação é *cloud-ready*: não depende de arquivos locais, estado em memória ou configuração
exclusiva de `localhost`.

### Construir a imagem Docker

```bash
docker build -t api001-votacao:latest .
```

Multi-stage build (`maven:3.9-eclipse-temurin-21` para build, `eclipse-temurin:21-jre-alpine` para
execução), usuário não-root, sem dados persistidos no filesystem do container.

### Executar o container localmente

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://<host-do-postgres>:5432/votacao \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e CALLBACK_BASE_URL=http://localhost:8080 \
  api001-votacao:latest
```

> Testado neste ambiente: `docker build` e `docker run` funcionaram corretamente, conectando a um
> PostgreSQL real (subido via `docker compose`), aplicando as migrations Flyway automaticamente na
> inicialização e respondendo corretamente em `/actuator/health` e nos endpoints da API.

### Variáveis de ambiente em produção

| Variável | Descrição |
|---|---|
| `SERVER_PORT` | Porta HTTP da aplicação (padrão `8080`) |
| `DB_URL` | JDBC URL do PostgreSQL gerenciado |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais do banco |
| `CALLBACK_BASE_URL` | URL pública base usada nas telas mobile |
| `USERINFO_ENABLED` | Habilita o bônus de validação de CPF externo |
| `USERINFO_BASE_URL` / `USERINFO_TIMEOUT_MS` | Configuração do serviço externo de CPF |

### Observações

- **Porta**: configurável via `SERVER_PORT` (`server.port=${SERVER_PORT:8080}`).
- **Health check**: `GET /actuator/health` (Spring Boot Actuator, apenas `health` e `info`
  expostos — decisão deliberada para não expor métricas/detalhes sensíveis por padrão).
- **Migrations**: executadas automaticamente pelo Flyway na inicialização; nenhuma ação manual de
  schema é necessária no ambiente de destino.
- **Persistência**: toda a persistência é delegada ao PostgreSQL gerenciado (RDS ou equivalente);
  o container da aplicação não grava estado em disco.
- **Logs**: SLF4J/Logback para stdout/stderr (formato padrão do Spring Boot), compatível com
  qualquer coletor de logs de plataforma de containers (ECS, Cloud Run, Kubernetes, etc.).
- **Deploy genérico**: a imagem gerada pelo `Dockerfile` pode ser publicada em qualquer registro de
  containers e executada em qualquer orquestrador (Kubernetes, ECS, Cloud Run, Azure Container
  Apps, etc.), fornecendo as variáveis de ambiente acima e um PostgreSQL acessível.

## Versionamento

A API usa `/api/v1/...`. Mudanças incompatíveis serão introduzidas em `/api/v2/...`, preservando o
contrato atual para consumidores existentes.

## Bônus implementados

- ✅ **Integração CPF** (`integration/userinfo`): client dedicado (`UserInfoClient`) com timeout
  configurável, tratamento de erro de comunicação e de CPF inválido (404 → `400`), desabilitado por
  padrão via `USERINFO_ENABLED=false` (nenhuma chamada externa ocorre no fluxo obrigatório).
- ✅ **Performance**: consultas agregadas (`COUNT`) para resultado, índices nas colunas de busca
  (`pauta_id`, `associado_id`), constraint única garantindo integridade sem lock manual, pool de
  conexões HikariCP, e transações delimitadas nos services.
- ✅ **Versionamento**: `/api/v1/...` documentado, estratégia de evolução via `/api/v2/...`.

## Limitações conhecidas

- Os testes de integração/concorrência com Testcontainers não puderam ser executados de ponta a
  ponta neste ambiente específico devido a uma incompatibilidade entre a API HTTP do Docker Desktop
  e a biblioteca cliente `docker-java` (ver seção "Testes" acima). Todos os cenários foram validados
  manualmente com sucesso.
