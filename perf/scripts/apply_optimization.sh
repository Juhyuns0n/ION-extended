#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

require_server
baseline_json="${RESULTS_DIR}/baseline.json"
baseline_csv="${RESULTS_DIR}/baseline.csv"
baseline_explain="${RESULTS_DIR}/explain/baseline"

if [[ ! -s "${baseline_json}" || ! -s "${baseline_csv}" || ! -d "${baseline_explain}" ]]; then
    printf 'Refusing optimization: complete baseline artifacts do not exist.\n' >&2
    exit 1
fi
python3 - "${baseline_json}" <<'PY'
import json
import sys
result = json.load(open(sys.argv[1]))
assert result["phase"] == "baseline"
assert len(result["datasets"]) == 3
assert all(not item["composite_index_present"] for item in result["datasets"])
PY

for database in ion_perf_10k ion_perf_100k ion_perf_500k; do
    existing="$(mysql_exec "${database}" --execute="SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='workbook' AND index_name='idx_workbook_user_chapter_lesson'")"
    if [[ "${existing}" != "0" ]]; then
        printf 'Refusing optimization: composite index already exists in %s.\n' "${database}" >&2
        exit 1
    fi
done

for database in ion_perf_10k ion_perf_100k ion_perf_500k; do
    printf 'Adding composite index to %s ...\n' "${database}"
    mysql_exec "${database}" < "${PERF_DIR}/sql/add_composite_index.sql"
done

printf 'Composite index applied. The single-column user_id index remains for comparison.\n'
