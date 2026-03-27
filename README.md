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
- Argon2id (`de.mkammerer:argon2-jvm`)

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
- cadastro de usuários com hashing Argon2id
- login com JWT e verificação de hash
- uso de PEPPER em todas as operações de senha
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
- hashing de senhas robusto com Argon2id
- proteção contra ataques de dicionário e rainbow tables via PEPPER
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

### Argon2id
- hashing de senhas moderno e resistente a ataques por hardware (GPUs/ASICs)
- configurado com PEPPER para camada extra de segurança

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

## 📊 Qualidade de Software

O projeto possui um conjunto de ferramentas para garantir qualidade do código:

| Ferramenta | Finalidade | Comando |
|------------|------------|---------|
| **JaCoCo** | Cobertura de testes | `./gradlew jacocoTestReport` |
| **Checkstyle** | Padronização de código | `./gradlew checkstyleMain checkstyleTest` |
| **PMD** | Detecção de bugs e más práticas | `./gradlew pmdMain pmdTest` |
| **OWASP Dependency Check** | Segurança de dependências | `./gradlew dependencyCheckAnalyze` |
| **SonarQube Local** | Análise contínua (self-hosted) | `./gradlew sonar` |
| **GitHub Actions** | CI/CD automatizado | Executado em cada push/PR |
| **Spring Actuator** | Health checks e métricas | `/actuator/health`, `/actuator/metrics` |
| **Micrometer Prometheus** | Export de métricas | `/actuator/prometheus` |
| **MDC Logging** | Logs estruturados com correlation ID | Automático em todas as requisições |

### Métricas de Qualidade

- **Cobertura mínima de testes:** 80% (camadas domain e application)
- **Padrão de código:** Checkstyle configurado com regras essenciais
- **CI/CD:** Pipeline automatizado com testes, cobertura e análise estática

### Relatórios

- **Cobertura de testes:** `build/reports/jacoco/test/html/index.html`
- **Checkstyle:** `build/reports/checkstyle/main.html` e `build/reports/checkstyle/test.html`
- **PMD:** `build/reports/pmd/main.html` e `build/reports/pmd/test.html`
- **OWASP:** `build/reports/dependency-check-report.html`

### Script de Automação

Use o script para gerar relatórios facilmente:

```bash
# Gerar todos os relatórios
./scripts/quality-reports.sh

# Gerar apenas JaCoCo
./scripts/quality-reports.sh jacoco

# Abrir relatórios no browser
./scripts/quality-reports.sh open
```

### SonarQube Local (Alternativa ao SonarCloud)

Para uma análise mais detalhada, use o SonarQube local:

```bash
# 1. Iniciar SonarQube (requer Docker)
docker compose up -d sonarqube

# 2. Aguardar inicialização (~2 minutos)
# Acesse: http://localhost:9000 (login: admin/admin)

# 3. Gerar análise
./gradlew sonar

# 4. Parar SonarQube
docker compose stop sonarqube
```

---

## ⚙️ Configuração e Execução

### Pré-requisitos
- JDK 25 instalado.
- Docker e Docker Compose instalados.

### Passos para rodar em Desenvolvimento
1.  **Infraestrutura:** Suba o banco, cache e mensageria:
    ```bash
    docker compose up -d
    ```
2.  **Variáveis de Ambiente:** Defina o pepper (opcional no dev, obrigatório no prod):
    ```bash
    export PASSWORD_PEPPER=meu_pepper_secreto
    ```
3.  **Aplicação:** Inicie a API (perfil `dev` ativo por padrão):
    ```bash
    ./gradlew bootRun
    ```

### Perfis de Ambiente
- **dev (padrão):** Configurado para uso local com containers Docker.
- **prod:** Requer variáveis de ambiente (`DB_URL`, `REDIS_HOST`, `PASSWORD_PEPPER`, etc.).
- **test:** Usado automaticamente durante a execução de `./gradlew test` (usa banco H2 em memória).

---

## 🛠️ Roadmap (Próximos Passos)

### Implementado
- [x] Módulo de autenticação (JWT) e testes automatizados
- [x] Hashing de senhas seguro com Argon2id
- [x] CRUD de eventos (Admin e Cliente)
- [x] Sistema de reserva com Redis
- [x] Integração com RabbitMQ
- [x] Worker de processamento
- [x] Qualidade de Software (JaCoCo, Checkstyle, CI/CD, SonarCloud)
- [x] Logs estruturados com MDC e correlation ID
- [x] Spring Actuator e Micrometer para métricas
- [x] Refatoração para Builder Pattern nas entidades
- [x] Módulo de pagamentos online (Pix, Débito e Crédito)

### Pendente
- [ ] Dashboard administrativo
- [ ] Testes de performance (Gatling/JMeter)
- [ ] Testes de contrato (Spring Cloud Contract)

---

## 📌 Objetivo do Projeto

Este projeto serve como:

- base para sistemas escaláveis
- estudo de arquitetura moderna
- portfólio profissional
