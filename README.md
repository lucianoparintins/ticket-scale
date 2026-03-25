# 🎟️ TicketScale

Plataforma SaaS para venda e gestão de ingressos online, projetada para suportar **alta concorrência**, evitando overselling e garantindo consistência e escalabilidade.

---

## 🚀 Visão Geral

O TicketScale permite:

- criação e gestão de eventos
- venda de ingressos com controle de concorrência
- reservas temporárias com expiração
- processamento assíncrono de eventos
- autenticação segura com JWT

Ideal para cenários como:

- shows e eventos
- conferências
- venda de ingressos em larga escala

---

## 🧱 Arquitetura

O sistema segue:

- Clean Architecture
- Domain-Driven Design (DDD)
- Arquitetura orientada a eventos (Event-Driven)

### 📐 Visão geral
```
Cliente
↓
Nginx (Load Balancer)
↓
Spring Boot API (múltiplas instâncias)
↓
├── Segurança (JWT)
├── Camada Application (Use Cases)
├── Domínio (Regras de negócio)
│
├── Redis (cache + lock distribuído)
├── PostgreSQL (persistência)
└── RabbitMQ (mensageria)
↓
Workers (processamento assíncrono)
```

---

## 🧰 Tecnologias

### Backend
- Java 25
- Spring Boot
- Spring Security (JWT)

### Banco de Dados
- PostgreSQL

### Cache e Concorrência
- Redis

### Mensageria
- RabbitMQ

### Infraestrutura
- Nginx (load balancer)

---

## 👥 Perfis de Usuário

### Cliente
- cadastro e login
- visualizar eventos
- comprar ingressos
- visualizar histórico

### Gestor (Admin)
- criar eventos
- gerenciar ingressos
- acompanhar vendas
- acessar relatórios

---

## 📋 Requisitos Funcionais

### Autenticação
- cadastro de usuários
- login com JWT
- controle de acesso por perfil

### Eventos
- criar evento
- editar evento
- listar eventos
- visualizar detalhes

### Ingressos
- criação de lotes
- definição de preço e quantidade
- consulta de disponibilidade

### Compra
- reserva temporária de ingresso
- confirmação de compra
- cancelamento
- histórico de pedidos

### Assíncrono
- envio de notificações
- expiração automática de reservas
- processamento de eventos

---

## ⚙️ Requisitos Não Funcionais

### Escalabilidade
- suporte a alto volume de acessos
- escalabilidade horizontal

### Performance
- uso de cache com Redis
- baixa latência em consultas

### Segurança
- autenticação via JWT
- proteção de dados sensíveis

### Consistência
- prevenção de overselling
- uso de lock distribuído

### Resiliência
- processamento assíncrono com filas
- retry automático

### Observabilidade
- logs estruturados
- métricas e monitoramento

---

## 🔥 Fluxo Crítico: Compra de Ingresso

1. Usuário autentica via JWT
2. Consulta eventos (cache Redis)
3. Solicita reserva
4. Sistema aplica lock no Redis
5. Valida disponibilidade
6. Cria reserva no banco
7. Publica evento no RabbitMQ
8. Worker processa (ex: confirmação)
9. Usuário finaliza pagamento

---

## 🧠 Decisões Arquiteturais

### Redis
- cache de leitura
- lock distribuído
- controle de reservas

### RabbitMQ
- desacoplamento
- resiliência
- processamento assíncrono

### JWT
- autenticação stateless
- escalabilidade horizontal

### PostgreSQL
- consistência transacional

---

## 📂 Estrutura do Projeto
```
src/main/java/com/ticketscale
├── domain
├── application
├── infrastructure
├── interfaces
```
---

## 🛠️ Roadmap (Próximos Passos)

- [x] Módulo de autenticação (JWT) e testes automatizados
- [ ] CRUD de eventos
- [ ] Sistema de reserva com Redis
- [ ] Integração com RabbitMQ
- [ ] Worker de processamento
- [ ] Dashboard administrativo

---

## 📌 Objetivo do Projeto

Este projeto serve como:

- base para sistemas escaláveis
- estudo de arquitetura moderna
- portfólio profissional
