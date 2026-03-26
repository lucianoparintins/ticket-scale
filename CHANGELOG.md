# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Added
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
