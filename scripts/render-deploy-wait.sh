#!/bin/sh
set -eu

if [ -f "$(dirname "$0")/.env" ]; then
  load_local_env() {
    while IFS='=' read -r key value; do
      case "$key" in
        ''|\#*) continue ;;
      esac

      eval "current=\${$key-}"
      if [ -z "$current" ]; then
        eval "$key=$value"
        export "$key"
      fi
    done < "$(dirname "$0")/.env"
  }

  load_local_env
fi

wait_for_backend() {
  api_base="${RENDER_API_BASE_URL:-https://api.render.com/v1}"
  api_key="${RENDER_API_KEY:?RENDER_API_KEY is required}"
  service_id="${RENDER_BACKEND_SERVICE_ID:?RENDER_BACKEND_SERVICE_ID is required}"
  baseline_deploy_id="${BASELINE_DEPLOY_ID:-}"
  attempt=1
  max_attempts="${RENDER_DEPLOY_WAIT_ATTEMPTS:-60}"
  delay_seconds="${RENDER_DEPLOY_WAIT_DELAY_SECONDS:-10}"

  case "$service_id" in
    http://*|https://*)
      echo "RENDER_BACKEND_SERVICE_ID must be the Render service ID (for example srv-...), not the public URL." >&2
      exit 1
      ;;
  esac

  latest_deploy_id_from_response() {
    printf '%s' "$1" | jq -r '
      def deploy_list:
        if type == "array" then .
        elif type == "object" then
          (.deploys // .items // .data // .result // .deployments // .service.deploys // .service.items // [])
        else [] end;

      deploy_list
      | map(select(type == "object"))
      | map(if has("deploy") and (.deploy | type == "object") then .deploy else . end)
      | map(.id // .deployId // empty)
      | map(select(length > 0))
      | .[0] // empty
    '
  }

  latest_deploy_status_from_response() {
    printf '%s' "$1" | jq -r '
      def deploy_list:
        if type == "array" then .
        elif type == "object" then
          (.deploys // .items // .data // .result // .deployments // .service.deploys // .service.items // [])
        else [] end;

      deploy_list
      | map(select(type == "object"))
      | map(if has("deploy") and (.deploy | type == "object") then .deploy else . end)
      | map(.status // .statusText // empty)
      | map(select(length > 0))
      | .[0] // empty
    '
  }

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail \
      --header "Authorization: Bearer $api_key" \
      --header "Accept: application/json" \
      "$api_base/services/$service_id/deploys")"; then
      latest_deploy_id="$(latest_deploy_id_from_response "$response")"
      status="$(latest_deploy_status_from_response "$response")"

      if [ -z "$latest_deploy_id" ]; then
        echo "Backend Render deploy response did not include a deploy id"
      elif [ -n "$baseline_deploy_id" ] && [ "$latest_deploy_id" = "$baseline_deploy_id" ]; then
        echo "Backend Render latest deploy is still the previous one ($latest_deploy_id, status ${status:-unknown})"
      else
        case "$status" in
          live)
            echo "Backend deployment is live on Render ($latest_deploy_id)"
            return 0
            ;;
          failed|canceled|deactivated)
            echo "Backend deployment ended with status '$status' ($latest_deploy_id)" >&2
            exit 1
            ;;
          *)
            echo "Backend Render deploy status: ${status:-unknown} ($latest_deploy_id)"
            ;;
        esac
      fi
    else
      echo "Waiting for backend deployment status from Render (attempt $attempt/$max_attempts)..."
    fi

    attempt=$((attempt + 1))
    sleep "$delay_seconds"
  done

  echo "Backend deployment did not become live on Render" >&2
  exit 1
}

wait_for_frontend() {
  api_base="${RENDER_API_BASE_URL:-https://api.render.com/v1}"
  api_key="${RENDER_API_KEY:?RENDER_API_KEY is required}"
  service_id="${RENDER_FRONTEND_SERVICE_ID:?RENDER_FRONTEND_SERVICE_ID is required}"
  baseline_deploy_id="${BASELINE_DEPLOY_ID:-}"
  attempt=1
  max_attempts="${RENDER_DEPLOY_WAIT_ATTEMPTS:-60}"
  delay_seconds="${RENDER_DEPLOY_WAIT_DELAY_SECONDS:-10}"

  case "$service_id" in
    http://*|https://*)
      echo "RENDER_FRONTEND_SERVICE_ID must be the Render service ID (for example srv-...), not the public URL." >&2
      exit 1
      ;;
  esac
 
  latest_deploy_id_from_response() {
    printf '%s' "$1" | jq -r '
      def deploy_list:
        if type == "array" then .
        elif type == "object" then
          (.deploys // .items // .data // .result // .deployments // .service.deploys // .service.items // [])
        else [] end;

      deploy_list
      | map(select(type == "object"))
      | map(if has("deploy") and (.deploy | type == "object") then .deploy else . end)
      | map(.id // .deployId // empty)
      | map(select(length > 0))
      | .[0] // empty
    '
  }

  latest_deploy_status_from_response() {
    printf '%s' "$1" | jq -r '
      def deploy_list:
        if type == "array" then .
        elif type == "object" then
          (.deploys // .items // .data // .result // .deployments // .service.deploys // .service.items // [])
        else [] end;

      deploy_list
      | map(select(type == "object"))
      | map(if has("deploy") and (.deploy | type == "object") then .deploy else . end)
      | map(.status // .statusText // empty)
      | map(select(length > 0))
      | .[0] // empty
    '
  }

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail \
      --header "Authorization: Bearer $api_key" \
      --header "Accept: application/json" \
      "$api_base/services/$service_id/deploys")"; then
      latest_deploy_id="$(latest_deploy_id_from_response "$response")"
      status="$(latest_deploy_status_from_response "$response")"

      if [ -z "$latest_deploy_id" ]; then
        echo "Frontend Render deploy response did not include a deploy id"
      elif [ -n "$baseline_deploy_id" ] && [ "$latest_deploy_id" = "$baseline_deploy_id" ]; then
        echo "Frontend Render latest deploy is still the previous one ($latest_deploy_id, status ${status:-unknown})"
      else
        case "$status" in
          live)
            echo "Frontend deployment is live on Render ($latest_deploy_id)"
            return 0
            ;;
          failed|canceled|deactivated)
            echo "Frontend deployment ended with status '$status' ($latest_deploy_id)" >&2
            exit 1
            ;;
          *)
            echo "Frontend Render deploy status: ${status:-unknown} ($latest_deploy_id)"
            ;;
        esac
      fi
    else
      echo "Waiting for frontend deployment status from Render (attempt $attempt/$max_attempts)..."
    fi

    attempt=$((attempt + 1))
    sleep "$delay_seconds"
  done

  echo "Frontend deployment did not become live on Render" >&2
  exit 1
}

case "${DEPLOY_CHECK:?DEPLOY_CHECK is required}" in
  backend) wait_for_backend ;;
  frontend) wait_for_frontend ;;
  *)
    echo "Unknown DEPLOY_CHECK value: ${DEPLOY_CHECK}" >&2
    exit 1
    ;;
esac
