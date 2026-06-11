#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ORIGIN="$(pwd)"
trap 'cd "$ORIGIN"' EXIT

. "$ROOT/scripts/frontend-prepare.sh"
prepare_frontend
npm_build_out_dir="$(mktemp -d)"
trap 'rm -rf "$npm_build_out_dir"' EXIT
export VITE_BUILD_OUT_DIR="$npm_build_out_dir"
npm run verify
