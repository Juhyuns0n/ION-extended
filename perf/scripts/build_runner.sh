#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_mysql_8
mkdir -p "${RUNTIME_DIR}/bin"
DEPENDENCY_LIB_DIR="${ION_PERF_DEPENDENCY_LIB_DIR:-/opt/homebrew/lib}"

"${MYSQL_HOME_DIR}/bin/mysql_config" --cflags --libs > "${RUNTIME_DIR}/mysql-build-flags.txt"
# mysql_config output is intentionally word-split into compiler arguments.
# shellcheck disable=SC2046
cc -O2 -Wall -Wextra -Werror \
    $("${MYSQL_HOME_DIR}/bin/mysql_config" --cflags) \
    "${PERF_DIR}/src/workbook_benchmark.c" \
    -o "${RUNTIME_DIR}/bin/workbook_benchmark" \
    -L"${DEPENDENCY_LIB_DIR}" \
    $("${MYSQL_HOME_DIR}/bin/mysql_config" --libs) \
    -lm

printf 'Built %s\n' "${RUNTIME_DIR}/bin/workbook_benchmark"
