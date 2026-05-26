#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ORIGIN="$(pwd)"
trap 'cd "$ORIGIN"' EXIT

cd "$ROOT/frontend"
[ -f .env ] || cp .env.example .env
set -a
. ./.env
set +a
[ -d node_modules/vite ] || npm ci --no-audit --no-fund
[ -d node_modules/vite ] || {
  echo "vite is still missing after npm ci. Run 'cd frontend && npm install' and try again." >&2
  exit 1
}
npm start
