# 🎟️ Análise do Projeto TicketScale — Por Onde Começar

## Estado Atual

O projeto possui **apenas documentação** ([README.md] e [GEMINI.md]). Não há:
- Nenhum código fonte Java
- Nenhum arquivo de build (`pom.xml` / `build.gradle`)
- Nenhuma estrutura de pacotes
- Nenhum `docker-compose.yml` ou configuração de infraestrutura

O projeto está na **fase zero** — precisa ser scaffolded do zero.

---

## 🗺️ Ordem Recomendada de Implementação

Baseado no roadmap do [README.md], nas dependências entre os módulos, e nas boas práticas de Clean Architecture + DDD:

### Fase 1 — Scaffolding do Projeto ⬅️ **COMEÇAR AQUI**

| Passo | Descrição |
|-------|-----------|
| 1.1 | Criar projeto Spring Boot (Java 21) com Spring Initializr (Maven ou Gradle) |
| 1.2 | Adicionar dependências: Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Spring Data Redis, Spring AMQP |
| 1.3 | Criar estrutura de pacotes Clean Architecture: `domain`, `application`, `infrastructure`, `interfaces` |
| 1.4 | Configurar `application.yml` com profiles (dev, prod) |
| 1.5 | Criar `docker-compose.yml` com PostgreSQL, Redis e RabbitMQ |

### Fase 2 — Módulo de Autenticação (JWT)

| Passo | Descrição |
|-------|-----------|
| 2.1 | Domínio: entidade `Usuario` com regras de negócio |
| 2.2 | Application: casos de uso `CadastrarUsuario`, `AutenticarUsuario` |
| 2.3 | Infrastructure: repositório JPA, configuração JWT (geração e validação de tokens) |
| 2.4 | Infrastructure: configuração Spring Security (filtros, `SecurityFilterChain`) |
| 2.5 | Interfaces: controllers REST (`/auth/cadastro`, `/auth/login`) |

### Fase 3 — CRUD de Eventos

| Passo | Descrição |
|-------|-----------|
| 3.1 | Domínio: entidade `Evento`, value objects (ex: `PeriodoEvento`) |
| 3.2 | Application: casos de uso CRUD (`CriarEvento`, `EditarEvento`, `ListarEventos`, etc.) |
| 3.3 | Infrastructure: repositório JPA para `Evento` |
| 3.4 | Interfaces: controllers REST com autorização por perfil (Admin/Cliente) |
| 3.5 | Cache: consulta de eventos via Redis |

### Fase 4 — Sistema de Reserva com Redis

| Passo | Descrição |
|-------|-----------|
| 4.1 | Domínio: entidades `Ingresso`, `Reserva`, `Lote` |
| 4.2 | Application: caso de uso `ReservarIngresso` com lock distribuído |
| 4.3 | Infrastructure: implementação de lock distribuído via Redis |
| 4.4 | Interfaces: endpoint de reserva |

### Fase 5 — Integração com RabbitMQ + Workers

| Passo | Descrição |
|-------|-----------|
| 5.1 | Infrastructure: configuração de filas e exchanges no RabbitMQ |
| 5.2 | Publicação de eventos de domínio (ex: `ReservaCriada`) |
| 5.3 | Workers: consumidores para notificações e expiração de reservas |

### Fase 6 — Dashboard Administrativo

| Passo | Descrição |
|-------|-----------|
| 6.1 | Endpoints para relatórios e métricas de vendas |
| 6.2 | Agregações de dados (vendas por evento, receita, etc.) |

---

## 🎯 Recomendação: Começar pela Fase 1

A **Fase 1 (Scaffolding)** é o ponto de partida obrigatório. Sem ela, não há ambiente para desenvolver. Após o scaffolding, a **Fase 2 (Autenticação JWT)** é o próximo passo natural, pois praticamente todos os outros módulos dependem de autenticação.

### Decisão necessária do usuário:

> [!IMPORTANT]
> **Maven ou Gradle?** — O [GEMINI.md] menciona ambos como possibilidades. É preciso definir qual ferramenta de build usar.

> [!IMPORTANT]
> **Spring Initializr ou scaffolding manual?** — Podemos usar `curl` no [start.spring.io](https://start.spring.io) para gerar o projeto automaticamente, ou criar os arquivos manualmente.
