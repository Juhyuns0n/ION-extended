#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_server

# Exact multiples of the application's 28 workbook rows per user.
DATASETS=("10k:ion_perf_10k:9996" "100k:ion_perf_100k:99988" "500k:ion_perf_500k:499996")

for spec in "${DATASETS[@]}"; do
    IFS=: read -r label database row_count <<<"${spec}"
    printf 'Rebuilding %s baseline (%s rows) ...\n' "${label}" "${row_count}"
    mysql_exec --execute="DROP DATABASE IF EXISTS ${database}; CREATE DATABASE ${database} CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    mysql_exec "${database}" < "${PERF_DIR}/sql/baseline_schema.sql"
    {
        printf 'SET @target_rows = %s;\n' "${row_count}"
        sed -n '1,$p' "${PERF_DIR}/sql/generate_workbook.sql"
    } | mysql_exec "${database}"
    actual="$(mysql_exec "${database}" --execute='SELECT COUNT(*) FROM workbook')"
    if [[ "${actual}" != "${row_count}" ]]; then
        printf 'Expected %s rows, found %s in %s\n' "${row_count}" "${actual}" "${database}" >&2
        exit 1
    fi
done

printf 'All deterministic baseline datasets are ready.\n'
