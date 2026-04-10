# 📋 Plano de Implementação - Pendências do Projeto

**Data de Criação:** 29 de março de 2026
**Última Atualização:** 10 de abril de 2026
**Status:** Frontend MVP concluído — Faltam telas de Gestão (Usuários, Lotes, Reservas, Pagamentos) e melhorias no Dashboard

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
| 1 | Nginx Load Balancer + Auto-scaling | Alta | Médio-Alto | **Concluído ✅** |
| 2 | Cache de Leitura Redis | Alta | Médio | **Concluído ✅** |
| 3 | Dashboard Administrativo (UI — MVP) | Média | Alto | **Concluído ✅** |
| 3.1 | Tela de Gestão de Usuários | Média | Médio | Pendente |
| 3.2 | Tela de Gestão de Lotes por Evento | Alta | Médio | Pendente |
| 3.3 | Tela de Gestão de Reservas | Média | Médio | Pendente |
| 3.4 | Tela de Histórico de Pagamentos | Baixa | Médio | Pendente |
| 3.5 | Dashboard com Filtros e Gráficos | Média | Médio | Pendente |
| 4 | Testes de Performance (Gatling) | Média | Médio | Pendente |
| 5 | Testes de Contrato (Mínimo) | Baixa | Médio | Pendente |
| 6 | Retry Automático | Média | Baixo | Pendente |
| 7 | Métricas e Alertas (Grafana + E-mail) | Baixa | Médio | Pendente |

**Total Restante Estimado:** ~72-102 horas
**Total Concluído:** ~56-72 horas (Fases 1 e 3 — MVP)

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
- [x] Adicionar serviço `nginx` no `docker-compose.yml`
- [x] Configurar rede interna entre nginx e aplicações
- [x] Definir health checks para as instâncias backend
- [x] Configurar auto-scaling dinâmico com Docker Swarm

#### 1.2 Configuração do Nginx
- [x] Criar `nginx/nginx.conf` com:
  - Load balancing round-robin
  - Proxy reverso para `/api/**`
  - Static file serving para `/admin/**`
  - Rate limiting básico
  - Timeout configurations
- [x] Criar `nginx/upstream.conf` com configuração para auto-scaling dinâmico
- [x] Configurar logs de acesso e erro

#### 1.3 Perfis de Ambiente
- [x] Perfil `dev`: single instance com nginx (via docker-compose.yml)
- [x] Perfil `prod`: Docker Swarm com auto-scaling + nginx

#### 1.4 Auto-Scaling (Docker Swarm)
- [x] Configurar `docker-compose.prod.yml` para Swarm mode
- [x] Definir políticas de scaling:
  - Mínimo: 2 réplicas
  - Máximo: 10 réplicas
  - Trigger: CPU > 70% ou Memory > 80%

### Arquivos a Criar/Modificar

```
docker-compose.yml          (modificado)
docker-compose.prod.yml     (criado)
Dockerfile                  (criado)
scripts/auto-scale.sh       (criado)
nginx/
├── nginx.conf             (criado)
├── upstream.conf          (criado)
└── conf.d/
    └── default.conf       (criado)
```

### Critérios de Aceite

- [x] Nginx inicia junto com os demais serviços via `docker compose up`
- [x] Requisições são distribuídas entre instâncias (quando múltiplas)
- [x] Health check do nginx retorna status correto
- [x] Logs de acesso e erro são gravados corretamente
- [x] Auto-scaling funciona com triggers de CPU/Memory (via script auto-scale.sh)

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
- [x] Adotar política **Cache-aside (Lazy Loading)**:
  1. Aplicação verifica cache primeiro
  2. Se miss, busca no banco de dados
  3. Popula cache com TTL definido
  4. Retorna dado ao cliente
- [x] Identificar entidades candidatas:
  - Eventos (listagem e detalhes)
  - Lotes de ingressos (TTL 2min definido na config)
  - Métricas de dashboard

#### 2.2 Implementação
- [x] Criar `CacheManager` interface na camada de aplicação
- [x] Implementar `RedisCacheManager` na infraestrutura
- [x] Adicionar anotações `@Cacheable`, `@CacheEvict` nos services
- [x] Configurar `CacheConfig` no Spring

#### 2.3 Chaves e Estrutura
- [x] Definir padrão de chaves: `ticketscale:{entidade}:{id}`
- [x] Definir TTLs por tipo de entidade:
  - Eventos: 5 minutos
  - Lotes: 2 minutos
  - Dashboard: 10 minutos

#### 2.4 Invalidação
- [x] Invalidar cache de eventos ao criar/editar/desativar
- [x] Invalidar cache de lotes ao alterar quantidade/preço (LoteService implementado)
- [x] Publicar evento de invalidação via RabbitMQ (CacheInvalidadoEvent implementado)

### Arquivos a Criar/Modificar

```
build.gradle                                  (modificado)
src/main/java/com/ticketscale/
├── application/
│   ├── port/out/CacheManager.java           (criado)
│   ├── evento/EventoService.java            (modificado)
│   └── usecase/
│       ├── ObterMetricasDashboard.java      (modificado)
│       ├── GerarRelatorioVendasPorEvento.java (modificado)
│       └── CalcularReceitaTotal.java        (modificado)
├── infrastructure/
│   ├── config/CacheConfig.java              (criado)
│   └── redis/
│       └── RedisCacheManagerImpl.java       (criado)
```

### Critérios de Aceite

- [x] Consultas de eventos retornam do cache (segunda chamada)
- [x] Cache é invalidado após criação/edição de evento
- [x] TTL configurado funciona corretamente
- [x] Testes validam comportamento do cache (EventoCacheIntegrationTest)
- [x] Métricas de hit/miss rate disponíveis via Actuator

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

- [x] Build de React + TypeScript no `src/main/resources/static/admin`
- [x] Spring Boot serve arquivos estáticos
- [x] Vite como build tool
- [x] Vanilla CSS para UI (conforme GEMINI.md)
- [x] Deploy único junto com a API
- [x] API de autenticação separada (`/api/**`)

**Vantagens desta abordagem:**
- Deploy simplificado (apenas um artifact)
- Sem necessidade de repositório separado
- CDN-ready no futuro se houver necessidade de escala global
- Menor complexidade operacional

#### 3.2 Páginas Implementadas (MVP)

| Página | Funcionalidades | Status |
|--------|----------------|------------|
| Login | Autenticação JWT | **Concluído ✅** |
| Dashboard | Visão geral com cards de métricas | **Concluído ✅** |
| Eventos | Listar, criar, desativar | **Concluído ✅** |
| Vendas | Tabela de vendas por evento (agregado) | **Concluído ✅** |

#### 3.2.1 Páginas Pendentes (Gestão)

| Página | Rota | Funcionalidades | Endpoints Backend Necessários |
|--------|------|-----------------|-------------------------------|
| **Usuários** | `/admin/usuarios` | Listar, cadastrar, editar papel, desativar | `GET /api/usuarios`, `GET /api/usuarios/{id}`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}` |
| **Detalhe do Evento + Lotes** | `/admin/eventos/:id` | Ver detalhes, CRUD de lotes (criar, editar, desativar) | `PUT /api/eventos/{id}`, `POST /api/eventos/{eventoId}/lotes`, `GET /api/eventos/{eventoId}/lotes`, `DELETE /api/v1/lotes/{id}` |
| **Reservas** | `/admin/reservas` | Listar todas as reservas, filtrar por evento/status, ver detalhes | `GET /api/v1/reservas`, `GET /api/v1/reservas/{id}`, `PUT /api/v1/reservas/{id}` |
| **Pagamentos** | `/admin/pagamentos` | Histórico de pagamentos, status (aprovado/recusado), detalhes | `GET /api/v1/pagamentos`, `GET /api/v1/pagamentos/{id}` |

#### 3.2.2 Melhorias Pendentes nas Páginas Existentes

| Página | Melhoria | Detalhe |
|--------|----------|---------|
| Dashboard | Filtros por período | Backend já suporta `dataInicio`, `dataFim`, `eventoId` — frontend não envia |
| Dashboard | Gráficos temporais | Linha de receita ao longo do tempo, barras de vendas por dia |
| Eventos | Edição de evento | Adicionar formulário de edição (requer `PUT /api/eventos/{id}` no backend) |
| Vendas | Drill-down por reserva | Clicar em uma linha e ver reservas individuais daquele evento |
| Geral | Toasts/notificações | Adicionar biblioteca de notificações (ex: react-hot-toast) |
| Geral | Loading states | Spinners e skeletons em todas as páginas |

### Arquivos Criados

```
frontend/                  (código fonte)
src/main/resources/static/admin/ (build final)
src/main/java/com/ticketscale/interfaces/rest/FrontendForwardController.java (roteamento SPA)
```

### Critérios de Aceite

- [x] Autenticação JWT integrada
- [x] Dashboard exibe métricas em tempo real
- [x] CRUD de eventos funcional via UI
- [x] Layout responsivo (mobile-friendly)
- [x] Tratamento de erros e loading states

### Estimativa
- **Tempo:** 40-60 horas
- **Complexidade:** Alta
- **Status:** ✅ **Concluído**

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

### Fase 1 - Fundação ✅ **Concluída**
1. [x] Nginx Load Balancer
2. [x] Cache de Leitura Redis

### Fase 2 - Observabilidade ⏳ **Pendente**
3. [ ] Métricas e Alertas (Grafana + Alertmanager)
4. [ ] Testes de Performance (baseline Gatling)

### Fase 3 - Frontend ✅ **MVP Concluído**
5. [x] Dashboard UI — Login, Dashboard, Eventos, Vendas

### Fase 3.1 - Frontend (Gestão Completa) ⏳ **Pendente**
6. [ ] Tela de Gestão de Usuários (`/admin/usuarios`)
7. [ ] Tela de Detalhe do Evento + Lotes (`/admin/eventos/:id`)
8. [ ] Tela de Gestão de Reservas (`/admin/reservas`)
9. [ ] Tela de Histórico de Pagamentos (`/admin/pagamentos`)
10. [ ] Dashboard com Filtros e Gráficos

### Fase 4 - Qualidade ⏳ **Pendente**
11. [ ] Testes de Contrato (Spring Cloud Contract — escopo mínimo)

### Dependências: Backend Necessário para Telas de Gestão

| Recurso | Endpoints Faltantes no Backend |
|---------|-------------------------------|
| Evento | `PUT /api/eventos/{id}` (editar) |
| Lote | `POST /api/eventos/{eventoId}/lotes` (criar), `GET /api/eventos/{eventoId}/lotes` (listar por evento), `DELETE /api/v1/lotes/{id}` (desativar) |
| Reserva | `GET /api/v1/reservas` (listar), `GET /api/v1/reservas/{id}` (detalhar), `PUT /api/v1/reservas/{id}` (atualizar status) |
| Pagamento | `GET /api/v1/pagamentos` (listar), `GET /api/v1/pagamentos/{id}` (detalhar) |
| Usuário | `GET /api/usuarios` (listar), `GET /api/usuarios/{id}` (detalhar), `PUT /api/usuarios/{id}` (editar), `DELETE /api/usuarios/{id}` (desativar) |

---

## 📊 Matriz de Riscos

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Cache inconsistente | Média | Alto | Invalidação adequada, TTL curto |
| Nginx como SPOF | Baixa | Alto | Health checks, múltiplas instâncias |
| UI desatualizada | Média | Baixo | Manter APIs como fonte da verdade |
| Testes de performance lentos | Alta | Médio | Executar sob demanda, não no CI |
| Backend sem endpoints para telas | Alta | Alto | Implementar endpoints em paralelo com frontend (Fase 3.1) |
| Frontend sem testes E2E | Alta | Médio | Adicionar Playwright/Cypress na Fase 4 |
| Sem lib de gráficos no dashboard | Média | Baixo | Avaliar Recharts ou Chart.js (leve e open-source) |

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

| Fase | Itens | Horas Estimadas | Status |
|------|-------|-----------------|--------|
| Fase 1 - Fundação | Nginx, Cache | 20-28h | ✅ Concluída |
| Fase 2 - Observabilidade | Grafana, Gatling | 22-30h | ⏳ Pendente |
| Fase 3 - Frontend MVP | Dashboard UI (4 telas) | 40-60h | ✅ Concluída |
| Fase 3.1 - Frontend Gestão | Backend endpoints (14) + 5 telas + melhorias | 38-56h | ⏳ Pendente |
| Fase 4 - Qualidade | Testes de Contrato | 8-10h | ⏳ Pendente |
| **Total Concluído** | | **~56-72h** | |
| **Total Restante** | | **~72-102h** | |
| **Total Geral** | | **~134-180h** | |

#### Detalhamento Fase 3.1 (Frontend Gestão)

| Item | Backend Endpoints | Frontend Tela | Horas |
|------|-------------------|---------------|-------|
| Gestão de Usuários | 4 endpoints | 1 tela (CRUD) | 8-12h |
| Detalhe do Evento + Lotes | 4 endpoints | 1 tela (detalhe + CRUD lotes) | 12-16h |
| Gestão de Reservas | 3 endpoints | 1 tela (listagem + filtros) | 8-12h |
| Histórico de Pagamentos | 2 endpoints | 1 tela (listagem + detalhes) | 6-10h |
| Dashboard com Filtros/Gráficos | — (backend pronto) | Melhorias + lib gráficos | 4-6h |

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

| Data | Versão | Descrição |
|------|--------|-----------|
| 29/03/2026 | 1.0 | Criação inicial do plano |
| 29/03/2026 | 1.1 | Atualização com decisões arquiteturais validadas (Nginx + Docker Swarm, Cache-aside, SPA React, Gatling, Contratos mínimos, Apenas Retry, Alertas por e-mail) |
| 10/04/2026 | 1.2 | Fases 1 e 3 concluídas: Nginx + auto-scaling, Cache Redis, Dashboard UI (React + Vite). Roadmap e carga horária atualizados. |
| 10/04/2026 | 1.3 | GAP analysis do frontend: 4 telas novas identificadas (Usuários, Lotes, Reservas, Pagamentos), melhorias no Dashboard, 14 endpoints backend faltantes. Fase 3.1 criada. Carga horária recalculada (~134-180h total). |
