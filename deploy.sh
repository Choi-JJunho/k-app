#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TARGET_HOST="${TARGET_HOST:-homeamd}"
REMOTE_BASE_DIR="${REMOTE_BASE_DIR:-/opt/stacks/projects/k-app}"
REMOTE_SRC_DIR="${REMOTE_BASE_DIR}/src"
REMOTE_ENV_FILE="${REMOTE_ENV_FILE:-${REMOTE_BASE_DIR}/.env}"
SUPABASE_STACK_DIR="${SUPABASE_STACK_DIR:-/opt/stacks/supabase}"
SUPABASE_ENV_FILE="${SUPABASE_ENV_FILE:-${SUPABASE_STACK_DIR}/.env}"
DOCKER_NETWORK="${DOCKER_NETWORK:-}"
SUPABASE_POOLER_CONTAINER="${SUPABASE_POOLER_CONTAINER:-supabase-pooler}"
KMEAL_CONTAINER_NAME="${KMEAL_CONTAINER_NAME:-kmeal-scraping}"

DB_HOST_OVERRIDE="${DB_HOST_OVERRIDE:-}"
DB_PORT_OVERRIDE="${DB_PORT_OVERRIDE:-}"
DB_NAME_OVERRIDE="${DB_NAME_OVERRIDE:-}"
DB_USERNAME_OVERRIDE="${DB_USERNAME_OVERRIDE:-}"
DB_PASSWORD_OVERRIDE="${DB_PASSWORD_OVERRIDE:-}"
DB_SSLMODE_OVERRIDE="${DB_SSLMODE_OVERRIDE:-}"

APP_NAME="${APP_NAME:-k-app-api}"
CONTAINER_NAME="${CONTAINER_NAME:-k-app-api}"
IMAGE_NAME="${IMAGE_NAME:-k-app-api}"
IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d-%H%M%S)}"

HOST_PORT="${HOST_PORT:-9295}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
HEALTH_WAIT_SECONDS="${HEALTH_WAIT_SECONDS:-90}"

LOCAL_ENV_FILE="${LOCAL_ENV_FILE:-${ROOT_DIR}/deploy.homeamd.env}"
SKIP_TESTS=0

log() {
  printf '[deploy] %s\n' "$*"
}

fail() {
  printf '[deploy] ERROR: %s\n' "$*" >&2
  exit 1
}

usage() {
  cat <<'USAGE'
Usage: ./deploy.sh [-x test]

Options:
  -x test    Skip tests during image build.
  -h, --help Show this help message.
USAGE
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      -x)
        [[ $# -ge 2 ]] || fail "missing value for -x (supported: test)"
        if [[ "$2" == "test" ]]; then
          SKIP_TESTS=1
          shift 2
        else
          fail "unsupported -x value: $2 (supported: test)"
        fi
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        fail "unknown argument: $1"
        ;;
    esac
  done
}

need_local_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "local command not found: $1"
}

need_remote_cmd() {
  local cmd="$1"
  ssh "$TARGET_HOST" "command -v ${cmd} >/dev/null 2>&1" || fail "remote command not found on ${TARGET_HOST}: ${cmd}"
}

main() {
  parse_args "$@"

  need_local_cmd ssh
  need_local_cmd tar

  need_remote_cmd docker
  need_remote_cmd tar
  need_remote_cmd curl

  log "target=${TARGET_HOST} app=${APP_NAME} port=${HOST_PORT}"
  log "remote_base_dir=${REMOTE_BASE_DIR}"
  log "supabase_env_file=${SUPABASE_ENV_FILE}"
  log "supabase_pooler_container=${SUPABASE_POOLER_CONTAINER}"
  log "kmeal_container_name=${KMEAL_CONTAINER_NAME}"
  log "skip_tests=${SKIP_TESTS}"

  ssh "$TARGET_HOST" "mkdir -p '${REMOTE_BASE_DIR}'"

  if [[ -f "$LOCAL_ENV_FILE" ]]; then
    log "uploading local override env: ${LOCAL_ENV_FILE}"
    ssh "$TARGET_HOST" "cat > '${REMOTE_ENV_FILE}'" < "$LOCAL_ENV_FILE"
  else
    log "no local override env found; will use/create remote env file"
    ssh "$TARGET_HOST" "touch '${REMOTE_ENV_FILE}'"
  fi

  log "syncing source to ${TARGET_HOST}:${REMOTE_SRC_DIR}"
  ssh "$TARGET_HOST" "rm -rf '${REMOTE_SRC_DIR}' && mkdir -p '${REMOTE_SRC_DIR}'"

  COPYFILE_DISABLE=1 tar -czf - \
    --exclude='.git' \
    --exclude='.github' \
    --exclude='.gradle' \
    --exclude='.idea' \
    --exclude='.DS_Store' \
    --exclude='build' \
    --exclude='*/build' \
    --exclude='*/build/*' \
    -C "$ROOT_DIR" . \
    | ssh "$TARGET_HOST" "tar -xzf - -C '${REMOTE_SRC_DIR}'"

  log "building image and restarting container on remote host"
  ssh "$TARGET_HOST" \
    "APP_NAME='${APP_NAME}' CONTAINER_NAME='${CONTAINER_NAME}' IMAGE_NAME='${IMAGE_NAME}' IMAGE_TAG='${IMAGE_TAG}' HOST_PORT='${HOST_PORT}' CONTAINER_PORT='${CONTAINER_PORT}' SPRING_PROFILE='${SPRING_PROFILE}' REMOTE_SRC_DIR='${REMOTE_SRC_DIR}' REMOTE_ENV_FILE='${REMOTE_ENV_FILE}' SUPABASE_ENV_FILE='${SUPABASE_ENV_FILE}' DOCKER_NETWORK='${DOCKER_NETWORK}' SUPABASE_POOLER_CONTAINER='${SUPABASE_POOLER_CONTAINER}' KMEAL_CONTAINER_NAME='${KMEAL_CONTAINER_NAME}' DB_HOST_OVERRIDE='${DB_HOST_OVERRIDE}' DB_PORT_OVERRIDE='${DB_PORT_OVERRIDE}' DB_NAME_OVERRIDE='${DB_NAME_OVERRIDE}' DB_USERNAME_OVERRIDE='${DB_USERNAME_OVERRIDE}' DB_PASSWORD_OVERRIDE='${DB_PASSWORD_OVERRIDE}' DB_SSLMODE_OVERRIDE='${DB_SSLMODE_OVERRIDE}' HEALTH_PATH='${HEALTH_PATH}' HEALTH_WAIT_SECONDS='${HEALTH_WAIT_SECONDS}' SKIP_TESTS='${SKIP_TESTS}' bash -s" <<'REMOTE_SCRIPT'
set -Eeuo pipefail

read_env_key() {
  local env_file="$1"
  local key="$2"
  local value

  if [[ ! -f "$env_file" ]]; then
    printf ''
    return
  fi

  value="$(grep -E "^${key}=" "$env_file" | tail -n1 || true)"
  value="${value#${key}=}"
  value="${value#\"}"
  value="${value%\"}"
  value="${value#\'}"
  value="${value%\'}"
  printf '%s' "$value"
}

read_container_env() {
  local container_name="$1"
  local key="$2"
  local env_dump

  env_dump="$(docker inspect "$container_name" --format '{{range .Config.Env}}{{println .}}{{end}}' 2>/dev/null || true)"
  printf '%s\n' "$env_dump" \
    | awk -F= -v k="$key" '$1 == k { print substr($0, index($0, "=") + 1) }' \
    | tail -n1 \
    || true
}

set_env_key() {
  local env_file="$1"
  local key="$2"
  local value="$3"
  local tmp

  touch "$env_file"
  tmp="$(mktemp)"

  awk -v k="$key" -v v="$value" -F= '
    BEGIN { updated = 0 }
    $1 == k {
      print k "=" v
      updated = 1
      next
    }
    { print }
    END {
      if (updated == 0) {
        print k "=" v
      }
    }
  ' "$env_file" > "$tmp"

  mv "$tmp" "$env_file"
}

[[ -f "$SUPABASE_ENV_FILE" ]] || {
  echo "[remote] ERROR: supabase env file not found: ${SUPABASE_ENV_FILE}" >&2
  exit 1
}

pooler_tenant_id="$(read_env_key "$SUPABASE_ENV_FILE" POOLER_TENANT_ID)"
pooler_tenant_id="${pooler_tenant_id:-supabase}"
supabase_db_password="$(read_env_key "$SUPABASE_ENV_FILE" POSTGRES_PASSWORD)"
host_primary_ip="$(hostname -I | awk '{print $1}' | tr -d '[:space:]')"

kmeal_db_host="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_HOST)"
kmeal_db_port="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_PORT)"
kmeal_db_name="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_NAME)"
kmeal_db_user="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_USERNAME)"
kmeal_db_password="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_PASSWORD)"
kmeal_db_sslmode="$(read_container_env "$KMEAL_CONTAINER_NAME" DB_SSLMODE)"

db_host="${DB_HOST_OVERRIDE:-$kmeal_db_host}"
db_port="${DB_PORT_OVERRIDE:-$kmeal_db_port}"
db_name="${DB_NAME_OVERRIDE:-$kmeal_db_name}"
db_user="${DB_USERNAME_OVERRIDE:-$kmeal_db_user}"
db_password="${DB_PASSWORD_OVERRIDE:-$kmeal_db_password}"
db_sslmode="${DB_SSLMODE_OVERRIDE:-$kmeal_db_sslmode}"

if [[ "$db_host" == "$host_primary_ip" || "$db_host" == "127.0.0.1" || "$db_host" == "localhost" ]]; then
  db_host="supabase-pooler"
fi

db_host="${db_host:-supabase-pooler}"
db_port="${db_port:-5432}"
db_name="${db_name:-kapp}"
db_user="${db_user:-postgres.${pooler_tenant_id}}"
db_password="${db_password:-$supabase_db_password}"
db_sslmode="${db_sslmode:-disable}"

if [[ -z "$db_password" ]]; then
  echo "[remote] ERROR: DB password is missing (kmeal env and supabase env did not provide it)" >&2
  exit 1
fi

jwt_secret="$(read_env_key "$REMOTE_ENV_FILE" KAPP_JWT_SECRET)"
if [[ -z "$jwt_secret" ]]; then
  jwt_secret="$(read_env_key "$SUPABASE_ENV_FILE" JWT_SECRET)"
fi

if [[ -z "$jwt_secret" ]]; then
  echo "[remote] ERROR: KAPP_JWT_SECRET missing (set in ${REMOTE_ENV_FILE} or ensure JWT_SECRET exists in ${SUPABASE_ENV_FILE})" >&2
  exit 1
fi

jwt_exp_days="$(read_env_key "$REMOTE_ENV_FILE" KAPP_JWT_ACCESS_TOKEN_EXPIRE_DAYS)"
jwt_exp_days="${jwt_exp_days:-7}"

if [[ -z "${DOCKER_NETWORK:-}" ]]; then
  DOCKER_NETWORK="$(docker inspect "$SUPABASE_POOLER_CONTAINER" --format '{{range $name, $cfg := .NetworkSettings.Networks}}{{println $name}}{{end}}' 2>/dev/null | head -n1 | tr -d '[:space:]')"
fi
DOCKER_NETWORK="${DOCKER_NETWORK:-supabase_default}"

if ! docker network inspect "$DOCKER_NETWORK" >/dev/null 2>&1; then
  echo "[remote] ERROR: docker network not found: ${DOCKER_NETWORK}" >&2
  exit 1
fi

if ! docker run --rm --network "$DOCKER_NETWORK" -e "PGPASSWORD=${db_password}" postgres:15-alpine \
  psql -h "$db_host" -p "$db_port" -U "$db_user" -d "$db_name" -Atc "select 1" >/dev/null 2>&1; then
  echo "[remote] ERROR: cannot connect to database (${db_host}:${db_port}/${db_name} as ${db_user})" >&2
  exit 1
fi

db_url="jdbc:postgresql://${db_host}:${db_port}/${db_name}?sslmode=${db_sslmode}"

set_env_key "$REMOTE_ENV_FILE" KAPP_DB_URL "$db_url"
set_env_key "$REMOTE_ENV_FILE" KAPP_DB_USERNAME "$db_user"
set_env_key "$REMOTE_ENV_FILE" KAPP_DB_PASSWORD "$db_password"
set_env_key "$REMOTE_ENV_FILE" KAPP_JWT_SECRET "$jwt_secret"
set_env_key "$REMOTE_ENV_FILE" KAPP_JWT_ACCESS_TOKEN_EXPIRE_DAYS "$jwt_exp_days"
chmod 600 "$REMOTE_ENV_FILE"

port_owner="$(docker ps --format '{{.Names}}|{{.Ports}}' | awk -F'|' -v p="$HOST_PORT" '$2 ~ (":" p "->") {print $1; exit}')"
if [[ -n "$port_owner" && "$port_owner" != "$CONTAINER_NAME" ]]; then
  echo "[remote] ERROR: host port ${HOST_PORT} is already used by ${port_owner}" >&2
  exit 1
fi

cd "$REMOTE_SRC_DIR"
docker_build_args=(--pull)
if [[ "${SKIP_TESTS:-0}" == "1" ]]; then
  docker_build_args+=(--build-arg "GRADLE_BUILD_ARGS=-x test")
fi

docker build "${docker_build_args[@]}" -t "${IMAGE_NAME}:${IMAGE_TAG}" -t "${IMAGE_NAME}:latest" .

if docker ps -a --format '{{.Names}}' | grep -Fxq "$CONTAINER_NAME"; then
  docker rm -f "$CONTAINER_NAME" >/dev/null
fi

docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --network "$DOCKER_NETWORK" \
  --env-file "$REMOTE_ENV_FILE" \
  -e "SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}" \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  "${IMAGE_NAME}:${IMAGE_TAG}" >/dev/null

health_url="http://127.0.0.1:${HOST_PORT}${HEALTH_PATH}"
deadline=$((SECONDS + HEALTH_WAIT_SECONDS))
healthy=0

while (( SECONDS < deadline )); do
  status_code="$(curl -sS -o /tmp/${APP_NAME}.health -w '%{http_code}' "$health_url" || true)"
  if [[ "$status_code" == "200" ]]; then
    healthy=1
    break
  fi
  sleep 2
done

if (( healthy == 0 )); then
  echo "[remote] ERROR: health check failed: ${health_url}" >&2
  docker logs --tail 120 "$CONTAINER_NAME" >&2 || true
  exit 1
fi

docker ps --filter "name=^/${CONTAINER_NAME}$" --format 'NAME={{.Names}} IMAGE={{.Image}} STATUS={{.Status}} PORTS={{.Ports}}'
echo "HEALTH_URL=${health_url}"
echo "DB_URL=${db_url}"
echo "DB_USERNAME=${db_user}"
echo "DB_HOST=${db_host}"
echo "DOCKER_NETWORK=${DOCKER_NETWORK}"
REMOTE_SCRIPT

  log "deployment complete"
  log "service url: http://${TARGET_HOST}:${HOST_PORT}"
}

main "$@"
