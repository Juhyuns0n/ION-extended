#!/usr/bin/env bash
set -euo pipefail

PERF_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="${PERF_DIR}/.runtime"
RESULTS_DIR="${PERF_DIR}/results"
MYSQL_HOME_DIR="${MYSQL_HOME_DIR:-/opt/homebrew/opt/mysql@8.4}"
MYSQL_BIN="${MYSQL_HOME_DIR}/bin/mysql"
MYSQLADMIN_BIN="${MYSQL_HOME_DIR}/bin/mysqladmin"
MYSQLD_BIN="${MYSQL_HOME_DIR}/bin/mysqld"
MYSQL_SOCKET="${RUNTIME_DIR}/mysql.sock"
MYSQL_PORT="${ION_PERF_MYSQL_PORT:-3407}"
MYSQL_CONNECTION_ARGS=(--protocol=socket --socket="${MYSQL_SOCKET}" --user=root)
MYSQL_ARGS=("${MYSQL_CONNECTION_ARGS[@]}" --skip-column-names)

require_mysql_8() {
    if [[ ! -x "${MYSQLD_BIN}" ]]; then
        printf 'MySQL 8 executable not found at %s\n' "${MYSQLD_BIN}" >&2
        printf 'Set MYSQL_HOME_DIR to a MySQL 8 installation prefix.\n' >&2
        exit 1
    fi
    local version
    version="$(${MYSQLD_BIN} --version)"
    if [[ "${version}" != *"Ver 8."* ]]; then
        printf 'Expected MySQL 8, found: %s\n' "${version}" >&2
        exit 1
    fi
}

require_server() {
    if ! "${MYSQLADMIN_BIN}" "${MYSQL_CONNECTION_ARGS[@]}" ping >/dev/null 2>&1; then
        printf 'Isolated benchmark MySQL is not running. Run perf/scripts/start_mysql.sh first.\n' >&2
        exit 1
    fi
}

mysql_exec() {
    "${MYSQL_BIN}" "${MYSQL_ARGS[@]}" "$@"
}
