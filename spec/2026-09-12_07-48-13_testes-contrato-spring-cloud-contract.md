# Especificação Técnica: Testes de Contrato (Spring Cloud Contract — Escopo Mínimo)

## 1. Visão Geral

Este documento define o plano arquitetural e operacional para a implementação de **Testes de Contrato orientados pelo Consumidor (Consumer-Driven Contracts - CDC)** no projeto **TicketScale** utilizando o **Spring Cloud Contract** com **YAML DSL**.

O objetivo primordial é assegurar a compatibilidade e a estabilidade da API REST contra *breaking changes*, garantindo que as integrações com os consumidores (Frontend/SPA Admin, Mobile ou outros serviços) permaneçam íntegras e fornecendo **stubs WireMock** versionados e prontamente consumíveis.

---

## 2. Decisões Arquiteturais (Alinhadas via Grill-Me)

1. **Formato dos Contratos (YAML DSL):**
   - Uso de arquivos `.yml` declarativos sob `src/test/resources/contracts/`.
   - Agnóstico de linguagem, eliminando dependências do compilador Groovy e facilitando a leitura direta por desenvolvedores frontend, mobile e QA.
2. **Escopo Mínimo Selecionado:**
   - **Autenticação (`/api/login`):**
     - Sucesso: `200 OK` com payload contendo `token` JWT válido.
     - Falha: `401 Unauthorized` com mensagem de credenciais inválidas.
   - **Reserva de Ingressos (`/api/v1/reservas`):**
     - Sucesso: `201 Created` contendo UUIDs da reserva, usuário, ingresso, status `RESERVADA`, `dataCriacao` e `dataExpiracao`.
     - Falha de validação: `400 Bad Request` para payload inconsistente/nulo.
3. **Estratégia das Classes Base do Provider:**
   - Mapeamento por pacote com **MockMvc Standalone** e mocks do Mockito (`AutenticacaoBase` e `ReservaBase`).
   - Testes ultrarrápidos, determinísticos e isolados, sem necessidade de carregar banco de dados, Docker ou dependências externas pesadas.
4. **Validação do Consumidor e Stubs:**
   - Geração automática de stubs WireMock (`stubs.jar` e mappings JSON).
   - Teste automatizado de consumidor (`TicketScaleConsumerContractTest`) com `@AutoConfigureStubRunner` em ambiente de teste, comprovando que o cliente consome o stub com sucesso.
   - Documentação para uso dos stubs pelo Frontend/SPA Admin.
5. **Integração no Build e CI/CD:**
   - Execução automática como parte integrante do ciclo `./gradlew test` e `./gradlew check`.

---

## 3. Stack Tecnológica e Dependências

- **Linguagem & Runtime:** Java 25.
- **Build Tool:** Gradle 9.4.0.
- **Plugins Gradle:**
  - `org.springframework.cloud.contract` (versão `4.3.0`).
- **Dependências de Teste:**
  - `org.springframework.cloud:spring-cloud-starter-contract-verifier:4.3.0`
  - `org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.3.0`
  - `io.rest-assured:spring-mock-mvc`
- **Diretórios do Projeto:**
  - Contratos YAML: `src/test/resources/contracts/autenticacao/` e `src/test/resources/contracts/reserva/`
  - Classes Base do Provider: `src/test/java/com/ticketscale/contract/`
  - Teste de Consumidor: `src/test/java/com/ticketscale/contract/consumer/`

---

## 4. Estrutura dos Contratos YAML

### 4.1 Autenticação: `src/test/resources/contracts/autenticacao/`

#### `login-sucesso.yml`
```yaml
description: "Deve autenticar com sucesso e retornar token JWT"
request:
  method: POST
  url: /api/login
  headers:
    Content-Type: application/json
  body:
    login: "usuario.teste"
    senha: "senhaValida123"
  matchers:
    body:
      - path: $.login
        type: by_regex
        value: "[a-zA-Z0-9._-]+"
      - path: $.senha
        type: by_regex
        value: ".+"
response:
  status: 200
  headers:
    Content-Type: application/json
  body:
    token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy.token"
  matchers:
    body:
      - path: $.token
        type: by_regex
        value: "^eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$"
```

#### `login-credenciais-invalidas.yml`
```yaml
description: "Deve retornar 401 quando as credenciais forem incorretas"
request:
  method: POST
  url: /api/login
  headers:
    Content-Type: application/json
  body:
    login: "usuario.teste"
    senha: "senhaIncorreta"
response:
  status: 401
  headers:
    Content-Type: application/json
  body:
    mensagem: "Bad credentials"
```

### 4.2 Reserva: `src/test/resources/contracts/reserva/`

#### `criar-reserva-sucesso.yml`
```yaml
description: "Deve criar uma reserva com sucesso e retornar 201 Created"
request:
  method: POST
  url: /api/v1/reservas
  headers:
    Content-Type: application/json
  body:
    loteId: "11111111-1111-1111-1111-111111111111"
    usuarioId: "22222222-2222-2222-2222-222222222222"
  matchers:
    body:
      - path: $.loteId
        type: by_regex
        value: "[a-f0-9\\-]{36}"
      - path: $.usuarioId
        type: by_regex
        value: "[a-f0-9\\-]{36}"
response:
  status: 201
  headers:
    Content-Type: application/json
  body:
    id: "33333333-3333-3333-3333-333333333333"
    usuarioId: "22222222-2222-2222-2222-222222222222"
    ingressoId: "44444444-4444-4444-4444-444444444444"
    status: "RESERVADO"
    dataCriacao: "2026-09-12T07:45:00"
    dataExpiracao: "2026-09-12T07:55:00"
  matchers:
    body:
      - path: $.id
        type: by_regex
        value: "[a-f0-9\\-]{36}"
      - path: $.usuarioId
        type: by_regex
        value: "[a-f0-9\\-]{36}"
      - path: $.ingressoId
        type: by_regex
        value: "[a-f0-9\\-]{36}"
      - path: $.status
        type: by_regex
        value: "RESERVADO"
```

#### `criar-reserva-payload-invalido.yml`
```yaml
description: "Deve retornar 400 Bad Request ao enviar payload inválido para reserva"
request:
  method: POST
  url: /api/v1/reservas
  headers:
    Content-Type: application/json
  body:
    loteId: null
    usuarioId: null
response:
  status: 400
```

---

## 5. Classes Base dos Testes do Provider

### 5.1 `AutenticacaoBase.java`
- Localização: `src/test/java/com/ticketscale/contract/AutenticacaoBase.java`
- Configuração: Instancia `AutenticacaoController` com mocks de `AuthenticationManager` e `TokenService`.
- Inicialização com `RestAssuredMockMvc.standaloneSetup(controller)`.
- Stubbing:
  - Para `usuario.teste` / `senhaValida123` $\rightarrow$ autenticação mockada com sucesso e retorno de token JWT.
  - Para credenciais inválidas $\rightarrow$ lança `BadCredentialsException("Bad credentials")`.

### 5.2 `ReservaBase.java`
- Localização: `src/test/java/com/ticketscale/contract/ReservaBase.java`
- Configuração: Instancia `ReservaController` com mock de `ReservarIngressoUseCase`.
- Inicialização com `RestAssuredMockMvc.standaloneSetup(controller)`.
- Stubbing:
  - Quando chamado com os UUIDs do contrato $\rightarrow$ retorna entidade `Reserva` com status `StatusIngresso.RESERVADO` e datas preenchidas.

---

## 6. Validação do Consumidor (`TicketScaleConsumerContractTest`)

- Localização: `src/test/java/com/ticketscale/contract/consumer/TicketScaleConsumerContractTest.java`
- Configuração:
  ```java
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
  @AutoConfigureStubRunner(
      ids = "com.ticketscale:ticket-scale",
      stubsMode = StubRunnerProperties.StubsMode.LOCAL
  )
  ```
- Executa chamadas HTTP reais via `RestClient` ou `RestTemplate` contra a porta dinâmica do WireMock, validando o consumo dos contratos de Login e Reserva.

---

## 7. Plano de Verificação e Critérios de Aceite

### Critérios de Aceite:
1. Contratos YAML criados e validados para `/api/login` e `/api/v1/reservas` (fluxos de sucesso e erro).
2. Testes de verificação do Provider gerados pelo plugin e executados com sucesso no build Gradle (`./gradlew test`).
3. Stubs WireMock gerados corretamente.
4. Teste de consumidor (`TicketScaleConsumerContractTest`) aprovado com `@AutoConfigureStubRunner`.
5. Nenhum impacto negativo na suíte de testes existente ou cobertura JaCoCo.
6. `README.md`, `CHANGELOG.md` e documentação de pendências atualizados.

### Comandos de Validação:
```bash
# 1. Gerar testes de contrato a partir dos YAMLs
./gradlew generateContractTests

# 2. Executar suíte completa de testes
./gradlew test

# 3. Gerar e validar relatório JaCoCo
./gradlew jacocoTestReport

# 4. Checagens de qualidade de código
./gradlew checkstyleMain checkstyleTest pmdMain pmdTest
```
