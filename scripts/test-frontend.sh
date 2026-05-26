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
[ -d node_modules/jest ] || npm ci --no-audit --no-fund
[ -d node_modules/jest ] || {
  echo "jest is still missing after npm ci. Run 'cd frontend && npm install' and try again." >&2
  exit 1
}
npm run test:ci
