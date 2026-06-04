#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ORIGIN="$(pwd)"
trap 'cd "$ORIGIN"' EXIT

. "$ROOT/scripts/frontend-prepare.sh"
prepare_frontend
npm run test:ci
