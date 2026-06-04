#!/bin/sh
set -eu

ensure_java_home() {
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    if java_supports_project "$JAVA_HOME/bin/java"; then
      return 0
    fi
  fi

  case "$(uname -s 2>/dev/null || echo unknown)" in
    Linux*)
      for candidate in /usr/lib/jvm/default-java /usr/lib/jvm/* /usr/lib/jvm/*/*; do
        if [ -x "$candidate/bin/java" ]; then
          JAVA_HOME="$candidate"
          export JAVA_HOME
          if java_supports_project "$JAVA_HOME/bin/java"; then
            return 0
          fi
        fi
      done
      ;;
    Darwin*)
      if command -v /usr/libexec/java_home >/dev/null 2>&1; then
        JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
        if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
          export JAVA_HOME
          if java_supports_project "$JAVA_HOME/bin/java"; then
            return 0
          fi
        fi
      fi
      ;;
  esac

  if command -v java >/dev/null 2>&1; then
    JAVA_BIN="$(command -v java)"
    case "$JAVA_BIN" in
      /mnt/*)
        return 1
        ;;
      *)
        if [ -x "$JAVA_BIN" ]; then
          JAVA_HOME="$(cd "$(dirname "$JAVA_BIN")/.." && pwd)"
          if [ -x "$JAVA_HOME/bin/java" ]; then
            export JAVA_HOME
            if java_supports_project "$JAVA_HOME/bin/java"; then
              return 0
            fi
          fi
        fi
        ;;
    esac
  fi

  return 1
}

java_supports_project() {
  java_binary="$1"
  java_version_output="$("$java_binary" -version 2>&1 | head -n 1 || true)"
  java_major_version="$(printf '%s\n' "$java_version_output" | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p')"

  if [ -z "$java_major_version" ]; then
    return 1
  fi

  if [ "$java_major_version" -lt 21 ]; then
    return 1
  fi

  return 0
}

ensure_backend_env() {
  cd "$ROOT/backend"

  [ -f .env ] || cp .env.example .env
}

run_maven() {
  if command -v mvn >/dev/null 2>&1; then
    mvn "$@"
  else
    bash ./mvnw "$@"
  fi
}
