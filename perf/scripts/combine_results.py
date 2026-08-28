#!/usr/bin/env python3
import csv
import json
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) < 4:
        raise SystemExit("usage: combine_results.py PHASE OUTPUT_JSON INPUT_JSON...")
    phase = sys.argv[1]
    output_json = Path(sys.argv[2])
    inputs = [Path(path) for path in sys.argv[3:]]
    datasets = [json.loads(path.read_text()) for path in inputs]
    if any(item["phase"] != phase for item in datasets):
        raise SystemExit("phase mismatch while combining benchmark results")

    combined = {
        "phase": phase,
        "benchmark_kind": "single-client raw SQL including result transfer",
        "datasets": datasets,
    }
    output_json.write_text(json.dumps(combined, indent=2) + "\n")

    output_csv = output_json.with_suffix(".csv")
    with output_csv.open("w", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow([
            "phase", "dataset", "rows", "workload", "samples", "mean_ms",
            "p50_ms", "p95_ms", "p99_ms", "ops_per_second", "rows_returned",
        ])
        for dataset in datasets:
            for workload in dataset["workloads"]:
                writer.writerow([
                    phase, dataset["dataset"], dataset["rows"], workload["name"],
                    workload["samples"], workload["mean_ms"], workload["p50_ms"],
                    workload["p95_ms"], workload["p99_ms"],
                    workload["ops_per_second"], workload["rows_returned"],
                ])


if __name__ == "__main__":
    main()
