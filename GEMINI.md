# GEMINI.md

## Project Overview

**TicketScale** is a SaaS platform for online ticket sales and management, designed to handle high concurrency while preventing overselling. The system ensures consistency and scalability through a robust architectural stack.

- **Primary Technologies:** Java 25, Spring Boot, Spring Security (JWT).
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
- **Estratégia de Testes:**
    - **Unitários:** Devem ser feitos com JUnit 5 e Mockito para as camadas de `domain`, `application` e serviços de infraestrutura puros.
    - **Interfaces (Controllers):** Devem ser testados usando `@WebMvcTest` e `MockMvc`. Na versão 4.0.4, as anotações e autoconfigurações estão em pacotes modulares como `org.springframework.boot.webmvc.test.autoconfigure`.
    - **Integração:** Devem usar `@SpringBootTest` com banco de dados **H2 em memória** (configurado em `src/test/resources/application.yml`).
    - **Injeção de Mocks:** Utilize `@MockitoBean` em vez de `@MockBean` para compatibilidade com as versões mais recentes do Spring Boot.
- **Idioma do Código:** Todo o código (nomes de variáveis, classes, métodos, etc.) deve ser escrito em **Português (pt-br)**, visando clareza e padronização dentro do contexto do projeto.
- **Architectural Layers:** The project follows Clean Architecture with a clear separation of concerns:
    - `domain`: Core business logic and rules.
    - `application`: Use cases and orchestration.
    - `infrastructure`: Technical implementation (persistence, messaging, external APIs).
    - `interfaces`: Entry points (REST controllers, CLI, etc.).
- **Concurrency Control:** Distributed locks via Redis are used in critical paths (e.g., ticket reservation) to maintain consistency.
- **Security:** Stateless authentication using JWT.
- **Asynchronicity:** Heavy use of RabbitMQ for background tasks like notifications and reservation expiration.

## Fluxo de Trabalho e Git

- **Workflow de Commit (Obrigatório):** Antes de realizar qualquer commit, você DEVE:
    1. Validar as alterações rodando a suíte de testes: `JAVA_HOME=/home/luke/dev/java/jdk25 ./gradlew test`.
    2. Atualizar o arquivo `CHANGELOG.md` com as novas implementações ou correções.
    3. Revisar e atualizar o `README.md` caso novos módulos sejam concluídos ou o roadmap mude.
    4. Revisar e atualizar este arquivo (`GEMINI.md`) caso novas convenções ou tecnologias sejam adicionadas.
- **Commits Locais:** Realize commits apenas no repositório local.
- **Mensagens de Commit:** Devem ser escritas em **Português (pt-br)**.
- **Git Push:** **Não** execute `git push` para repositórios remotos sem autorização explícita do usuário.

## Key Files & Directories

- `README.md`: High-level project documentation, architecture diagrams, and roadmap.
- `GEMINI.md`: This file, providing context and instructions for AI-assisted development.
- `build.gradle`: Configuração de dependências e plugins (Gradle).
- `docker-compose.yml`: Infraestrutura local (PostgreSQL, Redis, RabbitMQ).
- `src/main/java/com/ticketscale/`: Root package com as camadas:
    - `domain/`: Entidades, value objects e regras de negócio.
    - `application/`: Casos de uso e portas.
    - `infrastructure/`: Implementações técnicas (JPA, Redis, RabbitMQ).
    - `interfaces/`: Controllers REST e DTOs.
- `src/main/resources/application.yml`: Configuração do Spring Boot.
