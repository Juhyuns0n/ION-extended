#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_mysql_8
mkdir -p "${RUNTIME_DIR}" "${RESULTS_DIR}"

if "${MYSQLADMIN_BIN}" "${MYSQL_CONNECTION_ARGS[@]}" ping >/dev/null 2>&1; then
    printf 'Benchmark MySQL is already running on socket %s\n' "${MYSQL_SOCKET}"
    exit 0
fi

if [[ ! -d "${RUNTIME_DIR}/data/mysql" ]]; then
    "${MYSQLD_BIN}" --no-defaults --initialize-insecure \
        --basedir="${MYSQL_HOME_DIR}" \
        --datadir="${RUNTIME_DIR}/data"
fi

"${MYSQLD_BIN}" --no-defaults --daemonize \
    --basedir="${MYSQL_HOME_DIR}" \
    --datadir="${RUNTIME_DIR}/data" \
    --socket="${MYSQL_SOCKET}" \
    --pid-file="${RUNTIME_DIR}/mysql.pid" \
    --log-error="${RUNTIME_DIR}/mysql-error.log" \
    --port="${MYSQL_PORT}" \
    --bind-address=127.0.0.1 \
    --max-connections=40 \
    --innodb-buffer-pool-size=512M \
    --local-infile=ON \
    --mysqlx=OFF \
    --performance-schema=ON

for _ in $(seq 1 60); do
    if "${MYSQLADMIN_BIN}" "${MYSQL_CONNECTION_ARGS[@]}" ping >/dev/null 2>&1; then
        mysql_exec --execute="SELECT CONCAT('MySQL ', VERSION(), ' ready')"
        exit 0
    fi
    sleep 1
done

printf 'MySQL did not become ready; inspect %s\n' "${RUNTIME_DIR}/mysql-error.log" >&2
exit 1
