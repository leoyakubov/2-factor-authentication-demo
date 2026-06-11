#!/bin/sh
set -eu

backend_smoke() {
  url="${RENDER_BACKEND_URL:?RENDER_BACKEND_URL is required}"
  attempt=1

  while [ "$attempt" -le 10 ]; do
    if response="$(curl --silent --show-error --fail "$url/actuator/health")"; then
      case "$response" in
        *'"status":"UP"'*) echo "Backend smoke passed for $url"; return 0 ;;
      esac
    fi

    attempt=$((attempt + 1))
    sleep 10
  done

  echo "Backend smoke failed for $url" >&2
  exit 1
}

frontend_smoke() {
  url="${RENDER_FRONTEND_URL:?RENDER_FRONTEND_URL is required}"
  attempt=1

  while [ "$attempt" -le 10 ]; do
    if response="$(curl --silent --show-error --fail "$url")"; then
      case "$response" in
        *"Two Factor Demo"*) echo "Frontend smoke passed for $url"; return 0 ;;
      esac
    fi

    attempt=$((attempt + 1))
    sleep 10
  done

  echo "Frontend smoke failed for $url" >&2
  exit 1
}

case "${SMOKE_CHECK:?SMOKE_CHECK is required}" in
  backend) backend_smoke ;;
  frontend) frontend_smoke ;;
  *)
    echo "Unknown SMOKE_CHECK value: ${SMOKE_CHECK}" >&2
    exit 1
    ;;
esac
