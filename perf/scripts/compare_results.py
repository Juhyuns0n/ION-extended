#!/usr/bin/env python3
import csv
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RESULTS = ROOT / "results"


def indexed(result: dict) -> dict:
    return {
        (dataset["dataset"], workload["name"]): workload
        for dataset in result["datasets"]
        for workload in dataset["workloads"]
    }


def improvement(before: float, after: float) -> float:
    return (before - after) / before * 100.0


baseline = json.loads((RESULTS / "baseline.json").read_text())
optimized = json.loads((RESULTS / "optimized.json").read_text())
before_by_key = indexed(baseline)
after_by_key = indexed(optimized)

rows = []
for key in sorted(before_by_key):
    before = before_by_key[key]
    after = after_by_key[key]
    rows.append({
        "dataset": key[0],
        "workload": key[1],
        "baseline_p50_ms": before["p50_ms"],
        "optimized_p50_ms": after["p50_ms"],
        "p50_improvement_percent": improvement(before["p50_ms"], after["p50_ms"]),
        "baseline_p95_ms": before["p95_ms"],
        "optimized_p95_ms": after["p95_ms"],
        "p95_improvement_percent": improvement(before["p95_ms"], after["p95_ms"]),
        "baseline_mean_ms": before["mean_ms"],
        "optimized_mean_ms": after["mean_ms"],
        "mean_improvement_percent": improvement(before["mean_ms"], after["mean_ms"]),
    })

(RESULTS / "comparison.json").write_text(json.dumps({"comparisons": rows}, indent=2) + "\n")
with (RESULTS / "comparison.csv").open("w", newline="") as handle:
    writer = csv.DictWriter(handle, fieldnames=rows[0].keys())
    writer.writeheader()
    writer.writerows(rows)

print("dataset workload              p50 improvement   p95 improvement")
for row in rows:
    print(f"{row['dataset']:>7} {row['workload']:<21} "
          f"{row['p50_improvement_percent']:>9.2f}% "
          f"{row['p95_improvement_percent']:>15.2f}%")

jpa_baseline_path = RESULTS / "jpa_baseline.json"
jpa_optimized_path = RESULTS / "jpa_optimized.json"
if jpa_baseline_path.exists() and jpa_optimized_path.exists():
    jpa_before = json.loads(jpa_baseline_path.read_text())
    jpa_after = json.loads(jpa_optimized_path.read_text())
    jpa_rows = []
    for before, after in zip(jpa_before["workloads"], jpa_after["workloads"]):
        if before["name"] != after["name"]:
            raise SystemExit("JPA workload mismatch")
        jpa_rows.append({
            "workload": before["name"],
            "baseline_p50_ms": before["p50_ms"],
            "optimized_p50_ms": after["p50_ms"],
            "p50_improvement_percent": improvement(before["p50_ms"], after["p50_ms"]),
            "baseline_p95_ms": before["p95_ms"],
            "optimized_p95_ms": after["p95_ms"],
            "p95_improvement_percent": improvement(before["p95_ms"], after["p95_ms"]),
        })
    (RESULTS / "jpa_comparison.json").write_text(
        json.dumps({"dataset": "500k", "comparisons": jpa_rows}, indent=2) + "\n"
    )
