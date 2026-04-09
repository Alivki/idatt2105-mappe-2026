#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  source "$ROOT_DIR/.env"
  set +a
fi

LOG_DIR="${LOG_DIR:-$ROOT_DIR/.run-logs}"

FOOD_PORT="${FOOD_PORT:-8081}"
ALCOHOL_PORT="${ALCOHOL_PORT:-8082}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

SKIP_NPM_INSTALL="${SKIP_NPM_INSTALL:-false}"
SKIP_MVN_INSTALL="${SKIP_MVN_INSTALL:-false}"
STOP_DB_ON_EXIT="${STOP_DB_ON_EXIT:-true}"

mkdir -p "$LOG_DIR"

section() {
  printf "\n=== %s ===\n" "$1"
}

die() {
  echo "ERROR: $1" >&2
  exit 1
}

wait_for_mysql_health() {
  local deadline_seconds=60
  local start_ts
  start_ts="$(date +%s)"

  while true; do
    local cid
    cid="$(docker compose ps -q mysql 2>/dev/null || true)"
    if [[ -n "$cid" ]]; then
      local health
      health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' "$cid" 2>/dev/null || true)"
      if [[ "$health" == "healthy" || "$health" == "no-healthcheck" ]]; then
        return 0
      fi
    fi

    local now_ts
    now_ts="$(date +%s)"
    if (( now_ts - start_ts >= deadline_seconds )); then
      echo "MySQL container did not become healthy within ${deadline_seconds}s." >&2
      echo "Check logs with: docker compose logs -f mysql" >&2
      return 1
    fi

    sleep 2
  done
}

cleanup() {
  echo
  section "Stopping dev stack"

  if [[ -n "${FOOD_PID:-}" ]]; then kill "$FOOD_PID" 2>/dev/null || true; fi
  if [[ -n "${ALCOHOL_PID:-}" ]]; then kill "$ALCOHOL_PID" 2>/dev/null || true; fi
  if [[ -n "${FRONTEND_PID:-}" ]]; then kill "$FRONTEND_PID" 2>/dev/null || true; fi

  if [[ "$STOP_DB_ON_EXIT" == "true" ]]; then
    docker compose stop mysql >/dev/null 2>&1 || true
  fi
}
trap cleanup INT TERM EXIT

cd "$ROOT_DIR"

section "Starting database (MySQL)"
docker compose up -d mysql
wait_for_mysql_health

echo "MySQL ready on localhost:3306"

section "Building shared backend module (ik-common)"
cd "$ROOT_DIR/backend"

if [[ "$SKIP_MVN_INSTALL" != "true" ]]; then
  ./mvnw -q -pl ik-common -Dmaven.test.skip=true install
else
  echo "SKIP_MVN_INSTALL=true -> skipping 'ik-common install'"
fi

section "Starting backends"
cd "$ROOT_DIR/backend/ik-food-service"

SERVER_PORT="$FOOD_PORT" ../mvnw -q spring-boot:run \
  >"$LOG_DIR/ik-food-service.log" 2>&1 &
FOOD_PID=$!

echo "IK-Mat starting (PID $FOOD_PID) on http://localhost:$FOOD_PORT"

echo
cd "$ROOT_DIR/backend/ik-alcohol-service"

SERVER_PORT="$ALCOHOL_PORT" ../mvnw -q spring-boot:run \
  >"$LOG_DIR/ik-alcohol-service.log" 2>&1 &
ALCOHOL_PID=$!

echo "IK-Alkohol starting (PID $ALCOHOL_PID) on http://localhost:$ALCOHOL_PORT"

section "Starting frontend (Vite dev server)"
cd "$ROOT_DIR/frontend"

if [[ ! -d node_modules ]]; then
  if [[ "$SKIP_NPM_INSTALL" == "true" ]]; then
    die "node_modules mangler, men SKIP_NPM_INSTALL=true. Kjør 'npm ci' i frontend/ først."
  fi
  npm ci
fi

npm run dev -- --port "$FRONTEND_PORT" \
  >"$LOG_DIR/frontend-dev.log" 2>&1 &
FRONTEND_PID=$!

echo "Frontend starting (PID $FRONTEND_PID) on http://localhost:$FRONTEND_PORT"

echo
section "Running"
echo "- Frontend:   http://localhost:$FRONTEND_PORT"
echo "- IK-Mat:     http://localhost:$FOOD_PORT"
echo "- IK-Alkohol: http://localhost:$ALCOHOL_PORT"
echo "- Logs:       $LOG_DIR/"
echo
echo "Press Ctrl+C to stop."

wait "$FOOD_PID" "$ALCOHOL_PID" "$FRONTEND_PID"
