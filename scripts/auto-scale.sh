#!/bin/bash

# Script de Auto-scaling para TicketScale no Docker Swarm
# Este script monitora CPU/Memory e escala o serviço automaticamente
#
# Uso: ./scripts/auto-scale.sh [min_replicas] [max_replicas] [cpu_threshold]
# Exemplo: ./scripts/auto-scale.sh 2 10 70

set -e

# Configurações padrão
MIN_REPLICAS=${1:-2}
MAX_REPLICAS=${2:-10}
CPU_THRESHOLD=${3:-70}
MEMORY_THRESHOLD=${4:-80}
CHECK_INTERVAL=${5:-30}
SERVICE_NAME="ticketscale_api"

echo "=========================================="
echo "TicketScale - Auto-scaling Service"
echo "=========================================="
echo "Configuração:"
echo "  Réplicas: $MIN_REPLICAS - $MAX_REPLICAS"
echo "  CPU Threshold: $CPU_THRESHOLD%"
echo "  Memory Threshold: $MEMORY_THRESHOLD%"
echo "  Check Interval: ${CHECK_INTERVAL}s"
echo "=========================================="

# Função para obter métricas de CPU
get_cpu_usage() {
    docker stats --no-stream --format "{{.CPUPerc}}" $(docker ps -q --filter name=$SERVICE_NAME) | \
    awk '{sum+=$1} END {print sum/NR}' | \
    sed 's/%//'
}

# Função para obter métricas de Memory
get_memory_usage() {
    docker stats --no-stream --format "{{.MemPerc}}" $(docker ps -q --filter name=$SERVICE_NAME) | \
    awk '{sum+=$1} END {print sum/NR}' | \
    sed 's/%//'
}

# Função para obter número atual de réplicas
get_current_replicas() {
    docker service ls --filter name=$SERVICE_NAME --format "{{.Replicas}}" | \
    awk -F'/' '{print $2}'
}

# Função para escalar serviço
scale_service() {
    local new_replicas=$1
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Escalando para $new_replicas réplicas..."
    docker service scale ${SERVICE_NAME}=${new_replicas}
}

# Loop principal de monitoramento
while true; do
    CPU_USAGE=$(get_cpu_usage)
    MEMORY_USAGE=$(get_memory_usage)
    CURRENT_REPLICAS=$(get_current_replicas)
    
    # Remove casas decimais para comparação
    CPU_INT=${CPU_USAGE%.*}
    MEMORY_INT=${MEMORY_USAGE%.*}
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] CPU: ${CPU_USAGE}% | Memory: ${MEMORY_USAGE}% | Réplicas: $CURRENT_REPLICAS"
    
    # Verifica se precisa escalar para cima
    if (( $(echo "$CPU_USAGE > $CPU_THRESHOLD" | bc -l) )) || \
       (( $(echo "$MEMORY_USAGE > $MEMORY_THRESHOLD" | bc -l) )); then
        if [ "$CURRENT_REPLICAS" -lt "$MAX_REPLICAS" ]; then
            NEW_REPLICAS=$((CURRENT_REPLICAS + 1))
            scale_service $NEW_REPLICAS
        else
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] Já atingiu máximo de réplicas ($MAX_REPLICAS)"
        fi
    # Verifica se precisa escalar para baixo
    elif (( $(echo "$CPU_USAGE < ($CPU_THRESHOLD / 2)" | bc -l) )) && \
         (( $(echo "$MEMORY_USAGE < ($MEMORY_THRESHOLD / 2)" | bc -l) )); then
        if [ "$CURRENT_REPLICAS" -gt "$MIN_REPLICAS" ]; then
            NEW_REPLICAS=$((CURRENT_REPLICAS - 1))
            scale_service $NEW_REPLICAS
        fi
    fi
    
    sleep $CHECK_INTERVAL
done
