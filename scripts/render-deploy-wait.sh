#!/bin/sh
set -eu

wait_for_backend() {
  url="${RENDER_BACKEND_URL:?RENDER_BACKEND_URL is required}"
  attempt=1
  max_attempts="${RENDER_DEPLOY_WAIT_ATTEMPTS:-60}"
  delay_seconds="${RENDER_DEPLOY_WAIT_DELAY_SECONDS:-10}"

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail "$url/actuator/health")"; then
      case "$response" in
        *'"status":"UP"'*)
          echo "Backend deployment is ready at $url"
          return 0
          ;;
      esac
    fi

    echo "Waiting for backend deployment to become ready at $url (attempt $attempt/$max_attempts)..."
    attempt=$((attempt + 1))
    sleep "$delay_seconds"
  done

  echo "Backend deployment did not become ready at $url" >&2
  exit 1
}

wait_for_frontend() {
  url="${RENDER_FRONTEND_URL:?RENDER_FRONTEND_URL is required}"
  attempt=1
  max_attempts="${RENDER_DEPLOY_WAIT_ATTEMPTS:-60}"
  delay_seconds="${RENDER_DEPLOY_WAIT_DELAY_SECONDS:-10}"

  while [ "$attempt" -le "$max_attempts" ]; do
    if response="$(curl --silent --show-error --fail "$url")"; then
      case "$response" in
        *"Two Factor Demo"*)
          echo "Frontend deployment is ready at $url"
          return 0
          ;;
      esac
    fi

    echo "Waiting for frontend deployment to become ready at $url (attempt $attempt/$max_attempts)..."
    attempt=$((attempt + 1))
    sleep "$delay_seconds"
  done

  echo "Frontend deployment did not become ready at $url" >&2
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
