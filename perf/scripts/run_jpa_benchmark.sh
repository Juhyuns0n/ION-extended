#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

phase="${1:-}"
if [[ "${phase}" != "baseline" && "${phase}" != "optimized" ]]; then
    printf 'Usage: %s baseline|optimized\n' "$0" >&2
    exit 2
fi
require_server

output="${RESULTS_DIR}/jpa_${phase}.json"
mkdir -p "${RESULTS_DIR}"

ION_PERF_ENABLED=true \
ION_PERF_USERS=17857 \
ION_PERF_PHASE="${phase}" \
ION_PERF_OUTPUT="${output}" \
SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/ion_perf_500k?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD= \
SPRING_JPA_HIBERNATE_DDL_AUTO=none \
bash "${PERF_DIR}/../backend/gradlew" \
    -p "${PERF_DIR}/../backend" \
    test --tests capstone.workbook.WorkbookRepositoryPerformanceTest --no-daemon --rerun-tasks

test -s "${output}"
printf 'Saved %s\n' "${output}"
