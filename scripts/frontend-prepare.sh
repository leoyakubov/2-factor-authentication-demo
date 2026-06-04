#!/bin/sh
set -eu

prepare_frontend() {
  cd "$ROOT/frontend"

  [ -f .env ] || cp .env.example .env
  set -a
  . ./.env
  set +a

  platform_key="$(uname -s 2>/dev/null || echo unknown)-$(uname -m 2>/dev/null || echo unknown)"
  platform_stamp=".node_modules-platform"

  if [ ! -f test/setupTests.cjs ]; then
    mkdir -p test
    cat > test/setupTests.cjs <<'EOF'
require("@testing-library/jest-dom");
const { TextDecoder, TextEncoder } = require("node:util");

jest.mock("antd");

if (typeof global.TextEncoder === "undefined") {
  global.TextEncoder = TextEncoder;
}

if (typeof global.TextDecoder === "undefined") {
  global.TextDecoder = TextDecoder;
}

const createMatchMedia = (query) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: jest.fn(),
  removeListener: jest.fn(),
  addEventListener: jest.fn(),
  removeEventListener: jest.fn(),
  dispatchEvent: jest.fn(),
});

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: jest.fn().mockImplementation(createMatchMedia),
});

class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

Object.defineProperty(window, "ResizeObserver", {
  writable: true,
  value: ResizeObserver,
});
EOF
  fi

  needs_install=0

  case "$(uname -s):$(uname -m)" in
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

  if [ ! -d node_modules ] || [ ! -d node_modules/vite ] || [ ! -d node_modules/jest ]; then
    needs_install=1
  fi

  if ! rolldown_binding_ok; then
    needs_install=1
  fi

  if [ -f "$platform_stamp" ]; then
    installed_platform="$(cat "$platform_stamp")"
    if [ "$installed_platform" != "$platform_key" ]; then
      needs_install=1
    fi
  else
    needs_install=1
  fi

  if [ "$needs_install" -eq 1 ]; then
    if [ -d node_modules ]; then
      remove_node_modules_tree
    fi
    npm ci --no-audit --no-fund
    printf '%s\n' "$platform_key" > "$platform_stamp"
  fi

  if [ ! -d node_modules/vite ] || [ ! -d node_modules/jest ] || ! rolldown_binding_ok; then
    echo "Frontend dependencies are incomplete after npm ci. Run 'cd frontend && npm install' and try again." >&2
    exit 1
  fi
}

remove_node_modules_tree() {
  if command -v wslpath >/dev/null 2>&1 && command -v powershell.exe >/dev/null 2>&1; then
    windows_frontend_dir="$(wslpath -w "$PWD")"
    powershell.exe -NoProfile -NonInteractive -Command "Set-Location -LiteralPath '$windows_frontend_dir'; Remove-Item -LiteralPath 'node_modules' -Recurse -Force -ErrorAction Stop" >/dev/null 2>&1 || rm -rf node_modules
    return 0
  fi

  if command -v cygpath >/dev/null 2>&1 && command -v cmd.exe >/dev/null 2>&1; then
    windows_frontend_dir="$(cygpath -w "$PWD")"
    cmd.exe /c "cd /d \"$windows_frontend_dir\" && rmdir /s /q node_modules" >/dev/null 2>&1 || rm -rf node_modules
    return 0
  fi

  rm -rf node_modules
}
