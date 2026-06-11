#!/bin/sh
set -eu

wait_for_backend() {
  api_base="${RENDER_API_BASE_URL:-https://api.render.com/v1}"
  api_key="${RENDER_API_KEY:?RENDER_API_KEY is required}"
  service_id="${RENDER_BACKEND_SERVICE_ID:?RENDER_BACKEND_SERVICE_ID is required}"
  baseline_deploy_id="${BASELINE_DEPLOY_ID:-}"
  attempt=1
  max_attempts="${RENDER_DEPLOY_WAIT_ATTEMPTS:-60}"
  delay_seconds="${RENDER_DEPLOY_WAIT_DELAY_SECONDS:-10}"

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail \
      --header "Authorization: Bearer $api_key" \
      --header "Accept: application/json" \
      "$api_base/services/$service_id/deploys")"; then
      latest_deploy_id="$(printf '%s' "$response" | jq -r '.[0].id // .deploys[0].id // empty')"
      status="$(printf '%s' "$response" | jq -r '.[0].status // .deploys[0].status // empty')"

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

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail \
      --header "Authorization: Bearer $api_key" \
      --header "Accept: application/json" \
      "$api_base/services/$service_id/deploys")"; then
      latest_deploy_id="$(printf '%s' "$response" | jq -r '.[0].id // .deploys[0].id // empty')"
      status="$(printf '%s' "$response" | jq -r '.[0].status // .deploys[0].status // empty')"

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
