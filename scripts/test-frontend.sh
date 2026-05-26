#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ORIGIN="$(pwd)"
trap 'cd "$ORIGIN"' EXIT

cd "$ROOT/frontend"
[ -f .env ] || cp .env.example .env
[ -d node_modules ] || npm install
CI=true npm test -- --watchAll=false
