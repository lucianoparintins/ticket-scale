# 📋 Plano de Implementação - Pendências do Projeto

**Data de Criação:** 29 de março de 2026  
**Última Atualização:** 29 de março de 2026  
**Status:** Planejamento

---

## 🎯 Objetivo

Este documento detalha o plano de implementação das funcionalidades e melhorias identificadas como pendentes ou parcialmente implementadas na análise do README.md do projeto TicketScale.

---

## 🔑 Decisões Arquiteturais

Este plano inclui as seguintes decisões técnicas já validadas:

| # | Decisão | Opção Selecionada |
|---|---------|-------------------|
| 1 | **Instâncias Nginx** | Auto-scaling dinâmico com Docker Swarm |
| 2 | **Política de Cache** | Cache-aside (Lazy Loading) |
| 3 | **Tecnologia UI** | SPA Estática Embutida (React + Vite) |
| 4 | **Tool Performance** | Gatling (DSL Scala) |
| 5 | **Abrangência Contratos** | Mínimo (autenticação + reserva) |
| 6 | **Circuit Breaker** | Apenas Retry (sem Resilience4j) |
| 7 | **Canal de Alertas** | E-mail via SMTP |
| 8 | **Ordem Roadmap** | Manter (Fundação → Observabilidade → Frontend → Qualidade) |

---

## 📊 Resumo das Pendências

| # | Pendência | Prioridade | Esforço Estimado | Status |
|---|-----------|------------|------------------|--------|
| 1 | Nginx Load Balancer + Auto-scaling | Alta | Médio-Alto | Pendente |
| 2 | Cache de Leitura Redis | Alta | Médio | Pendente |
| 3 | Dashboard Administrativo (UI) | Média | Alto | Pendente |
| 4 | Testes de Performance (Gatling) | Média | Médio | Pendente |
| 5 | Testes de Contrato (Mínimo) | Baixa | Médio | Pendente |
| 6 | Retry Automático | Média | Baixo | Pendente |
| 7 | Métricas e Alertas (Grafana + E-mail) | Baixa | Médio | Pendente |

**Total Estimado:** ~100-128 horas

---

## 1️⃣ Nginx Load Balancer

### Contexto
O README menciona Nginx como componente de load balancing na arquitetura, mas não há configuração no docker-compose.yml.

### Justificativa
- Permitir escalabilidade horizontal da API
- Distribuir requisições entre múltiplas instâncias
- Ponto único de entrada para SSL/TLS termination
- Rate limiting e proteção básica

### Escopo

#### 1.1 Configuração Docker
- [ ] Adicionar serviço `nginx` no `docker-compose.yml`
- [ ] Configurar rede interna entre nginx e aplicações
- [ ] Definir health checks para as instâncias backend
- [ ] Configurar auto-scaling dinâmico com Docker Swarm

#### 1.2 Configuração do Nginx
- [ ] Criar `nginx/nginx.conf` com:
  - Load balancing round-robin
  - Proxy reverso para `/api/**`
  - Static file serving para `/admin/**`
  - Rate limiting básico
  - Timeout configurations
- [ ] Criar `nginx/upstream.conf` com configuração para auto-scaling dinâmico
- [ ] Configurar logs de acesso e erro

#### 1.3 Perfis de Ambiente
- [ ] Perfil `dev`: single instance sem nginx (acesso direto à API)
- [ ] Perfil `prod`: Docker Swarm com auto-scaling + nginx

#### 1.4 Auto-Scaling (Docker Swarm)
- [ ] Configurar `docker-compose.prod.yml` para Swarm mode
- [ ] Definir políticas de scaling:
  - Mínimo: 2 réplicas
  - Máximo: 10 réplicas
  - Trigger: CPU > 70% ou Memory > 80%

### Arquivos a Criar/Modificar

```
docker-compose.yml          (modificar)
nginx/
├── nginx.conf             (criar)
├── upstream.conf          (criar)
└── conf.d/
    └── default.conf       (criar)
```

### Critérios de Aceite

- [ ] Nginx inicia junto com os demais serviços via `docker compose up`
- [ ] Requisições são distribuídas entre instâncias (quando múltiplas)
- [ ] Health check do nginx retorna status correto
- [ ] Logs de acesso e erro são gravados corretamente
- [ ] Auto-scaling funciona com triggers de CPU/Memory

### Estimativa
- **Tempo:** 8-12 horas (com Docker Swarm)
- **Complexidade:** Média-Alta

---

## 2️⃣ Cache de Leitura com Redis

### Contexto
O lock distribuído está implementado, mas o cache de leitura para consultas frequentes não foi evidenciado.

### Justificativa
- Reduzir latência em consultas frequentes
- Diminuir carga no banco de dados
- Melhorar experiência do usuário em listagens

### Escopo

#### 2.1 Estratégia de Cache
- [ ] Adotar política **Cache-aside (Lazy Loading)**:
  1. Aplicação verifica cache primeiro
  2. Se miss, busca no banco de dados
  3. Popula cache com TTL definido
  4. Retorna dado ao cliente
- [ ] Identificar entidades candidatas:
  - Eventos (listagem e detalhes)
  - Lotes de ingressos
  - Métricas de dashboard

#### 2.2 Implementação
- [ ] Criar `CacheManager` interface na camada de aplicação
- [ ] Implementar `RedisCacheManager` na infraestrutura
- [ ] Adicionar anotações `@Cacheable`, `@CacheEvict` nos services
- [ ] Configurar `CacheConfig` no Spring

#### 2.3 Chaves e Estrutura
- [ ] Definir padrão de chaves: `ticketscale:{entidade}:{id}`
- [ ] Definir TTLs por tipo de entidade:
  - Eventos: 5 minutos
  - Lotes: 2 minutos
  - Dashboard: 10 minutos

#### 2.4 Invalidação
- [ ] Invalidar cache de eventos ao criar/editar/desativar
- [ ] Invalidar cache de lotes ao alterar quantidade/preço
- [ ] Publicar evento de invalidação via RabbitMQ (cache distribuído)

### Arquivos a Criar/Modificar

```
src/main/java/com/ticketscale/
├── application/
│   ├── port/out/CacheManager.java           (criar)
│   └── usecase/
│       └── ... (adicionar @Cacheable)
├── infrastructure/
│   └── redis/
│       ├── RedisCacheManager.java           (criar)
│       └── CacheConfig.java                 (criar)
└── domain/evento/Evento.java                (modificar)
```

### Critérios de Aceite

- [ ] Consultas de eventos retornam do cache (segunda chamada)
- [ ] Cache é invalidado após criação/edição de evento
- [ ] TTL configurado funciona corretamente
- [ ] Testes validam comportamento do cache
- [ ] Métricas de hit/miss rate disponíveis

### Estimativa
- **Tempo:** 8-12 horas
- **Complexidade:** Média

---

## 3️⃣ Dashboard Administrativo (UI)

### Contexto
As APIs do dashboard estão implementadas (`DashboardController`, casos de uso), mas não há interface gráfica para consumo.

### Justificativa
- Permitir que gestores visualizem métricas sem consumir APIs diretamente
- Melhorar usabilidade do sistema
- Demonstração completa do produto

### Escopo

#### 3.1 Decisão de Tecnologia

**Opção Selecionada: C - SPA Estática Embutida**

- [ ] Build de React + TypeScript no `src/main/resources/static/admin`
- [ ] Spring Boot serve arquivos estáticos
- [ ] Vite como build tool
- [ ] Bootstrap + Material Design para UI
- [ ] Deploy único junto com a API
- [ ] API de autenticação separada (`/api/**`)

**Vantagens desta abordagem:**
- Deploy simplificado (apenas um artifact)
- Sem necessidade de repositório separado
- CDN-ready no futuro se houver necessidade de escala global
- Menor complexidade operacional

#### 3.2 Páginas a Implementar

| Página | Funcionalidades | Prioridade |
|--------|----------------|------------|
| Login | Autenticação JWT | Alta |
| Dashboard | Visão geral com cards de métricas | Alta |
| Eventos | Listar, criar, editar, desativar | Alta |
| Vendas | Gráfico de vendas por período | Média |
| Ingressos | Gestão de lotes e preços | Média |
| Relatórios | Exportar dados (CSV/PDF) | Baixa |

#### 3.3 Componentes Visuais
- [ ] Cards de métricas (receita, vendas, conversão)
- [ ] Tabela de eventos com paginação
- [ ] Formulário de cadastro de eventos
- [ ] Gráficos (Chart.js ou Recharts)
- [ ] Toasts para notificações

### Arquivos a Criar

```
src/main/resources/static/admin/
├── index.html
├── vite.config.ts
├── package.json
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── components/
│   │   ├── Dashboard/
│   │   ├── Eventos/
│   │   ├── Vendas/
│   │   └── common/
│   ├── services/
│   │   └── api.ts
│   ├── hooks/
│   └── styles/
└── public/
    └── manifest.json
```

### Critérios de Aceite

- [ ] Autenticação JWT integrada
- [ ] Dashboard exibe métricas em tempo real
- [ ] CRUD de eventos funcional via UI
- [ ] Layout responsivo (mobile-friendly)
- [ ] Tratamento de erros e loading states

### Estimativa
- **Tempo:** 40-60 horas
- **Complexidade:** Alta

---

## 4️⃣ Testes de Performance

### Contexto
Listado como pendente no README. Necessário validar escalabilidade e identificar gargalos.

### Justificativa
- Validar capacidade de concorrência do sistema
- Identificar gargalos antes de produção
- Estabelecer baseline de performance
- Validar eficácia do lock distribuído

### Escopo

#### 4.1 Decisão de Ferramenta

**Ferramenta Selecionada: Gatling**

- [ ] DSL em Scala para definição de cenários
- [ ] Relatórios HTML detalhados
- [ ] Integração nativa com Gradle
- [ ] Simulação de usuários concorrentes
- [ ] Gráficos de response time, throughput e erros

**Justificativa:**
- Melhor integração com build Gradle
- Relatórios mais modernos e informativos
- DSL expressiva para cenários complexos
- Versionamento junto com o código

#### 4.2 Cenários de Teste

| Cenário | Descrição | Carga | Duração |
|---------|-----------|-------|---------|
| Listagem de Eventos | GET /api/eventos | 100 req/s | 5 min |
| Reserva de Ingresso | POST /api/reservas | 50 req/s | 10 min |
| Checkout Completo | Reserva + Pagamento | 20 req/s | 10 min |
| Autenticação | POST /api/auth/login | 100 req/s | 5 min |
| Stress Test | Pico de acesso | 500 req/s | 2 min |

#### 4.3 Métricas a Coletar
- [ ] Response time (p50, p90, p99)
- [ ] Throughput (req/s)
- [ ] Error rate (%)
- [ ] Concurrent users
- [ ] CPU/Memory usage
- [ ] Redis connections
- [ ] Database connections

#### 4.4 Integração CI/CD
- [ ] Adicionar tarefa Gradle para Gatling
- [ ] Executar testes de performance manualmente (não no CI devido ao tempo)
- [ ] Publicar relatórios como artifact do GitHub Actions

### Arquivos a Criar

```
src/test/gatling/
├── simulations/
│   ├── EventoSimulation.scala
│   ├── ReservaSimulation.scala
│   └── CheckoutSimulation.scala
├── resources/
│   └── bodies/
└── build.gradle (adicionar plugin gatling)
```

### Critérios de Aceite

- [ ] Todos os cenários de teste implementados
- [ ] Relatórios gerados em HTML
- [ ] Metas de performance definidas:
  - p99 < 500ms para consultas
  - p99 < 2s para reservas
  - Error rate < 0.1%
- [ ] Documentação de como executar testes

### Estimativa
- **Tempo:** 12-16 horas
- **Complexidade:** Média

---

## 5️⃣ Testes de Contrato (Spring Cloud Contract)

### Contexto
Listado como pendente no README. Importante para validar integração entre serviços.

### Justificativa
- Garantir compatibilidade entre API e consumidores
- Prevenir breaking changes
- Documentação viva dos contratos

### Escopo

#### 5.1 Contratos a Definir

**Escopo Selecionado: Mínimo (Endpoints Críticos)**

| Contrato | Provider | Consumer | Prioridade |
|----------|----------|----------|------------|
| Autenticação (login) | TicketScale | Frontend/Admin | Alta |
| Reserva de Ingresso | TicketScale | Frontend/Mobile | Alta |

**Justificativa:**
- Focar nos endpoints de maior risco de breaking changes
- Reduzir esforço inicial de implementação
- Validar abordagem antes de expandir

#### 5.2 Implementação
- [ ] Adicionar `spring-cloud-contract` no build.gradle
- [ ] Definir contratos em Groovy DSL
- [ ] Gerar stubs para consumidores
- [ ] Testes de producer (verificar contrato)
- [ ] Testes de consumer (usar stubs)

### Arquivos a Criar

```
src/test/resources/contracts/
├── autenticacao/
│   └── login.groovy
└── reservas/
    └── criarReserva.groovy
```

### Critérios de Aceite

- [ ] Contratos definidos para endpoints principais
- [ ] Stubs gerados e publicados
- [ ] Testes de contrato passam no CI
- [ ] Documentação de como usar stubs

### Estimativa
- **Tempo:** 8-10 horas
- **Complexidade:** Média

---

## 6️⃣ Retry Automático

### Contexto
Mencionado em requisitos não funcionais, mas sem implementação evidenciada.

### Justificativa
- Aumentar resiliência do sistema
- Lidar com falhas transitórias
- Melhorar experiência do usuário

### Escopo

#### 6.1 Cenários para Retry
- [ ] Chamadas a gateways de pagamento
- [ ] Publicação de eventos no RabbitMQ
- [ ] Aquisição de lock no Redis
- [ ] Chamadas HTTP externas (se houver)

#### 6.2 Configuração Spring Retry
- [ ] Adicionar `spring-retry` dependency
- [ ] Configurar `@EnableRetry`
- [ ] Definir políticas de retry:
  - Max attempts: 3
  - Backoff: exponencial (1s, 2s, 4s)
  - Retryable exceptions específicas

#### 6.3 Circuit Breaker

**Decisão: Não implementar nesta fase**

Apenas retry será implementado. Circuit breaker com Resilience4j poderá ser adicionado em fase futura se necessário.

**Justificativa:**
- Retry cobre 80% dos casos de falhas transitórias
- Reduz complexidade inicial da implementação
- Pode ser adicionado incrementalmente se necessário

### Arquivos a Criar/Modificar

```
build.gradle                              (modificar - adicionar spring-retry)
src/main/java/com/ticketscale/
├── infrastructure/config/RetryConfig.java (criar)
├── infrastructure/pagamento/
│   └── ... (adicionar @Retryable)
└── infrastructure/messaging/
    └── RabbitMQEventPublisher.java       (adicionar @Retryable)
```

### Critérios de Aceite

- [ ] Retry configurado para operações elegíveis
- [ ] Logs registram tentativas de retry
- [ ] Circuit breaker abre após falhas consecutivas
- [ ] Fallback executa quando retry esgota

### Estimativa
- **Tempo:** 4-6 horas
- **Complexidade:** Baixa

---

## 7️⃣ Métricas e Alertas

### Contexto
Actuator e Prometheus estão configurados, mas não há dashboards ou alertas definidos.

### Justificativa
- Monitorar saúde do sistema em produção
- Detectar problemas proativamente
- Tomar decisões baseadas em dados

### Escopo

#### 7.1 Dashboard Prometheus/Grafana
- [ ] Criar docker-compose com Grafana
- [ ] Configurar datasource Prometheus
- [ ] Criar dashboards:
  - Visão geral da aplicação
  - Métricas de JVM
  - Métricas de negócio (vendas, reservas)
  - Métricas de infraestrutura (Redis, RabbitMQ, PostgreSQL)

#### 7.2 Alertas

**Canal de Notificação Selecionado: E-mail**

- [ ] Configurar Alertmanager com receiver de e-mail
- [ ] Definir alertas:
  - Error rate > 1%
  - Response time p99 > 1s
  - CPU > 80%
  - Memory > 85%
  - Redis connection pool esgotado
  - RabbitMQ queue depth > 1000
- [ ] Configurar SMTP para envio de e-mails
- [ ] Definir lista de destinatários (equipe de operações)

**Justificativa:**
- Simples de configurar e manter
- Não requer integração com serviços externos
- Adequado para fase inicial do projeto

#### 7.3 Métricas Customizadas
- [ ] Adicionar contador de reservas criadas
- [ ] Adicionar contador de pagamentos aprovados/recusados
- [ ] Adicionar histograma de tempo de reserva
- [ ] Adicionar gauge de ingressos disponíveis

### Arquivos a Criar

```
docker-compose.yml                    (adicionar grafana, alertmanager)
grafana/
├── provisioning/
│   ├── datasources/
│   │   └── prometheus.yml
│   └── dashboards/
│       └── dashboard.yml
└── dashboards/
    ├── ticketscale-overview.json
    └── ticketscale-business.json
prometheus/
└── prometheus.yml
alertmanager/
└── alertmanager.yml
```

### Critérios de Aceite

- [ ] Grafana acessível via browser
- [ ] Dashboards exibem métricas em tempo real
- [ ] Alertas disparam corretamente
- [ ] Métricas customizadas disponíveis
- [ ] Documentação de como acessar

### Estimativa
- **Tempo:** 10-14 horas
- **Complexidade:** Média

---

## 📅 Roadmap Sugerido

### Fase 1 - Fundação (Sprint 1-2)
1. Nginx Load Balancer
2. Retry Automático
3. Cache de Leitura Redis

### Fase 2 - Observabilidade (Sprint 3)
4. Métricas e Alertas (Grafana)
5. Testes de Performance (baseline)

### Fase 3 - Frontend (Sprint 4-6)
6. Dashboard Administrativo (UI)

### Fase 4 - Qualidade (Sprint 7)
7. Testes de Contrato

---

## 📊 Matriz de Riscos

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Cache inconsistente | Média | Alto | Invalidação adequada, TTL curto |
| Nginx como SPOF | Baixa | Alto | Health checks, múltiplas instâncias |
| UI desatualizada | Média | Baixo | Manter APIs como fonte da verdade |
| Testes de performance lentos | Alta | Médio | Executar sob demanda, não no CI |

---

## 📝 Definição de Pronto (DoD)

Para cada item deste plano ser considerado completo:

- [ ] Código implementado e revisado
- [ ] Testes unitários escritos (cobertura > 80%)
- [ ] Testes de integração (quando aplicável)
- [ ] Documentação atualizada
- [ ] README.md atualizado (roadmap)
- [ ] CHANGELOG.md atualizado
- [ ] Deploy em ambiente de teste validado

---

## 📈 Carga Horária Total

| Fase | Itens | Horas Estimadas |
|------|-------|-----------------|
| Fase 1 - Fundação | Nginx, Retry, Cache | 20-28h |
| Fase 2 - Observabilidade | Grafana, Gatling | 22-30h |
| Fase 3 - Frontend | Dashboard UI | 40-60h |
| Fase 4 - Qualidade | Testes de Contrato | 8-10h |
| **Total** | | **~100-128h** |

---

## 🔗 Referências

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Nginx Load Balancing](https://docs.nginx.com/nginx/admin-guide/load-balancer/http-load-balancer/)
- [Gatling Documentation](https://gatling.io/docs/gatling/reference/current/)
- [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract)
- [Resilience4j](https://resilience4j.readme.io/)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)

---

## 📞 Contato

Para dúvidas sobre este plano, consulte a documentação do projeto ou abra uma issue no repositório.

---

## 📜 Histórico de Revisões

| Data | Versão | Autor | Descrição |
|------|--------|-------|-----------|
| 29/03/2026 | 1.0 | Equipe | Criação inicial do plano |
| 29/03/2026 | 1.1 | Equipe | Atualização com decisões arquiteturais validadas: <br>• Nginx com Docker Swarm auto-scaling <br>• Cache-aside policy <br>• SPA React embutida <br>• Gatling para performance <br>• Contratos mínimos <br>• Apenas Retry (sem circuit breaker) <br>• Alertas por e-mail |
