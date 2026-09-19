# Implementar API REST de votação cooperativista em Java Spring Boot

## Objetivo

Implemente uma API REST em Java com Spring Boot para gerenciar pautas, sessões de votação e votos de associados em uma cooperativa.

A solução será utilizada em uma avaliação técnica para desenvolvedor Java sênior. Priorize:

1. Requisitos obrigatórios;
2. Correção das regras de negócio;
3. Persistência durável;
4. Concorrência;
5. Testes automatizados;
6. Documentação;
7. Simplicidade e ausência de over-engineering;
8. Tarefas bônus somente depois do fluxo principal estar estável, testado e documentado.

Não implemente funcionalidades fora do escopo.

---

## 1. Procedimento obrigatório antes da implementação

Antes de modificar qualquer arquivo:

1. Inspecione a estrutura atual do repositório.
2. Identifique:
   - linguagem e versão existentes;
   - build tool;
   - estrutura de pacotes;
   - banco já configurado;
   - testes existentes;
   - arquivos de configuração;
   - documentação existente.
3. Preserve funcionalidades existentes quando não conflitarem com este requisito.
4. Não crie um novo repositório.
5. Não substitua tecnologias existentes sem necessidade.
6. Se o repositório estiver vazio, crie a estrutura completa do projeto.
7. Se houver dúvidas que impeçam a implementação, informe-as antes de alterar o código.
8. Caso uma decisão não esteja especificada, escolha a alternativa mais simples e documente-a no README.

Depois da implementação:

1. Execute os testes.
2. Corrija falhas de compilação, testes e integração.
3. Revise as regras de negócio.
4. Verifique a persistência após reinício.
5. Atualize o README.
6. Informe claramente o que foi implementado, quais bônus foram concluídos e quais comandos foram executados.
7. Não declare a tarefa concluída apenas porque os arquivos foram criados. A solução só deve ser considerada pronta após execução válida dos testes e da aplicação.

Além disso, é obrigatório:

- executar `./mvnw clean test`;
- subir o PostgreSQL com Docker Compose, se houver Docker no ambiente;
- iniciar a aplicação com `./mvnw spring-boot:run`;
- testar os endpoints principais do fluxo de negócio;
- validar que as migrations funcionam;
- verificar que os dados persistem após reinício da aplicação;
- corrigir eventuais falhas encontradas antes de finalizar;
- se alguma dependência necessária não puder ser executada, informar exatamente o que faltou e não afirmar que os testes passaram ou que a aplicação está funcionando.

---

## 2. Requisitos obrigatórios

A API deve permitir:

1. Cadastrar uma nova pauta.
2. Abrir uma sessão de votação associada a uma pauta.
3. Definir a duração da sessão:
   - duração enviada na requisição; ou
   - 60 segundos por padrão.
4. Receber votos de associados.
5. Aceitar somente os votos:
   - `SIM`;
   - `NAO`.
6. Identificar cada associado por um ID único.
7. Permitir somente um voto por associado em cada pauta.
8. Contabilizar os votos.
9. Consultar o resultado da votação.
10. Persistir pautas, sessões e votos de forma durável.

A aplicação não precisa implementar autenticação ou autorização. Considere qualquer chamada autorizada.

---

## 3. Tecnologias

Utilize, preferencialmente:

- Java 17 ou superior;
- Spring Boot;
- Spring Web;
- Spring Data JPA;
- Bean Validation;
- PostgreSQL;
- Flyway;
- OpenAPI/Swagger;
- JUnit 5;
- Mockito quando necessário;
- Testcontainers para testes de integração, se aplicável;
- Maven.

Não adicione bibliotecas apenas para aumentar a quantidade de tecnologias.

Evite:

- microsserviços;
- Kafka;
- RabbitMQ;
- Redis;
- Kubernetes;
- API Gateway;
- CQRS;
- Event Sourcing;
- múltiplos módulos Maven;
- autenticação;
- infraestrutura cloud complexa.

Uma aplicação Spring Boot modular, persistente e bem testada é suficiente.

---

## 4. Modelo de domínio

Implemente pelo menos as entidades:

### 4.1 Pauta

Campos sugeridos:

- `id`;
- `descricao`;
- `dataCriacao`.

A descrição é obrigatória e não pode ser vazia ou composta apenas por espaços.

### 4.2 SessaoVotacao

Campos sugeridos:

- `id`;
- `pauta`;
- `inicio`;
- `fim`.

A sessão estará aberta quando:

```text
agora >= inicio && agora < fim
```

Não é necessário usar um scheduler ou job para encerrar a sessão. O encerramento pode ser calculado pela comparação entre o horário atual e `fim`.

A sessão deve começar imediatamente após a abertura.

### 4.3 Voto

Campos sugeridos:

- `id`;
- `pauta`;
- `associadoId`;
- `tipo`;
- `dataHora`.

`associadoId` deve ser `String`, não `Long`, para permitir:

- identificadores com zeros à esquerda;
- CPF;
- identificadores alfanuméricos;
- futura integração externa.

O tipo do voto deve ser um enum:

```java
public enum TipoVoto {
    SIM,
    NAO
}
```

---

## 5. Regras de negócio

### 5.1 Criar pauta

Endpoint para criar uma pauta.

Regras:

- descrição obrigatória;
- descrição não pode ser vazia;
- persistir a pauta no banco;
- retornar `201 Created`.

Exemplo de requisição:

```json
{
  "descricao": "Aprovação da reforma do estatuto"
}
```

### 5.2 Abrir sessão

A sessão deve estar associada a uma pauta existente.

Requisição:

```json
{
  "duracaoEmSegundos": 60
}
```

Regras:

- se `duracaoEmSegundos` não for informado, usar 60;
- duração zero ou negativa deve ser rejeitada;
- pauta inexistente deve retornar `404`;
- cada pauta pode possuir somente uma sessão;
- se já existir sessão para a pauta, retornar `409 Conflict`;
- registrar `inicio` na aplicação;
- calcular `fim` com base na duração;
- retornar `201 Created`.

A regra de uma sessão por pauta foi escolhida para manter coerência com o requisito de um voto por associado por pauta.

### 5.3 Registrar voto

Endpoint para registrar voto de um associado em uma pauta.

Exemplo:

```json
{
  "associadoId": "12345678909",
  "tipo": "SIM"
}
```

Regras:

- pauta inexistente → `404`;
- sessão inexistente → `404`;
- sessão encerrada → `409`;
- sessão ainda não iniciada → `409`;
- voto diferente de `SIM` ou `NAO` → `400`;
- `associadoId` ausente ou vazio → `400`;
- associado só pode votar uma vez na pauta;
- voto deve ser persistido;
- voto duplicado → `409`.

A unicidade deve ser garantida:

1. na camada de serviço;
2. no banco de dados por constraint única:

```text
UNIQUE (pauta_id, associado_id)
```

Não dependa somente de:

```java
if (!repository.exists(...)) {
    repository.save(...);
}
```

Essa verificação isolada não é suficiente em condições de concorrência.

Trate adequadamente a violação da constraint única, convertendo-a em erro de domínio com resposta `409 Conflict`.

### 5.4 Consultar resultado

Endpoint:

```text
GET /api/v1/pautas/{pautaId}/resultado
```

Exemplo de resposta:

```json
{
  "pautaId": 1,
  "sim": 10,
  "nao": 5,
  "total": 15
}
```

O resultado pode ser consultado durante ou depois da sessão. Durante a sessão, o resultado representa uma apuração parcial.

A contagem deve ser obtida do banco, preferencialmente com consulta agregada, sem carregar todos os votos em memória.

---

## 6. Contrato REST

Implemente, preferencialmente:

```text
POST /api/v1/pautas
POST /api/v1/pautas/{pautaId}/sessao
POST /api/v1/pautas/{pautaId}/votos
GET  /api/v1/pautas/{pautaId}/resultado
```

É permitido adicionar endpoints auxiliares somente se forem realmente necessários.

### Status HTTP esperados

| Situação | Status |
|---|---:|
| Criação de pauta | 201 |
| Criação de sessão | 201 |
| Registro de voto | 201 |
| Consulta de resultado | 200 |
| Payload inválido | 400 |
| Recurso inexistente | 404 |
| Sessão encerrada | 409 |
| Voto duplicado | 409 |
| Sessão já existente | 409 |
| Erro inesperado | 500 |

Não retorne `200 OK` para todos os cenários.

---

## 7. Tratamento de erros

Implemente tratamento global usando `@RestControllerAdvice`.

Use uma resposta consistente, por exemplo:

```json
{
  "timestamp": "2026-09-19T15:00:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Associado já realizou voto nesta pauta",
  "path": "/api/v1/pautas/1/votos"
}
```

Não exponha:

- stack trace;
- detalhes internos;
- SQL;
- informações sensíveis.

Trate pelo menos:

- erros de validação;
- pauta inexistente;
- sessão inexistente;
- sessão encerrada;
- pauta com sessão já aberta;
- voto duplicado;
- violação de constraint;
- erros inesperados.

---

## 8. Persistência

Use banco relacional persistente, preferencialmente PostgreSQL.

Configure Docker Compose para facilitar a execução local.

Use Flyway para criar e evoluir o schema.

Não use:

```yaml
spring.jpa.hibernate.ddl-auto: create
```

ou:

```yaml
spring.jpa.hibernate.ddl-auto: create-drop
```

Use:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Crie migrations para:

```text
pauta
sessao_votacao
voto
```

Crie, no mínimo:

```text
UNIQUE (voto.pauta_id, voto.associado_id)
```

Crie índices coerentes para:

- `voto.pauta_id`;
- `voto.associado_id`;
- `sessao_votacao.pauta_id`.

A aplicação deve funcionar após reiniciar sem perder dados.

---

## 9. Tempo e testes

Abstraia o relógio da aplicação usando `java.time.Clock`.

Evite usar diretamente `Instant.now()` dentro das regras de negócio.

Prefira:

```java
Instant.now(clock)
```

Isso permite testar deterministicamente:

- sessão aberta;
- sessão encerrada;
- duração padrão;
- duração customizada;
- limite exato do horário `fim`.

---

## 10. Comunicação mobile

O anexo da avaliação descreve telas JSON dos tipos:

- `FORMULARIO`;
- `SELECAO`.

A API pode expor respostas compatíveis com esses formatos quando necessário para o fluxo mobile.

Quando retornar URLs para o aplicativo, o domínio deve ser configurável.

Exemplo:

```yaml
app:
  callback:
    base-url: ${CALLBACK_BASE_URL:http://localhost:8080}
```

Não deixe URLs de callback hardcoded.

O formato da URL deve ser documentado no README e no OpenAPI quando aplicável.

Não crie funcionalidades mobile fora do que for necessário para atender ao anexo.

---

## 11. Testes

### 11.1 Testes unitários

Cubra:

- criação de pauta;
- descrição inválida;
- abertura com duração padrão;
- abertura com duração customizada;
- duração zero;
- duração negativa;
- pauta inexistente;
- tentativa de abrir segunda sessão;
- voto `SIM`;
- voto `NAO`;
- voto inválido;
- voto fora do período;
- voto duplicado;
- resultado da votação.

### 11.2 Testes de integração

Cubra o fluxo:

```text
criar pauta
→ abrir sessão
→ registrar voto SIM
→ registrar voto NAO
→ consultar resultado
```

Cubra também:

- persistência;
- migrations;
- constraint única;
- restart ou reabertura do contexto;
- pauta inexistente;
- sessão encerrada;
- tentativa de abrir sessão duplicada.

### 11.3 Teste de concorrência

Crie pelo menos um teste que execute dois votos simultâneos do mesmo associado na mesma pauta.

O resultado esperado é:

- no máximo um voto persistido;
- uma requisição bem-sucedida;
- a outra retornando conflito ou falha equivalente.

Sempre que possível, execute esse teste usando PostgreSQL ou Testcontainers, não somente mocks.

### 11.4 Execução obrigatória de testes

É obrigatório executar:

```bash
./mvnw clean test
```

e não declarar conclusão da tarefa sem que esse comando seja executado com sucesso.

Se houver falhas, corrigi-las antes de encerrar o trabalho.

---

## 12. OpenAPI

Adicione documentação Swagger/OpenAPI.

Documente:

- endpoints;
- parâmetros;
- payloads;
- respostas;
- códigos HTTP;
- exemplos;
- erros possíveis.

A documentação deve ser acessível pelo README.

---

## 13. Logs

Use SLF4J/Logback.

Registre eventos úteis:

- criação de pauta;
- abertura de sessão;
- voto recebido;
- voto rejeitado;
- consulta de resultado;
- erros de integração externa, caso o bônus seja implementado.

Não registre CPF ou identificadores sensíveis desnecessariamente.

Não use `System.out.println`.

---

## 14. Tarefas bônus

Implemente somente depois de finalizar e testar os requisitos obrigatórios.

### 14.1 Bônus 1 — Integração CPF

Integrar com:

```text
GET https://user-info.herokuapp.com/users/{cpf}
```

Regras:

- `404` do serviço → CPF inválido;
- `ABLE_TO_VOTE` → permitir voto;
- `UNABLE_TO_VOTE` → rejeitar voto.

Requisitos técnicos:

- timeout;
- tratamento de erro de comunicação;
- client/service separado;
- URL configurável;
- mocks nos testes;
- nenhuma chamada externa diretamente no controller.

Parametrize a URL via configuração.

### 14.2 Bônus 2 — Performance

Considerar:

- consultas agregadas;
- índices;
- pool de conexões;
- constraint única;
- ausência de carregamento de todos os votos em memória;
- transações;
- concorrência.

Se possível, adicione teste de carga simples e documente o resultado.

### 14.3 Bônus 3 — Versionamento

Usar:

```text
/api/v1/...
```

Documentar no README que mudanças incompatíveis poderão ser introduzidas em:

```text
/api/v2/...
```

---

## 15. README

Crie um README profissional contendo:

- objetivo;
- tecnologias;
- arquitetura;
- configuração;
- banco;
- Docker Compose;
- como executar;
- como executar os testes;
- Swagger;
- exemplos de `curl`;
- decisões técnicas;
- regra de uma sessão por pauta;
- regra de voto único;
- concorrência;
- versionamento;
- bônus implementados.

O fluxo esperado deve ser:

```bash
docker compose up -d
./mvnw clean test
./mvnw spring-boot:run
```

Se o projeto não possuir Maven Wrapper, configure-o ou documente claramente o uso do Maven instalado.

Não versionar:

- `.env`;
- credenciais reais;
- dados reais;
- volumes Docker;
- `target/`;
- arquivos do banco.

Forneça variáveis de exemplo:

```text
DB_URL=jdbc:postgresql://localhost:5432/votacao
DB_USERNAME=postgres
DB_PASSWORD=postgres
CALLBACK_BASE_URL=http://localhost:8080
```

---

## 16. Execução obrigatória da aplicação

Ao final da implementação, é obrigatório:

1. subir o banco via Docker Compose, se houver Docker disponível;
2. executar os testes com Maven;
3. iniciar a aplicação;
4. validar os endpoints principais;
5. verificar que a API responde como esperado;
6. se alguma etapa não puder ser executada, informar com transparência.

É obrigatório testar manualmente ou via HTTP os fluxos principais:

- criar pauta;
- abrir sessão;
- registrar voto SIM;
- registrar voto NAO;
- consultar resultado;
- verificar que votos duplicados são rejeitados;
- verificar que a sessão encerrada rejeita novos votos.

O agente deve informar no retorno final quais comandos foram executados e quais passos foram validados.

---

## 17. Critérios de aceite

Considere a implementação concluída somente quando:

- [ ] compilar;
- [ ] testes passarem;
- [ ] pauta puder ser criada;
- [ ] sessão puder ser aberta;
- [ ] duração padrão funcionar;
- [ ] duração customizada funcionar;
- [ ] voto SIM funcionar;
- [ ] voto NAO funcionar;
- [ ] voto inválido for rejeitado;
- [ ] voto fora do período for rejeitado;
- [ ] voto duplicado for rejeitado;
- [ ] concorrência não permitir duplicidade;
- [ ] resultado apresentar SIM, NAO e total;
- [ ] dados sobreviverem ao restart;
- [ ] erros possuírem formato consistente;
- [ ] OpenAPI estiver disponível;
- [ ] README estiver atualizado;
- [ ] Docker Compose funcionar;
- [ ] decisões relevantes estiverem documentadas;
- [ ] foi executado o comando de testes com sucesso;
- [ ] a aplicação foi iniciada e os endpoints principais foram validados.

Ao finalizar, apresente:

1. resumo das alterações;
2. arquivos principais criados ou modificados;
3. comandos executados;
4. resultado dos testes;
5. bônus implementados;
6. limitações conhecidas, se houver;
7. qualquer dependência externa ou configuração especial que tenha precisado.

---

## 18. Estratégia de implementação

Implemente nesta ordem:

### Fase 1 — Projeto
Criar:
- Spring Boot
- Java
- Maven
- Banco
- Docker Compose

### Fase 2 — Domínio
Implementar:
- Pauta
- SessaoVotacao
- Voto

### Fase 3 — Persistência
Criar:
- repositories
- migrations
- índices
- unique constraint

### Fase 4 — Regras de negócio
Implementar:
- criação de pauta
- abertura de sessão
- votação
- resultado

### Fase 5 — REST
Implementar controllers e DTOs.

### Fase 6 — Erros
Implementar:
- @RestControllerAdvice
- erros de domínio
- erros de validação
- erros de persistência

### Fase 7 — Testes
Criar testes unitários e integração.

### Fase 8 — Documentação
Adicionar:
- Swagger/OpenAPI
- README
- exemplos curl

### Fase 9 — Qualidade
Executar:
```bash
./mvnw clean test
```

e, se configurado:
- checkstyle
- spotbugs
- jacoco
- sonarqube

### Fase 10 — Bônus
Somente depois de estabilizar o obrigatório:
- integração CPF
- performance
- versionamento

---

## 19. Execução pelo avaliador

O projeto será disponibilizado em um repositório GitHub e deve ser fácil de executar por terceiros.

Fluxo esperado:
```bash
git clone <repositorio>
cd <projeto>

docker compose up -d

./mvnw clean test

./mvnw spring-boot:run
```

O README deve explicar esse fluxo exatamente e informar qualquer requisito adicional.

O avaliador não deve precisar:
- instalar PostgreSQL manualmente;
- criar banco manualmente;
- criar tabelas manualmente;
- executar SQL manualmente;
- alterar código-fonte para configurar o ambiente.

A aplicação deve criar/evoluir o schema automaticamente via migrations.

O `docker-compose.yml` deve estar no GitHub.

Não versionar:
- `.env`
- senhas reais
- dados reais
- arquivos do banco
- volumes Docker
- target/

Se forem usadas variáveis de ambiente, fornecer exemplo seguro no README, como:
```text
DB_URL=jdbc:postgresql://localhost:5432/votacao
DB_USERNAME=postgres
DB_PASSWORD=postgres
CALLBACK_BASE_URL=http://localhost:8080
```

Essas credenciais são apenas locais para ambiente de teste e não devem representar credenciais reais.

---

## 20. Entregável esperado

Ao final, entregar um projeto executável contendo:

```text
.
├── src/
├── pom.xml
├── README.md
├── docker-compose.yml
├── migrations/
└── demais arquivos de configuração
```

O avaliador deve conseguir:
1. clonar/receber o projeto;
2. iniciar suas dependências;
3. executar a aplicação;
4. executar os testes;
5. abrir a documentação Swagger;
6. criar uma pauta;
7. abrir uma sessão;
8. registrar votos;
9. consultar o resultado.

---

## 21. Instrução final para o agente de desenvolvimento

Implemente a solução como um desenvolvedor Java Sênior, priorizando simplicidade, qualidade e aderência ao requisito.

Antes de escrever código:
1. analise todos os requisitos;
2. identifique as regras de negócio;
3. defina o modelo de domínio;
4. defina o contrato REST;
5. defina a estratégia de persistência;
6. identifique possíveis condições de concorrência;
7. defina os testes necessários.

Depois:
1. implemente;
2. teste;
3. revise;
4. documente;
5. valide o fluxo completo.

Regra importante:
- não invente requisitos que não estejam no teste;
- quando o documento não especificar um detalhe de implementação, escolha a alternativa mais simples, consistente e profissional;
- registre essa decisão no README.

O resultado deve parecer uma solução desenvolvida para uma avaliação técnica real de Desenvolvedor Java Sênior, demonstrando domínio de:
- Java;
- Spring Boot;
- REST;
- persistência;
- modelagem de domínio;
- regras de negócio;
- concorrência;
- testes;
- tratamento de erros;
- documentação;
- qualidade de código.

Prioridade absoluta:
1. Requisitos obrigatórios
2. Qualidade e testes
3. Documentação
4. Bônus

Não sacrificar o funcionamento do requisito obrigatório para implementar tarefas bônus.

---

## 22. Observações finais

- O projeto deve seguir a regra de uma sessão por pauta;
- a regra de voto único é por pauta e associado;
- `associadoId` deve ser tratado como String;
- o banco deve garantir a integridade da regra e a concorrência;
- o resultado deve ser obtido de forma consistente a partir do banco;
- a documentação e os testes são obrigatórios e devem ser entregues com a solução;
- a tarefa só deve ser considerada concluída após execução real dos testes e validação da aplicação;
- não declare conclusão sem evidência de execução;
- a solução deve ser simples e não sobrecarregada por over-engineering.
