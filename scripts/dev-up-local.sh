#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.infra.yml"

log() {
  printf '%s\n' "$*"
}

die() {
  log "Erro: $*"
  exit 1
}

KEEP_INFRA=0

usage() {
  cat <<'EOF'
Uso:
  scripts/dev-up-local.sh            # sobe postgres/redis/rabbit e roda ./gradlew bootRun
  scripts/dev-up-local.sh --keep     # nao derruba a infra ao sair
  scripts/dev-up-local.sh --down     # derruba a infra e sai
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "${1:-}" == "--keep" ]]; then
  KEEP_INFRA=1
  shift
fi

if [[ "${1:-}" == "--down" ]]; then
  docker compose -f "${COMPOSE_FILE}" down
  exit 0
fi

command -v docker >/dev/null 2>&1 || die "docker nao encontrado no PATH"

cleanup() {
  if [[ "${KEEP_INFRA}" -eq 1 ]]; then
    log "Mantendo infra ativa (--keep)."
    return 0
  fi
  log "Derrubando infra..."
  docker compose -f "${COMPOSE_FILE}" down
}
trap cleanup EXIT

log "Subindo infra (postgres/redis/rabbitmq)..."
docker compose -f "${COMPOSE_FILE}" up -d

wait_for() {
  local name="$1"
  local cmd="$2"
  local timeout_s="${3:-60}"

  local start
  start="$(date +%s)"
  while true; do
    if eval "${cmd}" >/dev/null 2>&1; then
      log "OK: ${name}"
      return 0
    fi
    if (( "$(date +%s)" - start >= timeout_s )); then
      die "timeout esperando ${name}"
    fi
    sleep 1
  done
}

wait_for "Postgres" "docker exec ticketscale-postgres pg_isready -U ticketscale -d ticketscale" 60
wait_for "Redis" "docker exec ticketscale-redis redis-cli ping | grep -q PONG" 30
wait_for "RabbitMQ" "docker exec ticketscale-rabbitmq rabbitmq-diagnostics -q ping" 60

# Somente o pepper precisa ser consistente com os hashes salvos no banco.
# Os hashes do src/main/resources/data.sql validam com "default_pepper" (default do app).
export PASSWORD_PEPPER="${PASSWORD_PEPPER:-default_pepper}"

log "Iniciando API via Gradle..."
cd "${ROOT_DIR}"
exec ./gradlew bootRun
