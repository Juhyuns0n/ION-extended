#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

if "${MYSQLADMIN_BIN}" "${MYSQL_CONNECTION_ARGS[@]}" ping >/dev/null 2>&1; then
    "${MYSQLADMIN_BIN}" "${MYSQL_CONNECTION_ARGS[@]}" shutdown
    printf 'Benchmark MySQL stopped. Runtime data remains in %s\n' "${RUNTIME_DIR}"
else
    printf 'Benchmark MySQL is not running.\n'
fi
