# GEMINI.md

## Project Overview

**TicketScale** is a SaaS platform for online ticket sales and management, designed to handle high concurrency while preventing overselling. The system ensures consistency and scalability through a robust architectural stack.

- **Primary Technologies:** Java 25, Spring Boot, Spring Security (JWT).
- **Persistence & Cache:** PostgreSQL for transactional data, Redis for caching and distributed locking.
- **Messaging:** RabbitMQ for asynchronous event processing.
- **Architecture:** Clean Architecture, Domain-Driven Design (DDD), and Event-Driven Architecture.

## Building and Running

*Note: The project is currently in its initial setup phase. Build and run commands will be updated as the implementation progresses.*

- **Build:** `TODO: Add build command (e.g., ./mvnw clean install or ./gradlew build)`
- **Run API:** `TODO: Add run command (e.g., ./mvnw spring-boot:run or ./gradlew bootRun)`
- **Tests:** `TODO: Add test command (e.g., ./mvnw test or ./gradlew test)`

## Development Conventions

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

- **Commits Locais:** Realize commits apenas no repositório local.
- **Mensagens de Commit:** Devem ser escritas em **Português (pt-br)**.
- **Git Push:** **Não** execute `git push` para repositórios remotos sem autorização explícita do usuário.

## Key Files & Directories

- `README.md`: High-level project documentation, architecture diagrams, and roadmap.
- `src/main/java/com/ticketscale`: (Planned) Root package for the implementation.
- `GEMINI.md`: This file, providing context and instructions for AI-assisted development.
