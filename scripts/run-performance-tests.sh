#!/bin/bash
set -e

# ==============================================================================
# Script de Execução de Testes de Performance com Gatling
# Projeto: TicketScale
# ==============================================================================

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERS="${USERS:-50}"
RAMP_DURATION="${RAMP_DURATION:-10}"
TEST_DURATION="${TEST_DURATION:-60}"
PROFILE="${PROFILE:-smoke}"
SIMULATION="${SIMULATION:-all}"

echo "======================================================================"
echo "           TicketScale - Execução de Testes de Performance             "
echo "======================================================================"
echo " URL Alvo:         $BASE_URL"
echo " Perfil:           $PROFILE"
echo " Usuários:         $USERS"
echo " Ramp-up (s):      $RAMP_DURATION"
echo " Duração (s):      $TEST_DURATION"
echo " Simulação:        $SIMULATION"
echo "======================================================================"

# Verifica se o endpoint está acessível
echo "Verificando disponibilidade da aplicação em $BASE_URL..."
if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1 || curl -s -f "$BASE_URL/api/eventos" > /dev/null 2>&1 || curl -s "$BASE_URL" > /dev/null 2>&1; then
    echo "✅ Aplicação alvo está online."
else
    echo "⚠️  Aviso: Não foi possível conectar a $BASE_URL. Certifique-se de que a aplicação esteja rodando antes do teste."
fi

GRADLE_OPTS="-DbaseUrl=$BASE_URL -Dusers=$USERS -DrampDuration=$RAMP_DURATION -DtestDuration=$TEST_DURATION -Dprofile=$PROFILE"

case "$SIMULATION" in
    "auth"|"autenticacao")
        echo "🚀 Executando Simulação de Autenticação..."
        ./gradlew gatlingRun-com.ticketscale.performance.AutenticacaoSimulation $GRADLE_OPTS
        ;;
    "eventos"|"consulta")
        echo "🚀 Executando Simulação de Consulta de Eventos..."
        ./gradlew gatlingRun-com.ticketscale.performance.ConsultaEventosSimulation $GRADLE_OPTS
        ;;
    "reserva"|"concorrencia")
        echo "🚀 Executando Simulação de Reserva Concorrente com Lock..."
        ./gradlew gatlingRun-com.ticketscale.performance.ReservaConcorrenteSimulation $GRADLE_OPTS
        ;;
    "checkout"|"e2e")
        echo "🚀 Executando Simulação de Checkout Completo..."
        ./gradlew gatlingRun-com.ticketscale.performance.CheckoutCompletoSimulation $GRADLE_OPTS
        ;;
    "all"|*)
        echo "🚀 Executando todas as simulações do Gatling..."
        ./gradlew gatlingRun $GRADLE_OPTS
        ;;
esac

echo ""
echo "======================================================================"
echo "✅ Testes de performance concluídos com sucesso!"
echo "Relatórios HTML gerados em: build/reports/gatling/"
echo "======================================================================"
