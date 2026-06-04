#!/bin/sh
set -eu

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ORIGIN="$(pwd)"
trap 'cd "$ORIGIN"' EXIT

. "$ROOT/scripts/backend-prepare.sh"

if ! ensure_java_home; then
  echo "Java 21 JDK was not found. Install it or set JAVA_HOME before running this script." >&2
  exit 1
fi

ensure_backend_env
run_maven verify
