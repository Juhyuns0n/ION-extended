#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"

phase="${1:-}"
if [[ "${phase}" != "baseline" && "${phase}" != "optimized" ]]; then
    printf 'Usage: %s baseline|optimized\n' "$0" >&2
    exit 2
fi

require_server
if [[ ! -x "${RUNTIME_DIR}/bin/workbook_benchmark" ]]; then
    "${PERF_DIR}/scripts/build_runner.sh"
fi

warmup="${ION_PERF_WARMUP:-500}"
iterations="${ION_PERF_ITERATIONS:-5000}"
DATASETS=("10k:ion_perf_10k:9996" "100k:ion_perf_100k:99988" "500k:ion_perf_500k:499996")
phase_runtime="${RUNTIME_DIR}/results/${phase}"
explain_root="${RESULTS_DIR}/explain/${phase}"
mkdir -p "${phase_runtime}" "${explain_root}"
json_inputs=()

for spec in "${DATASETS[@]}"; do
    IFS=: read -r label database row_count <<<"${spec}"
    output="${phase_runtime}/${label}.json"
    explain_dir="${explain_root}/${label}"
    mkdir -p "${explain_dir}"
    "${RUNTIME_DIR}/bin/workbook_benchmark" \
        --socket "${MYSQL_SOCKET}" \
        --database "${database}" \
        --phase "${phase}" \
        --dataset-label "${label}" \
        --rows "${row_count}" \
        --warmup "${warmup}" \
        --iterations "${iterations}" \
        --json "${output}" \
        --explain-dir "${explain_dir}"
    json_inputs+=("${output}")
done

python3 "${PERF_DIR}/scripts/combine_results.py" \
    "${phase}" "${RESULTS_DIR}/${phase}.json" "${json_inputs[@]}"
printf 'Saved %s and %s\n' "${RESULTS_DIR}/${phase}.json" "${RESULTS_DIR}/${phase}.csv"
