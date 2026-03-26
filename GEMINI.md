# GEMINI.md

## Project Overview

**TicketScale** is a SaaS platform for online ticket sales and management, designed to handle high concurrency while preventing overselling. The system ensures consistency and scalability through a robust architectural stack.

- **Primary Technologies:** Java 25, Spring Boot, Spring Security (JWT), Argon2id (Hashing).
- **Persistence & Cache:** PostgreSQL for transactional data, Redis for caching and distributed locking.
- **Messaging:** RabbitMQ for asynchronous event processing.
- **Architecture:** Clean Architecture, Domain-Driven Design (DDD), and Event-Driven Architecture.

## Building and Running

*Requer JDK 25 instalado. O Gradle Wrapper (`./gradlew`) está incluído no projeto.*

*Configure o `JAVA_HOME` no arquivo `gradle.properties` ou via variável de ambiente.*

- **Infraestrutura:** `docker compose up -d` (PostgreSQL, Redis, RabbitMQ)
- **Build:** `JAVA_HOME=/caminho/para/jdk25 ./gradlew build`
- **Run API:** `JAVA_HOME=/caminho/para/jdk25 ./gradlew bootRun`
- **Tests:** `JAVA_HOME=/caminho/para/jdk25 ./gradlew test`

## Development Conventions

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
    1. Validar as alterações rodando a suíte de testes: `JAVA_HOME=/caminho/para/jdk25 ./gradlew test`.
    2. Atualizar o arquivo `CHANGELOG.md` com as novas implementações ou correções.
    3. Revisar e atualizar o `README.md` caso novos módulos sejam concluídos ou o roadmap mude.
    4. Revisar e atualizar este arquivo (`GEMINI.md`) caso novas convenções ou tecnologias sejam adicionadas.
- **Commits Locais:** Realize commits apenas no repositório local.
- **Mensagens de Commit:** Devem ser escritas em **Português (pt-br)**.
- **Git Push:** **Não** execute `git push` para repositórios remotos sem autorização explícita do usuário.

## Key Files & Directories

- `README.md`: High-level project documentation, architecture diagrams, and roadmap.
- `GEMINI.md`: This file, providing context and instructions for AI-assisted development.
- `CHANGELOG.md`: Registro de todas as alterações notáveis do projeto.
- `build.gradle`: Configuração de dependências e plugins (Gradle).
- `docker-compose.yml`: Infraestrutura local (PostgreSQL, Redis, RabbitMQ).
- `src/main/java/com/ticketscale/`: Root package com as camadas:
    - `domain/`: Entidades (`Usuario`, `Evento`, `Ingresso`, `Lote`, `Reserva`), value objects (`PeriodoEvento`), enums (`StatusIngresso`, `StatusReserva`), repositórios e `PasswordHasher`.
    - `application/`: Casos de uso e portas (`EventoService`, `AutenticacaoService`, `ReservarIngressoUseCase`, `LockManager`).
    - `infrastructure/`: Implementações técnicas (JPA para `Usuario` e `Evento`, Redis (`RedisLockManager`), RabbitMQ, `Argon2PasswordHasher`).
    - `interfaces/`: Controllers REST (`AutenticacaoController`, `UsuarioController`, `EventoController`, `ReservaController`) e DTOs.
- `src/main/resources/application.yml`: Configuração do Spring Boot.
