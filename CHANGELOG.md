# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Adicionado
- **Cache de Leitura com Redis (Política Cache-aside):**
  - Interface `CacheManager` na camada de aplicação para desacoplamento.
  - Implementação `RedisCacheManagerImpl` na infraestrutura usando Spring Cache (`@Cacheable`, `@CacheEvict`).
  - TTLs customizados: Eventos (5 min), Lotes (2 min), Dashboard (10 min).
  - Listener `CacheInvalidationListener` para invalidação distribuída via RabbitMQ (`CacheInvalidadoEvent`).
  - Testes de integração: `EventoCacheIntegrationTest`, `EventoControllerRedisCacheIT`.
- **Dashboard Administrativo — UI Completa:**
  - SPA React + TypeScript + Vite no diretório `/frontend`, com build para `src/main/resources/static/admin`.
  - Páginas: Login (JWT), Dashboard (métricas), Eventos (CRUD), Vendas (relatório por evento).
  - `FrontendForwardController` para roteamento SPA no Spring Boot.
  - Autenticação JWT integrada via `AuthContext` e `ProtectedRoute`.
  - Estilização com Vanilla CSS seguindo diretrizes do `GEMINI.md`.
- **Testcontainers para Testes de Integração:**
  - PostgreSQL real via Docker para testes de integração (`Testcontainers` + `@ServiceConnection`).
  - `DashboardRepositoryPostgresIT` validando consultas JPQL em banco real.
  - Perfil `redis-it` com configuração dedicada (`application-redis-it.yml`).

### Corrigido
- **Configuração de Ambiente (Docker):**
  - `application-dev.yml` com variáveis de ambiente e valores padrão (fallback para localhost), permitindo conexão correta aos serviços via Docker Compose ou localmente.

### Alterado
- Unificação das seções "Adicionado" e "Added" do CHANGELOG para organização consistente.
- Refatoração de código para remoção de imports não utilizados em classes de infraestrutura e suítes de teste.
- `RabbitMQConfig` ajustado para utilizar `JacksonJsonMessageConverter`.
- Perfis de aplicação reorganizados: `dev` (padrão) e `prod` (variáveis de ambiente).

### Removido
- OWASP Dependency Check do CI (`.github/workflows/ci.yml`) para reduzir tempo de execução. Scan disponível localmente via `./gradlew dependencyCheckAnalyze`.

---

## [Implementações Anteriores]

### Autenticação e Segurança
- Módulo de autenticação com JWT e testes automatizados.
- Hashing de senhas com Argon2id (iterations=3, memory=64MB, parallelism=1).
- Suporte a PEPPER via variável de ambiente `PASSWORD_PEPPER`.
- Spring Security com `Argon2PasswordHasher` implementando `PasswordEncoder`.

### Eventos e Reservas
- CRUD completo de eventos com controle de acesso por perfil (`ROLE_ADMIN`).
- Entidade `Evento` com Value Object `PeriodoEvento`.
- Sistema de reserva com lock distribuído Redis (`RedisLockManager`).
- Entidades `Ingresso`, `Lote`, `Reserva` com enums de status.
- `ReservarIngressoUseCase` com aquisição de lock distribuído.

### Módulo de Pagamento
- 3 métodos: **Pix**, **Cartão de Débito**, **Cartão de Crédito**.
- Strategy pattern com `GatewayPagamentoResolver`.
- Sealed interface `DadosMetodoPagamento` (Java 25) para tipagem segura.
- `ProcessarPagamentoUseCase` com lock distribuído para idempotência.
- Evento de domínio `PagamentoConfirmadoEvent` via RabbitMQ.
- Mocks de gateways para desenvolvimento e testes.
- `PagamentoExceptionHandler` mapeando erros para HTTP.

### Mensageria (RabbitMQ)
- `EventPublisher` e `RabbitMQEventPublisher`.
- Eventos: `ReservaCriadaEvent`, `PagamentoConfirmadoEvent`.
- Listeners: `ExpiracaoReservaListener`, `NotificacaoListener`, `CacheInvalidationListener`.

### Dashboard — APIs
- Records de domínio: `MetricaVendas`, `RelatorioReceita`, `MetricasDashboard`.
- `DashboardRepositoryImpl` com JPQL dinâmico (`EntityManager`).
- Casos de uso: `GerarRelatorioVendasPorEvento`, `CalcularReceitaTotal`, `ObterMetricasDashboard`.
- Endpoints: `/dashboard/vendas-por-evento`, `/dashboard/receita-total`, `/dashboard/metricas`.
- Restrição de acesso a `ADMIN` via `SecurityConfigurations`.

### Qualidade de Software e CI/CD
- JaCoCo (cobertura ≥ 80%), Checkstyle, PMD, SonarQube Local.
- Pipeline CI/CD com GitHub Actions.
- Spring Actuator + Micrometer Prometheus.
- Logs estruturados com MDC e correlation ID (`LoggingFilter`, `logback-spring.xml`).
- Script `quality-reports.sh` para geração automatizada de relatórios.
- Refatoração para Builder Pattern em `Evento`, `Reserva`, `Ingresso`, `Lote`.
- Virtual Threads do Java 25 habilitados (`spring.threads.virtual.enabled: true`).

## [0.0.1] - 2026-03-24

### Added
- Módulo de Autenticação com JWT:
...
- Estrutura de pacotes seguindo Clean Architecture.
- Configuração de infraestrutura com Docker Compose (PostgreSQL, Redis, RabbitMQ).
- Documentação inicial (`README.md` e `GEMINI.md`).
