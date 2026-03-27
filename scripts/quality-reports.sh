#!/bin/bash

# =============================================================================
# Script de Geração de Relatórios de Qualidade
# =============================================================================
# Uso: ./scripts/quality-reports.sh [option]
#
# Opções:
#   all       - Gera todos os relatórios (padrão)
#   jacoco    - Apenas cobertura de testes
#   checkstyle- Apenas Checkstyle
#   pmd       - Apenas PMD
#   owasp     - Apenas OWASP Dependency Check
#   sonar     - Apenas SonarQube (requer SonarQube rodando)
#   open      - Abre relatórios no browser
#   clean     - Limpa build e relatórios
# =============================================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPORTS_DIR="$PROJECT_ROOT/build/reports"

print_header() {
    echo ""
    echo "================================================================"
    echo -e "${GREEN}$1${NC}"
    echo "================================================================"
    echo ""
}

print_info() {
    echo -e "${YELLOW}INFO:${NC} $1"
}

print_error() {
    echo -e "${RED}ERRO:${NC} $1"
}

print_success() {
    echo -e "${GREEN}SUCESSO:${NC} $1"
}

show_help() {
    echo "Uso: $0 [opcao]"
    echo ""
    echo "Opções:"
    echo "  all        - Gera todos os relatórios (padrão)"
    echo "  jacoco     - Apenas cobertura de testes"
    echo "  checkstyle - Apenas Checkstyle"
    echo "  pmd        - Apenas PMD"
    echo "  owasp      - Apenas OWASP Dependency Check"
    echo "  sonar      - Apenas SonarQube (requer SonarQube rodando)"
    echo "  open       - Abre relatórios no browser"
    echo "  clean      - Limpa build e relatórios"
    echo "  help       - Mostra esta ajuda"
    echo ""
}

generate_jacoco() {
    print_header "📊 Gerando Relatório JaCoCo (Cobertura de Testes)"
    cd "$PROJECT_ROOT"
    ./gradlew jacocoTestReport --quiet
    print_success "Relatório JaCoCo gerado em: $REPORTS_DIR/jacoco/test/html/index.html"
}

generate_checkstyle() {
    print_header "📝 Gerando Relatório Checkstyle"
    cd "$PROJECT_ROOT"
    ./gradlew checkstyleMain checkstyleTest --quiet
    print_success "Relatórios Checkstyle gerados em:"
    echo "  - $REPORTS_DIR/checkstyle/main.html"
    echo "  - $REPORTS_DIR/checkstyle/test.html"
}

generate_pmd() {
    print_header "🔍 Gerando Relatório PMD"
    cd "$PROJECT_ROOT"
    ./gradlew pmdMain pmdTest --quiet
    print_success "Relatórios PMD gerados em:"
    echo "  - $REPORTS_DIR/pmd/main.html"
    echo "  - $REPORTS_DIR/pmd/test.html"
}

generate_owasp() {
    print_header "🔒 Gerando Relatório OWASP Dependency Check"
    cd "$PROJECT_ROOT"
    ./gradlew dependencyCheckAnalyze --quiet
    print_success "Relatório OWASP gerado em: $REPORTS_DIR/dependency-check-report.html"
}

generate_sonar() {
    print_header "📈 Gerando Análise SonarQube"
    cd "$PROJECT_ROOT"
    
    # Verifica se SonarQube está rodando
    if ! curl -s http://localhost:9000/api/system/status > /dev/null 2>&1; then
        print_error "SonarQube não está rodando em http://localhost:9000"
        print_info "Inicie com: docker compose up -d sonarqube"
        exit 1
    fi
    
    ./gradlew sonar --quiet
    print_success "Análise SonarQube enviada para: http://localhost:9000"
}

open_reports() {
    print_header "🌐 Abrindo Relatórios"
    
    if [ -f "$REPORTS_DIR/jacoco/test/html/index.html" ]; then
        print_info "Abrindo JaCoCo..."
        xdg-open "$REPORTS_DIR/jacoco/test/html/index.html" 2>/dev/null || \
        open "$REPORTS_DIR/jacoco/test/html/index.html" 2>/dev/null || true
    else
        print_error "Relatório JaCoCo não encontrado. Execute './gradlew jacocoTestReport'"
    fi
    
    if [ -f "$REPORTS_DIR/pmd/main.html" ]; then
        print_info "Abrindo PMD..."
        xdg-open "$REPORTS_DIR/pmd/main.html" 2>/dev/null || \
        open "$REPORTS_DIR/pmd/main.html" 2>/dev/null || true
    fi
    
    if [ -f "$REPORTS_DIR/checkstyle/main.html" ]; then
        print_info "Abrindo Checkstyle..."
        xdg-open "$REPORTS_DIR/checkstyle/main.html" 2>/dev/null || \
        open "$REPORTS_DIR/checkstyle/main.html" 2>/dev/null || true
    fi
}

clean_build() {
    print_header "🧹 Limpando Build e Relatórios"
    cd "$PROJECT_ROOT"
    ./gradlew clean --quiet
    print_success "Build limpo!"
}

generate_all() {
    print_header "🚀 Gerando Todos os Relatórios de Qualidade"
    
    generate_jacoco
    generate_checkstyle
    generate_pmd
    generate_owasp
    
    echo ""
    print_success "✅ Todos os relatórios foram gerados!"
    echo ""
    echo "📁 Localização dos relatórios:"
    echo "   JaCoCo:     $REPORTS_DIR/jacoco/test/html/index.html"
    echo "   Checkstyle: $REPORTS_DIR/checkstyle/main.html"
    echo "   PMD:        $REPORTS_DIR/pmd/main.html"
    echo "   OWASP:      $REPORTS_DIR/dependency-check-report.html"
    echo ""
    echo "💡 Dica: Use '$0 open' para abrir os relatórios no browser"
}

# Main
case "${1:-all}" in
    all)
        generate_all
        ;;
    jacoco)
        generate_jacoco
        ;;
    checkstyle)
        generate_checkstyle
        ;;
    pmd)
        generate_pmd
        ;;
    owasp)
        generate_owasp
        ;;
    sonar)
        generate_sonar
        ;;
    open)
        open_reports
        ;;
    clean)
        clean_build
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Opção desconhecida: $1"
        show_help
        exit 1
        ;;
esac
