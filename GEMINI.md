# GEMINI.md

## Project Overview

**TicketScale** is a SaaS platform for online ticket sales and management, designed to handle high concurrency while preventing overselling. The system ensures consistency and scalability through a robust architectural stack.

- **Primary Technologies:** Java 25, Spring Boot, Spring Security (JWT), Argon2id (Hashing).
- **Persistence & Cache:** PostgreSQL for transactional data, Redis for caching and distributed locking.
- **Messaging:** RabbitMQ for asynchronous event processing.
- **Architecture:** Clean Architecture, Domain-Driven Design (DDD), and Event-Driven Architecture.

## Building and Running

*Requer JDK 25 instalado. O Gradle Wrapper (`./gradlew`) está incluído no projeto.*

*Configure o `JAVA_HOME` no arquivo `gradle.properties` ou via variável de ambientes no arquivos .bashrc ou .zshrc (Linux/Mac) ou .bash_profile (Windows).*

```bash
# Linux/Mac $HOME/.bashrc ou $HOME/.zshrc
export JAVA_HOME=/caminho/para/jdk25
export PATH=$JAVA_HOME/bin:$PATH

# Windows .bash_profile
export JAVA_HOME=C:\caminho\para\jdk25
export PATH=$JAVA_HOME/bin:$PATH
```

- **Infraestrutura:** `docker compose up -d` (PostgreSQL, Redis, RabbitMQ)
- **Build:** `./gradlew build`
- **Run API:** `./gradlew bootRun`
- **Tests:** `./gradlew test`
- **Cobertura de Testes:** `./gradlew jacocoTestReport` (relatório em `build/reports/jacoco/test/html/`)
- **Checkstyle:** `./gradlew checkstyleMain checkstyleTest`
- **PMD:** `./gradlew pmdMain pmdTest`
- **Script de Qualidade:** `./scripts/quality-reports.sh`
- **SonarQube Local:** `docker compose up -d sonarqube` e `./gradlew sonar`

## Development Conventions

- **Ferramentas de Busca:** Utilize **ripgrep (`rg`)** ao invés de `grep` para buscas no código-fonte. O ripgrep é mais rápido e respeita automaticamente arquivos `.gitignore`.
  ```bash
  # Exemplos de uso
  rg "ReservarIngressoUseCase"                    # Buscar por classe
  rg "public.*List<.*>.*eventos"                  # Buscar padrão regex
  rg --type java "CacheManager"                   # Buscar apenas em Java
  rg --glob "*.yml" "rabbitmq"                    # Buscar em YAML
  rg -A 3 -B 3 "LockManager"                      # Buscar com contexto
  ```
- **Java 25:** Utilize as novidades e recursos mais recentes do Java 25 (e.g., Virtual Threads, Pattern Matching aprimorado, Scoped Values, Structured Concurrency, etc.) sempre que possível para garantir um código moderno e eficiente.
- **Segurança e Hashing:**
    - O hashing de senhas deve SEMPRE utilizar o `Argon2id`.
    - O uso de PEPPER é obrigatório (lido da variável de ambiente `PASSWORD_PEPPER`).
    - Nunca armazene senhas em texto plano.
    - O salt é gerado automaticamente pela biblioteca.
- **Estratégia de Testes:**
    - **Unitários:** Devem ser feitos com JUnit 5 e Mockito para as camadas de `domain`, `application` e serviços de infraestrutura puros.
      <br>*Exemplo (`application`):*
      ```java
      @ExtendWith(MockitoExtension.class)
      class ReservarIngressoUseCaseTest {
          @Mock private LockManager lockManager;
          @InjectMocks private ReservarIngressoUseCase useCase;

          @Test
          void executar_deveReservarComSucesso_quandoLockAdquirido() {
              when(lockManager.acquireLock(anyString(), anyInt())).thenReturn(true);
              Reserva reservaSalva = useCase.executar(loteId, usuarioId);
              assertNotNull(reservaSalva);
              verify(lockManager).releaseLock(anyString());
          }
      }
      ```
    - **Interfaces (Controllers):** Devem ser testados usando `@WebMvcTest` e `MockMvc`. Na versão 4.0.4, as anotações e autoconfigurações estão em pacotes modulares como `org.springframework.boot.webmvc.test.autoconfigure`.
      <br>*Exemplo (`interfaces`):*
      ```java
      @WebMvcTest(ReservaController.class)
      @AutoConfigureMockMvc(addFilters = false) // Desabilita segurança geral
      class ReservaControllerTest {
          @Autowired private MockMvc mockMvc;
          @MockitoBean private ReservarIngressoUseCase reservarIngressoUseCase;

          @Test
          void reservar_deveRetornarStatus201_quandoPayloadForValido() throws Exception {
              mockMvc.perform(post("/api/v1/reservas")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("{\"loteId\":\"...\", \"usuarioId\":\"...\"}"))
                      .andExpect(status().isCreated());
          }
      }
      ```
    - **Integração:** Devem usar `@SpringBootTest` com banco de dados **H2 em memória** (configurado em `src/test/resources/application.yml`).
      <br>*Exemplo de um `IntegrationTest`:*
      ```java
      @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
      @ActiveProfiles("test") // H2 database configurado no application-test.yml
      public class ReservarIngressoIntegrationTest {
          @Autowired private ReservarIngressoUseCase useCase;
          @MockitoBean private LockManager lockManager; // Injeta fake para dependência externa
          @Autowired private ReservaRepository reservaRepository;

          @Test
          void executar_deveGravarReservaNoH2_aoSucesso() { // Teste real em banco de memória
              Reserva salva = useCase.executar(loteId, usuarioId);
              assertTrue(reservaRepository.findById(salva.getId()).isPresent());
          }
      }
      ```
    - **Injeção de Mocks:** Utilize `@MockitoBean` em vez de `@MockBean` para compatibilidade com as versões mais recentes do Spring Boot.
- **Módulo de Pagamento:**
    - Utilize o **Strategy Pattern** via `GatewayPagamentoResolver` para gerenciar diferentes métodos de pagamento.
    - Utilize **Sealed Interfaces** (`DadosMetodoPagamento`) para garantir tipagem segura e sem nulos nos dados de pagamento.
    - **DIP (Dependency Inversion Principle):** Repositórios do domínio de pagamento devem ser interfaces puras, com implementações JPA localizadas na camada de infraestrutura.
    - **Concorrência:** Sempre adquira um lock distribuído (Redis) via `LockManager` antes de processar um pagamento para garantir idempotência.
    - **Exceções:** Crie exceções de domínio específicas herdando de `PagamentoException` e mapeie-as para status HTTP adequados no `PagamentoExceptionHandler`.
- **Idioma do Código:** Todo o código (nomes de variáveis, classes, métodos, etc.) deve ser escrito em **Português (pt-br)**, visando clareza e padronização dentro do contexto do projeto.
- **Architectural Layers:** The project follows Clean Architecture with a clear separation of concerns:
    - `domain`: Core business logic and rules (inclui `PasswordHasher`).
    - `application`: Use cases and orchestration.
    - `infrastructure`: Technical implementation (persistence, messaging, external APIs, `Argon2PasswordHasher`).
    - `interfaces`: Entry points (REST controllers, CLI, etc.).
- **Concurrency Control:** Distributed locks via Redis are used in critical paths (e.g., ticket reservation) to maintain consistency.
- **Security:** Stateless authentication using JWT and robust hashing with Argon2id.
- **Asynchronicity:** Heavy use of RabbitMQ for background tasks like notifications and reservation expiration.

## Fluxo de Trabalho e Git

- **Workflow de Commit (Obrigatório):** Antes de realizar qualquer commit, você DEVE:
    1. Validar as alterações rodando a suíte de testes: `./gradlew test`.
    2. Atualizar o arquivo `CHANGELOG.md` com as novas implementações ou correções.
    3. Revisar e atualizar o `README.md` caso novos módulos sejam concluídos ou o roadmap mude.
    4. Revisar e atualizar este arquivo (`GEMINI.md`) caso novas convenções ou tecnologias sejam adicionadas.
- **Commits Locais:** Realize commits apenas no repositório local.
- **Mensagens de Commit:** Devem ser escritas em **Português (pt-br)**.
- **Git Push:** **Não** execute `git push` para repositórios remotos sem autorização explícita do usuário.

## Key Files & Directories

- `README.md`: Documentação geral do projeto, arquitetura e roadmap.
- `GEMINI.md`: Este arquivo — contexto e diretrizes para desenvolvimento com IA.
- `CHANGELOG.md`: Registro de todas as alterações notáveis do projeto.
- `build.gradle`: Configuração de dependências e plugins (Gradle, Spring Boot 4.0.4, Java 25).
- `docker-compose.yml`: Infraestrutura local (PostgreSQL, Redis, RabbitMQ, Nginx, **SonarQube**).
- `docker-compose.prod.yml`: Configuração para Docker Swarm (produção).
- `Dockerfile`: Build multi-stage para imagem otimizada da API.
- `.github/workflows/ci.yml`: Pipeline de CI/CD com GitHub Actions.
- `config/checkstyle/checkstyle.xml`: Regras de padronização de código.
- `config/pmd/ruleset.xml`: Regras de análise PMD.
- `docs/plano_implementacao_pendencias.md`: Plano detalhado de pendências e progresso.
- `scripts/quality-reports.sh`: Script de automação de relatórios de qualidade.
- `scripts/auto-scale.sh`: Script de auto-scaling baseado em CPU/Memory (Docker Swarm).
- `scripts/dev-up-local.sh`: Script para subir ambiente local rapidamente.
- `nginx/`: Configurações do Nginx (load balancer + reverse proxy + static files).
- `frontend/`: SPA React + TypeScript + Vite (código fonte do dashboard admin).
- `src/main/resources/static/admin/`: Build final do frontend (servida pelo Spring Boot).
- `src/main/java/com/ticketscale/`: Root package com as camadas:
    - `domain/`: Entidades (`Usuario`, `Evento`, `Ingresso`, `Lote`, `Reserva`, `Pagamento`), value objects (`PeriodoEvento`), enums (`StatusIngresso`, `StatusReserva`, `StatusPagamento`, `MetodoPagamento`), sealed interface (`DadosMetodoPagamento`), eventos de domínio (`ReservaCriadaEvent`, `PagamentoConfirmadoEvent`, `CacheInvalidadoEvent`), **dashboard** (`MetricaVendas`, `RelatorioReceita`, `MetricasDashboard`), repositórios e `PasswordHasher`.
    - `application/`: Casos de uso (`ReservarIngressoUseCase`, `ProcessarPagamentoUseCase`, `GerarRelatorioVendasPorEvento`, `CalcularReceitaTotal`, `ObterMetricasDashboard`), serviços (`EventoService`, `AutenticacaoService`, `LoteService`), portas (`LockManager`, `CacheManager`, `EventPublisher`, `GatewayPagamento`, `GatewayPagamentoResolver`).
    - `infrastructure/`: Implementações técnicas (JPA para `Usuario`, `Evento`, `Pagamento`; Redis (`RedisLockManager`, `RedisCacheManagerImpl`), RabbitMQ (`RabbitMQEventPublisher`, listeners), **Dashboard** (`DashboardRepositoryImpl` com JPQL dinâmico), `Argon2PasswordHasher`, **LoggingFilter** com MDC, **SecurityFilter**, **TokenService**, mock gateways de pagamento).
    - `interfaces/`: Controllers REST (`AutenticacaoController`, `UsuarioController`, `EventoController`, `ReservaController`, `LoteController`, `PagamentoController`, `DashboardController`, `PagamentoExceptionHandler`) e DTOs.
- `src/main/resources/application.yml`: Configuração comum do Spring Boot.
- `src/main/resources/application-dev.yml`: Configurações para desenvolvimento (localhost com fallback).
- `src/main/resources/application-prod.yml`: Configurações via variáveis de ambiente (produção).
- `src/main/resources/logback-spring.xml`: Configuração de logs estruturados com MDC.
- `src/main/resources/data.sql`: Dados iniciais para desenvolvimento.
- `src/test/`: Testes unitários, de integração (Testcontainers) e de controller (`@WebMvcTest`).
