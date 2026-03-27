# 📊 Dashboard de Qualidade de Software

Este diretório contém relatórios consolidados de qualidade do código.

## Relatórios Gerados

Após executar `./gradlew build`, os seguintes relatórios estarão disponíveis:

### 1. Cobertura de Testes (JaCoCo)
- **Local:** `build/reports/jacoco/test/html/index.html`
- **Descrição:** Mostra porcentagem de cobertura de código por classe/método
- **Meta:** ≥ 80% nas camadas domain e application

### 2. Análise Estática (Checkstyle)
- **Local:** `build/reports/checkstyle/main.html`
- **Local (testes):** `build/reports/checkstyle/test.html`
- **Descrição:** Verifica padronização de código (nomenclatura, imports, formatação)
- **Meta:** 0 violações críticas

### 3. Análise de Bugs (PMD)
- **Local:** `build/reports/pmd/main.html`
- **Local (testes):** `build/reports/pmd/test.html`
- **Descrição:** Detecta bugs potenciais, código ineficiente, más práticas
- **Meta:** 0 violações de alta prioridade

### 4. Dependências Vulneráveis (OWASP)
- **Local:** `build/reports/dependency-check-report.html`
- **Descrição:** Identifica vulnerabilidades conhecidas em dependências
- **Meta:** 0 vulnerabilidades críticas

## Como Gerar Relatórios

```bash
# Build completo com todos os relatórios
./gradlew clean build

# Apenas cobertura de testes
./gradlew jacocoTestReport

# Apenas Checkstyle
./gradlew checkstyleMain checkstyleTest

# Apenas PMD
./gradlew pmdMain pmdTest

# Apenas OWASP Dependency Check
./gradlew dependencyCheckAnalyze

# Abrir relatório JaCoCo (Linux)
xdg-open build/reports/jacoco/test/html/index.html

# Abrir relatório JaCoCo (Mac)
open build/reports/jacoco/test/html/index.html
```

## Interpretação dos Relatórios

### JaCoCo - Cobertura

| Cobertura | Status | Ação |
|-----------|--------|------|
| ≥ 80% | ✅ Excelente | Manter |
| 60-79% | ⚠️ Aceitável | Melhorar |
| < 60% | ❌ Crítico | Refatorar com testes |

### Checkstyle/PMD - Violações

| Prioridade | Cor | Ação |
|------------|-----|------|
| Error | 🔴 | Corrigir imediatamente |
| Warning | 🟡 | Planejar correção |
| Info | 🔵 | Opcional |

## Histórico de Qualidade

| Data | Cobertura | Checkstyle Errors | PMD Errors |
|------|-----------|-------------------|------------|
| 2026-03-26 | Ver relatório | Ver relatório | Ver relatório |

## Ferramentas Utilizadas

| Ferramenta | Versão | Finalidade |
|------------|--------|------------|
| JaCoCo | 0.8.13 | Cobertura de testes |
| Checkstyle | 10.12.0 | Padronização de código |
| PMD | 7.7.0 | Detecção de bugs |
| OWASP DC | 12.1.0 | Segurança de dependências |

## Integração Contínua

Todos os relatórios são gerados automaticamente no GitHub Actions a cada push/PR.

- **Workflow:** `.github/workflows/ci.yml`
- **Artefatos:** Relatórios são uploadados como artifacts do GitHub

## SonarQube Local (Opcional)

Para uma análise mais detalhada, é possível rodar o SonarQube localmente:

```bash
# Iniciar SonarQube (requer Docker)
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community

# Acessar dashboard
# http://localhost:9000 (admin/admin)

# Rodar análise
./gradlew sonar -Dsonar.host.url=http://localhost:9000
```

## Contato

Para dúvidas sobre qualidade de código, consulte a documentação ou abra uma issue.
