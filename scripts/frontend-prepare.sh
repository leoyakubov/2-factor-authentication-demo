#!/bin/sh
set -eu

prepare_frontend() {
  cd "$ROOT/frontend"

  [ -f .env ] || cp .env.example .env
  set -a
  . ./.env
  set +a

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

  if [ ! -d node_modules ]; then
    npm ci --no-audit --no-fund
  fi

  if [ ! -d node_modules/vite ] || [ ! -d node_modules/jest ] || ! rolldown_binding_ok; then
    shell_name="$(uname -s 2>/dev/null || echo unknown)"
    echo "Frontend dependencies are missing, incomplete, or were installed for a different shell/platform ($shell_name). Run 'cd frontend && npm ci' in the same environment you plan to use for the app (Git Bash, WSL, Linux, or macOS) and try again." >&2
    exit 1
  fi
}
