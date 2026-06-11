#!/bin/sh
set -eu

prepare_frontend() {
  cd "$ROOT/frontend"

  [ -f .env ] || cp .env.example .env
  set -a
  . ./.env
  set +a

  if [ ! -f src/testSetup.js ] || [ ! -f scripts/test-ci.cjs ]; then
    echo "Missing frontend test setup files. Restore src/testSetup.js and scripts/test-ci.cjs, then try again." >&2
    exit 1
  fi

  shell_name="$(uname -s 2>/dev/null || echo unknown)"
  machine_name="$(uname -m 2>/dev/null || echo unknown)"
  platform_key="$shell_name:$machine_name"

  case "$platform_key" in
    Linux:x86_64)
      rolldown_binding_ok() {
        [ -d node_modules/@rolldown/binding-linux-x64-gnu ] || [ -f node_modules/rolldown/rolldown-binding.linux-x64-gnu.node ]
      }
      ;;
    Linux:aarch64|Linux:arm64)
      rolldown_binding_ok() {
        [ -d node_modules/@rolldown/binding-linux-arm64-gnu ] || [ -f node_modules/rolldown/rolldown-binding.linux-arm64-gnu.node ]
      }
      ;;
    MINGW*:x86_64|MSYS*:x86_64|CYGWIN*:x86_64)
      rolldown_binding_ok() {
        [ -d node_modules/@rolldown/binding-win32-x64-msvc ] || [ -f node_modules/rolldown/rolldown-binding.win32-x64-msvc.node ]
      }
      ;;
    *)
      rolldown_binding_ok() {
        find node_modules -name 'rolldown-binding.*.node' -print -quit 2>/dev/null | grep -q .
      }
      ;;
  esac

  if [ ! -d node_modules ]; then
    npm ci --no-audit --no-fund
  fi

  if [ ! -d node_modules/vite ] || [ ! -d node_modules/jest ] || ! rolldown_binding_ok; then
    echo "Frontend dependencies are missing, incomplete, or were installed for a different shell/platform ($platform_key)." >&2
    echo "" >&2
    echo "node_modules contains native packages and cannot be shared safely between Windows, Git Bash, WSL, Linux, and macOS." >&2
    echo "" >&2
    case "$shell_name:$(pwd)" in
      Linux:/mnt/*)
        echo "You are running from WSL inside the Windows filesystem (/mnt/...). If npm previously installed Windows-native packages here, WSL may fail with EIO when trying to replace them." >&2
        echo "" >&2
        echo "Recommended options:" >&2
        echo "1. Best: clone or move the repo into the WSL filesystem, for example ~/projects/2-factor-authentication-demo, then run ./scripts/frontend-verify.sh there." >&2
        echo "2. If you want to keep the repo on C:, delete frontend/node_modules from Windows File Explorer or PowerShell, then run 'cd frontend && npm ci' from WSL." >&2
        ;;
      *)
        echo "Delete frontend/node_modules, then run 'cd frontend && npm ci' in the same environment you plan to use for the app." >&2
        ;;
    esac
    exit 1
  fi
}
