# 📋 Plano de Melhoria da Qualidade — TicketScale

## Visão Geral

Este plano define as iniciativas para elevar a qualidade do software do TicketScale de **8.5/10** para **9.5/10+**, com foco em automação, métricas e boas práticas de engenharia de software.

---

## 🎯 Objetivos e Metas

| Objetivo | Meta Atual | Meta Alvo | Prioridade |
|----------|------------|-----------|------------|
| Cobertura de testes | Não mensurada | ≥ 80% | Alta |
| Análise estática | Não integrada | 0 warnings críticos | Alta |
| CI/CD | Não configurado | Pipeline automatizado | Alta |
| Qualidade contínua | Não monitorada | Sonar Cloud integrado | Média |
| Logs estruturados | Parcial | MDC + tracing | Média |
| Imutabilidade | Entidades mutáveis | Records/Builders | Baixa |

---

## 📅 Roadmap de Implementação

### Fase 1 — Fundamentos de Qualidade (Semana 1-2) ⭐ **Prioridade Máxima**

| Tarefa | Descrição | Esforço | Critério de Aceite |
|--------|-----------|---------|-------------------|
| **1.1 Configurar Jacoco** | Adicionar plugin de cobertura de testes no `build.gradle` | 2h | Relatório HTML/XML gerado após `./gradlew test` |
| **1.2 Definir metas de cobertura** | Configurar regras mínimas (80% classes críticas) | 1h | Build falha se cobertura < 80% |
| **1.3 Integrar SpotBugs** | Plugin para detecção de bugs comuns | 2h | 0 warnings críticos no build |
| **1.4 Integrar Checkstyle** | Padronização de código Java | 2h | Código formatado conforme padrão definido |

### Fase 2 — Automação e CI/CD (Semana 3-4) ⭐ **Prioridade Máxima**

| Tarefa | Descrição | Esforço | Critério de Aceite |
|--------|-----------|---------|-------------------|
| **2.1 Criar GitHub Actions** | Workflow de CI com testes em cada push | 4h | Pipeline roda testes automaticamente |
| **2.2 Integrar SonarCloud** | Análise contínua de qualidade | 3h | Dashboard Sonar disponível publicamente |
| **2.3 Gate de qualidade no PR** | Exigir aprovação do Sonar para merge | 2h | PR bloqueado se qualidade degradar |

### Fase 3 — Melhorias de Código (Semana 5-6)

| Tarefa | Descrição | Esforço | Critério de Aceite |
|--------|-----------|---------|-------------------|
| **3.1 Refatorar entidades para imutabilidade** | Usar records (Java 17+) ou builder pattern | 8h | Entidades `Usuario`, `Reserva`, `Evento` imutáveis |
| **3.2 Adicionar logs estruturados** | SLF4J com MDC para tracing distribuído | 4h | Logs com correlation ID em todas as requisições |
| **3.3 Integrar Micrometer** | Métricas de aplicação e saúde | 4h | Endpoints `/actuator/metrics` e `/actuator/health` |

### Fase 4 — Expansão de Testes (Semana 7-8)

| Tarefa | Descrição | Esforço | Critério de Aceite |
|--------|-----------|---------|-------------------|
| **4.1 Testes de contrato** | Validar APIs REST com Spring Cloud Contract | 6h | Contratos definidos para todos os endpoints |
| **4.2 Testes de performance** | JMeter ou Gatling para fluxo crítico (reserva) | 8h | Relatório de performance sob carga |
| **4.3 Testes de mutação** | Pitest para validar eficácia dos testes | 4h | Mutation score ≥ 70% |

---

## 🔧 Detalhamento Técnico

### 1. Configuração do Jacoco

```gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.12"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/config/**',
                '**/dto/**',
                '**/*Application.*',
                '**/*DTO.*'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = 'CLASS'
            includes = ['com.ticketscale.domain.**', 'com.ticketscale.application.**']
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.80
            }
        }
    }
}

// Hook para rodar cobertura automaticamente
check.dependsOn jacocoTestCoverageVerification
```

### 2. Configuração do SpotBugs

```gradle
plugins {
    id 'com.github.spotbugs' version '6.0.0'
}

spotbugs {
    ignoreFailures = false
    showProgress = true
    effort = 'max'
    reportLevel = 'medium'
}

tasks.withType(com.github.spotbugs.snom.SpotBugsTask).configureEach {
    reports {
        html { required = true }
        xml { required = true }
    }
}
```

### 3. Configuração do Checkstyle

```gradle
plugins {
    id 'checkstyle'
}

checkstyle {
    toolVersion = '10.12.0'
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    ignoreFailures = false
    showViolations = true
}
```

### 4. GitHub Actions Workflow

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:17
        env:
          POSTGRES_DB: ticketscale_test
          POSTGRES_USER: ticketscale
          POSTGRES_PASSWORD: ticketscale
        ports: [5432:5432]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          cache: gradle
      
      - name: Build with Gradle
        run: ./gradlew build
      
      - name: Run Tests with Coverage
        run: ./gradlew test jacocoTestReport
      
      - name: Run Static Analysis
        run: ./gradlew spotbugsMain checkstyleMain
      
      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@master
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### 5. SonarCloud Configuration

```properties
# sonar-project.properties
sonar.organization=ticketscale
sonar.projectKey=ticketscale
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=build/classes/java/main
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
sonar.host.url=https://sonarcloud.io

# Quality Gates
sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300

# Exclusions
sonar.exclusions=**/config/**,**/dto/**,**/*Application.*
sonar.coverage.exclusions=**/config/**,**/dto/**,**/*DTO.*
```

### 6. Logs Estruturados com MDC

```java
// MDC Filter para correlation ID
@Component
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        try {
            MDC.put("correlationId", UUID.randomUUID().toString());
            MDC.put("timestamp", Instant.now().toString());
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

### 7. Refatoração para Imutabilidade (Exemplo)

```java
// Antes
@Entity(name = "Usuario")
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String login;
    
    // getters e setters...
}

// Depois (com builder pattern)
@Entity(name = "Usuario")
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;
    
    @Column(unique = true, nullable = false)
    private final String login;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final Papel papel;
    
    // Construtor privado
    private Usuario(Builder builder) {
        this.id = builder.id;
        this.login = builder.login;
        this.papel = builder.papel;
    }
    
    // Builder estático
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private UUID id;
        private String login;
        private Papel papel;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder login(String login) { this.login = login; return this; }
        public Builder papel(Papel papel) { this.papel = papel; return this; }
        public Usuario build() { return new Usuario(this); }
    }
}
```

---

## 📊 Métricas de Sucesso

| Métrica | Linha de Base | Meta | Como Medir |
|---------|---------------|------|------------|
| Cobertura de testes | ~30% (estimado) | ≥ 80% | Jacoco report |
| Bugs críticos (SpotBugs) | Desconhecido | 0 | SpotBugs report |
| Violações Checkstyle | Desconhecido | 0 | Checkstyle report |
| Tempo de build CI | N/A | < 10 min | GitHub Actions logs |
| Technical Debt Ratio | Desconhecido | < 5% | SonarCloud |
| Code Smells | Desconhecido | < 50 | SonarCloud |
| Vulnerabilidades | Desconhecido | 0 | SonarCloud Security |

---

## 🚦 Critérios de Aceite do Plano

O plano será considerado **concluído** quando:

- [ ] ✅ Cobertura de testes ≥ 80% nas camadas `domain` e `application`
- [ ] ✅ Pipeline CI rodando em todos os PRs
- [ ] ✅ SonarCloud integrado com Quality Gate ativo
- [ ] ✅ 0 warnings críticos do SpotBugs
- [ ] ✅ 0 violações do Checkstyle
- [ ] ✅ Logs estruturados com correlation ID em todas as requisições
- [ ] ✅ Documentação atualizada no README.md e GEMINI.md

---

## 📝 Próximos Passos Imediatos

1. **Revisar e aprovar este plano** com o time
2. **Criar branch `feat/quality-improvements`** para implementação
3. **Implementar Fase 1** (Jacoco + SpotBugs + Checkstyle) — 1 semana
4. **Implementar Fase 2** (GitHub Actions + SonarCloud) — 1 semana
5. **Revisar progresso** e ajustar roadmap conforme necessário

---

## 🔗 Referências

- [Jacoco Documentation](https://www.eclemma.org/jacoco/)
- [SpotBugs Gradle Plugin](https://spotbugs.github.io/)
- [Checkstyle](https://checkstyle.org/)
- [SonarCloud](https://sonarcloud.io/)
- [GitHub Actions for Java](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-gradle)
- [Micrometer](https://micrometer.io/)
- [SLF4J MDC](https://www.slf4j.org/api/org/slf4j/MDC.html)
