# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Added
- **Módulo de Pagamento de Ingressos (Fase 4):**
  - Implementação do fluxo de pagamento seguindo Clean Architecture, SOLID e DDD.
  - Suporte a 3 métodos de pagamento: **Pix**, **Cartão de Débito** e **Cartão de Crédito**.
  - Design baseado em **Strategy pattern** com `GatewayPagamentoResolver` para seleção de gateways.
  - **Inversão de Dependência (DIP)**: `PagamentoRepository` como interface pura no domínio, com implementação JPA na infraestrutura.
  - Caso de uso `ProcessarPagamentoUseCase` com **lock distribuído (Redis)** para garantir idempotência e evitar race conditions.
  - Sealed interface `DadosMetodoPagamento` (Java 25) para eliminação de campos nulos e validação por tipo (pattern matching).
  - Entidade de domínio `Pagamento` com ciclo de vida (`PENDENTE`, `APROVADO`, `RECUSADO`).
  - Evento de domínio `PagamentoConfirmadoEvent` integrado ao RabbitMQ via `EventPublisher`.
  - Mocks de gateways (`MockGatewayPix`, `MockGatewayCartaoDebito`, `MockGatewayCartaoCredito`) para desenvolvimento e testes.
  - Tratamento de exceções centralizado com `PagamentoExceptionHandler` mapeando erros de domínio para códigos HTTP.
  - Testes unitários abrangentes para domínio, aplicação e infraestrutura.
  - Teste de controller com `@WebMvcTest` para validação de payload e respostas REST.
- **Fase 2 e 3 - Melhorias de Qualidade e Código:**
  - Logs estruturados com MDC e correlation ID (`LoggingFilter`).
  - Configuração do Logback com padrão estruturado (`logback-spring.xml`).
  - Spring Boot Actuator para health checks e métricas.
  - Micrometer com Prometheus para monitoramento.
  - Refatoração das entidades para Builder Pattern (`Evento`, `Reserva`, `Ingresso`, `Lote`).
  - Validações reforçadas nos builders das entidades.
  - Endpoints de actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.
- **Qualidade de Software e CI/CD:**
  - Unificação dos pacotes de portas da aplicação: `application.ports` removido e `LockManager` movido para `application.port.out`.
  - Configuração do JaCoCo para relatórios de cobertura de testes (HTML/XML).

  - Integração do Checkstyle para padronização de código Java.
  - **PMD** para análise estática de bugs e más práticas.
  - **SonarQube Local** (via Docker) como alternativa ao SonarCloud.
  - Pipeline de CI/CD com GitHub Actions (`.github/workflows/ci.yml`).
  - OWASP Dependency Check para segurança de dependências.
  - Script de automação de relatórios (`scripts/quality-reports.sh`).
  - Documentação do plano de melhoria de qualidade em `docs/plano_melhoria_qualidade.md`.
  - Dashboard de qualidade em `docs/quality-dashboard.md`.
- Plano de implementação da tecnologia de Pagamentos (`docs/implementation_plan_pagamentos.md`) revisado e adequado aos princípios de Clean Architecture, DDD, SOLID e concorrência (lock distribuído).
- Habilitação de Virtual Threads do Java 25 no Spring Boot (`spring.threads.virtual.enabled: true`).
- Integração com RabbitMQ (Fase 5):
  - Porta `EventPublisher` na camada de aplicação.
  - Evento de domínio `ReservaCriadaEvent`.
  - Configuração da infraestrutura via `RabbitMQConfig`.
  - Implementação `RabbitMQEventPublisher`.
  - Listeners para processamento em background (`ExpiracaoReservaListener` e `NotificacaoListener`).
  - Testes unitários e de integração para mensageria (`RabbitMQEventPublisherTest`, `RabbitMQIntegrationTest`, etc).
- Adição de exemplos de código para testes unitários, testes de controller (WebMvcTest) e de integração no `GEMINI.md`.
- Sistema de Reserva de Ingressos com Redis:
  - Entidades de domínio: `Ingresso`, `Lote`, `Reserva` com enums `StatusIngresso` e `StatusReserva`.
  - Repositórios de domínio: `IngressoRepository`, `LoteRepository`, `ReservaRepository`.
  - Interface `LockManager` na camada de aplicação (porta para lock distribuído).
  - `ReservarIngressoUseCase` na camada de aplicação com lock distribuído via Redis.
  - `RedisLockManager` na camada de infraestrutura (implementação do `LockManager` com `RedisTemplate`).
  - `ReservaController` com endpoint para criar reservas.
  - DTOs: `ReservaRequestDTO` e `ReservaResponseDTO`.
  - Testes unitários para `ReservarIngressoUseCase`, `Ingresso` e `Reserva`.
  - Teste de controller para `ReservaController` com `@WebMvcTest`.
  - Teste de integração `ReservarIngressoIntegrationTest` com `@SpringBootTest`.
- Módulo de CRUD de Eventos:
  - Entidade `Evento` e Value Object `PeriodoEvento` na camada de domínio.
  - Interface `EventoRepository` e implementação JPA na infraestrutura.
  - `EventoService` na camada de aplicação para orquestrar as operações de negócio.
  - `EventoController` com endpoints para criar, listar, detalhar e desativar eventos.
  - Controle de acesso por perfil: criação e remoção restritas a `ROLE_ADMIN`.
  - Testes unitários para `EventoService` e `EventoController`.
- Hashing de senhas com Argon2id:
  - Interface `PasswordHasher` na camada de domínio.
  - Implementação `Argon2PasswordHasher` na camada de infraestrutura (Argon2id, iterations=3, memory=64MB, parallelism=1).
  - Suporte a PEPPER via variável de ambiente `PASSWORD_PEPPER`.
  - Integração nativa com Spring Security (`Argon2PasswordHasher` implementa `PasswordEncoder`).
  - Testes unitários para o novo mecanismo de hash (`Argon2PasswordHasherTest`).
- Documentação de análise inicial do projeto e roadmap estratégico em `docs/analise_projeto_inicial.md`.
- Configuração de perfis de aplicação:
  - `application.yml`: Configurações comuns e ativação do perfil `dev` por padrão.
  - `application-dev.yml`: Configurações locais (PostgreSQL, Redis, RabbitMQ em localhost).
  - `application-prod.yml`: Configurações via variáveis de ambiente para produção.
- Workflow de commit obrigatório:
  - Exigência de atualização de `CHANGELOG.md`, `README.md` e `GEMINI.md` antes de cada commit.

### Changed
- `application.yml` original: Decomposto nos perfis `dev` e `prod` para melhor gestão de ambientes.

## [0.0.1] - 2026-03-24

### Added
- Módulo de Autenticação com JWT:
...
- Estrutura de pacotes seguindo Clean Architecture.
- Configuração de infraestrutura com Docker Compose (PostgreSQL, Redis, RabbitMQ).
- Documentação inicial (`README.md` e `GEMINI.md`).
