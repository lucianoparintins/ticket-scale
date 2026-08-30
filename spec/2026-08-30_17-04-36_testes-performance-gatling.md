# Especificação Técnica: Testes de Performance com Gatling

## 1. Visão Geral

Este documento define o plano arquitetural e operacional para a implementação da suíte de **Testes de Performance e Carga** do projeto **TicketScale** utilizando a ferramenta [Gatling](https://gatling.io/) com **Java DSL**.

O objetivo primordial é validar a capacidade do sistema em cenários de alta concorrência, avaliar a latência dos endpoints críticos, aferir a eficácia do cache distribuído (Redis) e certificar a consistência do mecanismo de lock distribuído na prevenção de *overselling* de ingressos.

---

## 2. Stack Tecnológica e Integração

- **Linguagem:** Java 25 (Gatling Java DSL).
- **Build Tool:** Gradle com o plugin `io.gatling.gradle`.
- **Compatibilidade:** Totalmente integrado ao toolchain Java 25 existente, sem dependência do compilador Scala.
- **Localização dos Arquivos no Projeto:**
  - Código-fonte das simulações: `src/gatling/java/com/ticketscale/performance/`
  - Massa de dados e payloads: `src/gatling/resources/data/` e `src/gatling/resources/bodies/`

---

## 3. Cenários de Teste e Simulações

A suíte será composta por simulações modulares e focadas:

### 3.1 `AutenticacaoSimulation`
- **Objetivo:** Avaliar o throughput e latência do endpoint de login e geração de JWT.
- **Endpoint:** `POST /api/v1/auth/login`
- **Payload:** Credenciais de usuário (email e senha).
- **Métricas:** Throughput máximo suportado pelo hashing Argon2id e validações de segurança.

### 3.2 `ConsultaEventosSimulation`
- **Objetivo:** Avaliar o desempenho dos endpoints de leitura sob alta concorrência e validar o impacto do cache Redis.
- **Endpoints:**
  - `GET /api/v1/eventos`
  - `GET /api/v1/eventos/{id}`
- **Comportamento Esperado:** Baixa latência sustentada devido às respostas servidas via cache Redis.

### 3.3 `ReservaConcorrenteSimulation` (Cenário Crítico)
- **Objetivo:** Simular alta disputa simultânea pelo mesmo lote de ingressos limitados, validando a eficácia do lock distribuído via Redis (`RedisLockManager`) e garantindo que não ocorra *overselling*.
- **Endpoint:** `POST /api/v1/reservas`
- **Validação de Negócio:**
  - Usuários que obtiverem a reserva recebem `201 Created`.
  - Quando a capacidade do lote esgotar, respostas `409 Conflict` ou `422 Unprocessable Entity` são computadas como resultado de negócio esperado (e não como falhas de infraestrutura).
  - Nenhuma reserva excedente ao estoque disponível do lote pode ser persistida.

### 3.4 `CheckoutCompletoSimulation` (End-to-End)
- **Objetivo:** Simular o fluxo completo de compra do usuário final.
- **Fluxo do Usuário Virtual:**
  1. `POST /api/v1/auth/login` $\rightarrow$ Obtenção e extração do JWT.
  2. `GET /api/v1/eventos` $\rightarrow$ Listagem dos eventos disponíveis.
  3. `POST /api/v1/reservas` $\rightarrow$ Reserva de ingresso em um lote específico.
  4. `POST /api/v1/pagamentos` $\rightarrow$ Confirmação e processamento do pagamento via gateway mock.

---

## 4. Massa de Dados e Autenticação

1. **Estratégia de Autenticação:**
   - Cada usuário virtual executa o login dinamicamente no início da sessão ou utiliza massa alimentada por feeders CSV (`users.csv`).
   - O token JWT recebido no corpo da resposta do login é salvo na `Session` do Gatling via `jsonPath("$.token").saveAs("jwtToken")` e repassado no header `Authorization: Bearer #{jwtToken}` nas requisições autenticadas.

2. **Feeder de Eventos e Lotes:**
   - Arquivo `eventos.csv` mapeando IDs de eventos e lotes pré-populados pelo `data.sql` de desenvolvimento ou criados via scripts de seed.

---

## 5. Parametrização e Perfis de Execução

As simulações serão configuráveis via propriedades de sistema (`System Properties`) do Java para fácil parametrização local e em pipelines:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | URL base da API |
| `users` | `50` | Quantidade de usuários virtuais concorrentes |
| `rampDuration` | `10` | Tempo de ramp-up em segundos |
| `testDuration` | `60` | Duração do teste sustentado em segundos |
| `profile` | `smoke` | Perfil de carga (`smoke`, `load`, `stress`) |

---

## 6. SLAs e Critérios de Aceite (Gatling Assertions)

As simulações conterão asserções automatizadas para validar se a aplicação atende aos requisitos não funcionais de performance:

- **Consultas e Leituras (com Cache):**
  - $p_{95} < 200\text{ms}$
  - $p_{99} < 500\text{ms}$
  - Taxa de sucesso $> 99.9\%$
- **Operações Transacionais / Concorrentes (Reserva & Pagamento):**
  - $p_{95} < 1.0\text{s}$
  - $p_{99} < 2.0\text{s}$
- **Erros de Infraestrutura (HTTP 5xx):**
  - Taxa de erro 5xx $< 0.1\%$
  - Erros 4xx esperados por regras de domínio (ex: ingresso esgotado) não invalidam o SLA de estabilidade.

---

## 7. Automação e Comandos de Execução

- **Execução via Gradle:**
  ```bash
  # Executar todas as simulações
  ./gradlew gatlingRun

  # Executar uma simulação específica
  ./gradlew gatlingRun-com.ticketscale.performance.ReservaConcorrenteSimulation -Dusers=100 -DrampDuration=15
  ```

- **Script Helper:**
  Criação do script `scripts/run-performance-tests.sh` para facilitar execuções locais interativas com logs e relatórios organizados em `build/reports/gatling/`.
