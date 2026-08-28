#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"
require_server

output="${RESULTS_DIR}/hibernate_sql.txt"
ION_PERF_ENABLED=true \
ION_PERF_SQL_OUTPUT="${output}" \
SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/ion_perf_500k?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD= \
SPRING_JPA_HIBERNATE_DDL_AUTO=none \
bash "${PERF_DIR}/../backend/gradlew" \
    -p "${PERF_DIR}/../backend" \
    test --tests capstone.workbook.WorkbookRepositorySqlShapeTest --no-daemon --rerun-tasks

test -s "${output}"
printf 'Saved %s\n' "${output}"
