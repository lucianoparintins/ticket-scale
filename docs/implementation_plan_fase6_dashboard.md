# 📊 Plano de Implementação — Fase 6: Dashboard Administrativo

## 🎯 Objetivo

Desenvolver endpoints e funcionalidades para relatórios e métricas de vendas, permitindo que gestores acompanhem o desempenho de eventos, receitas e outras métricas relevantes.

---

## 📚 Contexto Atual do Projeto

### O que já está implementado

#### 1. **Estrutura Clean Architecture**
```
src/main/java/com/ticketscale/
├── domain/           → Regras de negócio puras
├── application/      → Casos de uso
├── infrastructure/   → Implementações técnicas
└── interfaces/       → Controllers REST
```

#### 2. **Padrões de Entidades**
- Entidades usam **Builder Pattern** (ex: `Reserva`, `Pagamento`)
- Métodos de domínio ricos (ex: `confirmarPagamento()`, `cancelar()`)
- Construtores privados forçando uso do builder
- Exemplo: `Reserva` com métodos `confirmarPagamento()`, `cancelar()`, `isExpirada()`

#### 3. **Repositórios**
- Interface de domínio estendendo `JpaRepository`
- Exemplo: `IngressoRepository` com método `findFirstByLoteIdAndStatus()`
- Consultas via JPQL com `@Query` quando necessário

#### 4. **Casos de Uso**
- Services anotados com `@Service`
- Transacionais com `@Transactional`
- Injeção de dependências via construtor
- Exemplo: `ReservarIngressoUseCase` com lock distribuído

#### 5. **DTOs como Records**
- Records Java 16+ para DTOs
- Validações com Bean Validation
- Construtores de mapeamento nas classes record
- Exemplo: `DadosCadastroEvento`, `ReservaRequestDTO`

#### 6. **Segurança**
- Spring Security com JWT
- `SecurityConfigurations` define regras de acesso
- Roles: `ADMIN`, `USUARIO`
- Password hashing com Argon2

#### 7. **Redis**
- Usado **apenas para lock distribuído** (`RedisLockManager`)
- **NÃO há cache de dados implementado ainda**

#### 8. **O que NÃO está implementado**
- ❌ Specifications / JpaSpecificationExecutor
- ❌ Cache de dados com Redis (`@Cacheable`)
- ❌ OpenAPI/Swagger

---

## 📋 Tarefas de Implementação Detalhadas

### 6.1 — Definição dos Requisitos do Dashboard

| ID | Tarefa | Detalhes de Implementação |
|----|--------|---------------------------|
| 6.1.1 | Definir métricas | Baseado nas entidades existentes: `Reserva`, `Ingresso`, `Pagamento` |
| 6.1.2 | Definir períodos | Usar `LocalDateTime` como `PeriodoEvento` |
| 6.1.3 | Definir filtros | Query params nos endpoints: `?dataInicio=`, `?dataFim=`, `?eventoId=` |

---

### 6.2 — Camada de Domínio (Domain)

**Pasta:** `src/main/java/com/ticketscale/domain/dashboard/`

| ID | Arquivo | Descrição | Padrão a Seguir |
|----|---------|-----------|-----------------|
| 6.2.1 | `MetricaVendas.java` | Value Object como **record** com campos: `UUID eventoId`, `String eventoNome`, `long ingressosVendidos`, `BigDecimal receitaTotal` | Similar a `PeriodoEvento.java` (record com validações) |
| 6.2.2 | `RelatorioReceita.java` | VO como record: `BigDecimal total`, `int quantidadeVendas`, `LocalDateTime periodoInicio`, `LocalDateTime periodoFim` | Igual padrão de `DadosMetodoPagamento` |
| 6.2.3 | `FiltroDashboard.java` | VO como record: `LocalDateTime dataInicio`, `LocalDateTime dataFim`, `UUID eventoId`, `int pagina`, `int tamanho` | Similar a `DadosCadastroEvento` com validações `@NotNull`, `@Future` |
| 6.2.4 | `DashboardRepository.java` | Interface de domínio com métodos: `List<MetricaVendas> buscarVendasPorEvento(FiltroDashboard filtro)`, `RelatorioReceita calcularReceitaTotal(FiltroDashboard filtro)` | Seguir padrão de `IngressoRepository` (estende `JpaRepository`) |

**Exemplo de código esperado para `MetricaVendas.java`:**
```java
package com.ticketscale.domain.dashboard;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record MetricaVendas(
    UUID eventoId,
    String eventoNome,
    long ingressosVendidos,
    BigDecimal receitaTotal
) {
    public MetricaVendas {
        if (eventoId == null) {
            throw new IllegalArgumentException("Evento ID é obrigatório.");
        }
        if (eventoNome == null || eventoNome.isBlank()) {
            throw new IllegalArgumentException("Nome do evento é obrigatório.");
        }
        if (ingressosVendidos < 0) {
            throw new IllegalArgumentException("Quantidade vendida não pode ser negativa.");
        }
        if (receitaTotal == null || receitaTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Receita não pode ser nula ou negativa.");
        }
    }
}
```

---

### 6.3 — Camada de Aplicação (Application)

**Pasta:** `src/main/java/com/ticketscale/application/usecase/`

| ID | Arquivo | Descrição | Padrão a Seguir |
|----|---------|-----------|-----------------|
| 6.3.1 | `GerarRelatorioVendasPorEvento.java` | Caso de uso que usa `DashboardRepository` para buscar métricas agrupadas por evento | Igual `ReservarIngressoUseCase`: `@Service`, injeção via construtor, método `executar()` |
| 6.3.2 | `CalcularReceitaTotal.java` | Caso de uso que calcula receita total no período | Mesmo padrão, método `executar(FiltroDashboard filtro)` |
| 6.3.3 | `ObterMetricasDashboard.java` | Caso de uso que agrega múltiplas métricas em um único DTO | Mesmo padrão |
| 6.3.4 | `ListarVendasPorPeriodo.java` | Caso de uso que lista vendas evoluindo no tempo (diário/semanal) | Mesmo padrão |

**Exemplo de código esperado para `GerarRelatorioVendasPorEvento.java`:**
```java
package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricaVendas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GerarRelatorioVendasPorEvento {

    private final DashboardRepository dashboardRepository;

    public GerarRelatorioVendasPorEvento(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public List<MetricaVendas> executar(FiltroDashboard filtro) {
        return dashboardRepository.buscarVendasPorEvento(filtro);
    }
}
```

---

### 6.4 — Camada de Infraestrutura (Infrastructure)

**Pasta:** `src/main/java/com/ticketscale/infrastructure/repository/`

| ID | Arquivo | Descrição | Detalhes de Implementação |
|----|---------|-----------|---------------------------|
| 6.4.1 | `DashboardRepositoryImpl.java` | Implementação do repositório com consultas JPQL | Usar `@PersistenceContext EntityManager` para queries dinâmicas |
| 6.4.2 | Consultas otimizadas | Queries com `SUM()`, `COUNT()`, `GROUP BY` | Ex: `SELECT p.evento.id, SUM(p.valor) FROM Pagamento p WHERE p.status = APROVADO GROUP BY p.evento.id` |
| 6.4.3 | Cache Redis (opcional) | Se implementar, usar `RedisTemplate` | Similar ao `RedisLockManager`, mas com `@Cacheable` do Spring |
| 6.4.4 | Invalidação de cache | Publicar evento após venda | Usar `ApplicationEventPublisher` do Spring |

**Exemplo de código esperado para `DashboardRepositoryImpl.java`:**
```java
package com.ticketscale.infrastructure.repository;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricaVendas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MetricaVendas> buscarVendasPorEvento(FiltroDashboard filtro) {
        String jpql = """
            SELECT new com.ticketscale.domain.dashboard.MetricaVendas(
                e.id, e.nome, COUNT(i), SUM(p.valor)
            )
            FROM Ingresso i
            JOIN i.lote l
            JOIN l.evento e
            LEFT JOIN Pagamento p ON p.ingresso = i AND p.status = 'APROVADO'
            WHERE i.status = 'VENDIDO'
              AND (:dataInicio IS NULL OR p.dataCriacao >= :dataInicio)
              AND (:dataFim IS NULL OR p.dataCriacao <= :dataFim)
              AND (:eventoId IS NULL OR e.id = :eventoId)
            GROUP BY e.id, e.nome
        """;
        
        TypedQuery<MetricaVendas> query = entityManager.createQuery(jpql, MetricaVendas.class);
        query.setParameter("dataInicio", filtro.dataInicio());
        query.setParameter("dataFim", filtro.dataFim());
        query.setParameter("eventoId", filtro.eventoId());
        
        return query.getResultList();
    }
}
```

---

### 6.5 — Camada de Interfaces (API REST)

**Pasta:** `src/main/java/com/ticketscale/interfaces/rest/dashboard/`

| ID | Arquivo | Descrição | Padrão a Seguir |
|----|---------|-----------|-----------------|
| 6.5.1 | `DashboardController.java` | Controller REST com endpoints do dashboard | Igual `EventoController`: `@RestController`, injeção via construtor |
| 6.5.2 | `DadosMetricaVendas.java` | DTO como record para resposta de vendas por evento | Similar a `DadosDetalhamentoEvento` |
| 6.5.3 | `DadosRelatorioReceita.java` | DTO como record para resposta de receita | Mesmo padrão |
| 6.5.4 | `DadosFiltroDashboard.java` | DTO como record para filtro no request | Similar a `DadosCadastroEvento` com validações |

**Endpoints a implementar:**

```java
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final GerarRelatorioVendasPorEvento gerarRelatorioVendasPorEvento;
    private final CalcularReceitaTotal calcularReceitaTotal;
    private final ObterMetricasDashboard obterMetricasDashboard;

    // Construtor

    @GetMapping("/vendas-por-evento")
    public ResponseEntity<List<DadosMetricaVendas>> vendasPorEvento(
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(required = false) UUID eventoId) {
        
        var filtro = new FiltroDashboard(dataInicio, dataFim, eventoId, 0, 100);
        var resultado = gerarRelatorioVendasPorEvento.executar(filtro);
        // Mapear para DTO
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/receita-total")
    public ResponseEntity<DadosRelatorioReceita> receitaTotal(
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim) {
        
        var filtro = new FiltroDashboard(dataInicio, dataFim, null, 0, 100);
        var resultado = calcularReceitaTotal.executar(filtro);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/metricas")
    public ResponseEntity<DadosMetricasDashboard> metricas(
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim) {
        
        var filtro = new FiltroDashboard(dataInicio, dataFim, null, 0, 100);
        var resultado = obterMetricasDashboard.executar(filtro);
        return ResponseEntity.ok(resultado);
    }
}
```

**Regras de segurança no `SecurityConfigurations.java`:**
```java
// Adicionar na linha de authorizeHttpRequests:
req.requestMatchers(HttpMethod.GET, "/dashboard/**").hasRole("ADMIN");
```

---

### 6.6 — Testes

**Pasta:** `src/test/java/com/ticketscale/`

| ID | Arquivo | Descrição | Padrão a Seguir |
|----|---------|-----------|-----------------|
| 6.6.1 | `GerarRelatorioVendasPorEventoTest.java` | Testes unitários do caso de uso | Usar `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` |
| 6.6.2 | `DashboardControllerTest.java` | Testes de integração do controller | Usar `@SpringBootTest`, `@AutoConfigureMockMvc`, `TestRestTemplate` |
| 6.6.3 | `DashboardRepositoryTest.java` | Testes de integração do repositório | Usar `@DataJpaTest`, banco H2 em memória |
| 6.6.4 | Testes de performance | Medir tempo de resposta das queries | Usar `@Timed` do Micrometer ou JUnit benchmarks |

**Exemplo de teste unitário:**
```java
@ExtendWith(MockitoExtension.class)
class GerarRelatorioVendasPorEventoTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private GerarRelatorioVendasPorEvento useCase;

    @Test
    void deveGerarRelatorioVendasPorEvento() {
        // Given
        var filtro = new FiltroDashboard(null, null, null, 0, 100);
        var metricas = List.of(new MetricaVendas(UUID.randomUUID(), "Evento A", 100L, BigDecimal.valueOf(1000)));
        when(dashboardRepository.buscarVendasPorEvento(filtro)).thenReturn(metricas);

        // When
        var resultado = useCase.executar(filtro);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).eventoNome()).isEqualTo("Evento A");
    }
}
```

---

### 6.7 — Documentação e Observabilidade

| ID | Tarefa | Detalhes |
|----|--------|----------|
| 6.7.1 | Documentar endpoints | Adicionar seção no `README.md` ou criar `docs/api_dashboard.md` |
| 6.7.2 | Logs estruturados | Usar MDC com correlation ID (já implementado no `LoggingFilter.java`) |
| 6.7.3 | Métricas Actuator | Adicionar `@Timed` nos use cases, expor em `/actuator/prometheus` |
| 6.7.4 | CHANGELOG | Adicionar entrada em `CHANGELOG.md` |

**Exemplo de documentação para README:**

```markdown
### Dashboard Administrativo

Endpoints para relatórios e métricas de vendas (acesso restrito a ADMIN):

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/dashboard/vendas-por-evento` | Lista vendas agrupadas por evento |
| GET | `/dashboard/receita-total` | Retorna receita total no período |
| GET | `/dashboard/metricas` | Retorna métricas consolidadas do dashboard |

**Query Params:**
- `dataInicio`: Data inicial do filtro (ISO 8601)
- `dataFim`: Data final do filtro (ISO 8601)
- `eventoId`: Filtrar por evento específico (UUID)
```

---

## 🏗️ Estrutura de Arquivos Final

```
src/main/java/com/ticketscale/
├── domain/
│   └── dashboard/
│       ├── MetricaVendas.java (record VO)
│       ├── RelatorioReceita.java (record VO)
│       ├── FiltroDashboard.java (record VO)
│       └── DashboardRepository.java (interface)
│
├── application/
│   └── usecase/
│       ├── GerarRelatorioVendasPorEvento.java
│       ├── CalcularReceitaTotal.java
│       └── ObterMetricasDashboard.java
│
├── infrastructure/
│   └── repository/
│       └── DashboardRepositoryImpl.java
│
└── interfaces/
    └── rest/
        └── dashboard/
            ├── DashboardController.java
            ├── DadosMetricaVendas.java
            ├── DadosRelatorioReceita.java
            └── DadosMetricasDashboard.java
```

**Testes:**
```
src/test/java/com/ticketscale/
├── application/usecase/
│   └── GerarRelatorioVendasPorEventoTest.java
├── infrastructure/repository/
│   └── DashboardRepositoryTest.java
└── interfaces/rest/dashboard/
    └── DashboardControllerTest.java
```

---

## 🔐 Segurança

### Configuração Atual (`SecurityConfigurations.java`)
- JWT com filtro `SecurityFilter`
- Roles: `ADMIN`, `USUARIO`
- Endpoints públicos: `POST /login`, `POST /usuarios`

### Alteração Necessária
Adicionar no método `securityFilterChain`:
```java
req.requestMatchers(HttpMethod.GET, "/dashboard/**").hasRole("ADMIN");
```

---

## ⚡ Performance

### Situação Atual
- Redis usado **apenas para lock distribuído**
- **Sem cache de dados** implementado

### Estratégia Recomendada

#### Opção 1: Cache Simples com Redis (Recomendada para Fase 6)
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

Uso nos use cases:
```java
@Cacheable(value = "dashboard-metricas", key = "#filtro")
public List<MetricaVendas> executar(FiltroDashboard filtro) {
    return dashboardRepository.buscarVendasPorEvento(filtro);
}
```

#### Opção 2: Invalidação Manual
Publicar evento após venda para invalidar cache:
```java
@Autowired
private ApplicationEventPublisher eventPublisher;

@Transactional
public void confirmarVenda() {
    // ... lógica de venda
    eventPublisher.publishEvent(new CacheInvalidationEvent("dashboard-metricas"));
}
```

---

## 📊 Métricas do Dashboard

### Métricas Prioritárias (Fase 6)

| Métrica | Entidades Envolvidas | Query SQL Aproximada |
|---------|---------------------|---------------------|
| **Receita Total** | `Pagamento`, `Ingresso` | `SELECT SUM(valor) FROM Pagamento WHERE status = 'APROVADO'` |
| **Ingressos Vendidos** | `Ingresso` | `SELECT COUNT(*) FROM Ingresso WHERE status = 'VENDIDO'` |
| **Vendas por Evento** | `Ingresso`, `Lote`, `Evento`, `Pagamento` | `SELECT evento, COUNT(ingresso), SUM(valor) GROUP BY evento` |
| **Vendas por Período** | `Pagamento` | `SELECT DATE(data_criacao), SUM(valor) GROUP BY DATE(data_criacao)` |
| **Taxa de Conversão** | `Reserva`, `Pagamento` | `COUNT(Pagamentos APROVADOS) / COUNT(Reservas CONFIRMADAS)` |

---

## ✅ Critérios de Aceite

- [ ] **Domain:**
  - [ ] `MetricaVendas.java` criado como record com validações
  - [ ] `FiltroDashboard.java` criado como record com validações
  - [ ] `DashboardRepository.java` interface criada
- [ ] **Application:**
  - [ ] `GerarRelatorioVendasPorEvento.java` implementado
  - [ ] `CalcularReceitaTotal.java` implementado
  - [ ] Testes unitários com cobertura ≥ 80%
- [ ] **Infrastructure:**
  - [ ] `DashboardRepositoryImpl.java` com queries JPQL otimizadas
  - [ ] Queries com agregações (SUM, COUNT, GROUP BY) funcionando
- [ ] **Interfaces:**
  - [ ] `DashboardController.java` com todos os endpoints
  - [ ] DTOs de resposta como records
  - [ ] Segurança configurada (apenas ADMIN)
- [ ] **Testes:**
  - [ ] Testes unitários dos use cases
  - [ ] Testes de integração dos endpoints
  - [ ] Testes de integração do repositório
- [ ] **Documentação:**
  - [ ] Endpoints documentados no README ou docs/
  - [ ] CHANGELOG.md atualizado

---

## 🔄 Dependências

### Pré-requisitos
- ✅ Fase 1 (Scaffolding) — Implementada
- ✅ Fase 2 (Autenticação JWT) — Implementada
- ✅ Fase 3 (CRUD de Eventos) — Implementada
- ✅ Fase 4 (Sistema de Reserva com Redis) — Implementada
- ✅ Fase 5 (RabbitMQ + Workers) — Implementada

### Entidades Dependentes
- `Reserva` — Para taxa de conversão
- `Ingresso` — Para quantidade vendida
- `Pagamento` — Para receita
- `Evento` — Para agrupamento
- `Lote` — Para relacionamento

---

## 📝 Histórico de Revisão

| Data | Versão | Descrição | Autor |
|------|--------|-----------|-------|
| 2026-03-26 | 1.0 | Criação do plano de implementação | — |
| 2026-03-26 | 1.1 | Adicionado detalhamento baseado nos padrões existentes | — |

---

## 🔗 Referências de Código Existente

| Padrão | Arquivo de Referência |
|--------|----------------------|
| Entidade com Builder | `domain/reserva/Reserva.java` |
| Repository Interface | `domain/reserva/IngressoRepository.java` |
| Use Case | `application/usecase/ReservarIngressoUseCase.java` |
| Controller | `interfaces/rest/evento/EventoController.java` |
| DTO Record | `interfaces/rest/evento/DadosCadastroEvento.java` |
| VO Record | `domain/evento/PeriodoEvento.java` |
| Security Config | `infrastructure/security/SecurityConfigurations.java` |
| Redis Lock | `infrastructure/redis/RedisLockManager.java` |
