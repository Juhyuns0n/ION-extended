# Workbook Index Benchmark Results

## Summary

Adding an index on `(user_id, chapter_id, lesson_id)` improved all three Workbook query patterns.

On the 500K-row dataset, raw SQL p50 latency improved by 41.7–45.8%. Spring Data JPA p50 latency improved by 15.2–19.7%.

## Methodology

The benchmark used deterministic datasets of approximately 10K, 100K, and
500K Workbook rows, preserving the application's 7-chapter × 4-lesson structure.
Fixed seeds made both the generated data and lookup inputs replayable.

Raw SQL measurements used a persistent MySQL connection with 500 warm-up
operations followed by 5,000 measured operations per workload. Result rows
were fully consumed so timings include client-side result transfer.

The Spring Data JPA benchmark used the actual WorkbookRepository methods on
the 500K dataset, with 200 warm-up and 1,000 measured operations.

Baseline and optimized runs used identical generated data and lookup keys.
The only tested schema change was the composite index on
`(user_id, chapter_id, lesson_id)`.

## 500K-row results

| Query | Raw p50 (before → after) | Raw p95 (before → after) | JPA p50 | JPA p95 |
|---|---:|---:|---:|---:|
| Lesson list | 0.090 → 0.051 ms | 0.585 → 0.060 ms | 0.428 → 0.344 ms | 0.741 → 0.544 ms |
| Exact Workbook | 0.115 → 0.067 ms | 0.569 → 0.080 ms | 0.520 → 0.422 ms | 0.831 → 0.627 ms |
| First lesson | 0.118 → 0.064 ms | 0.405 → 0.074 ms | 0.321 → 0.272 ms | 0.627 → 0.358 ms |

Full measurements are available in `results/baseline.json`, `results/optimized.json`, and `results/jpa_*.json`.

## Execution-plan change

With only `idx_workbook_user`, MySQL read all 28 Workbook rows belonging to a user and then filtered by chapter and lesson. The ordered queries also required a filesort.

After adding the composite index:

- Lesson list: the measured plan used the two-column `(user_id, chapter_id)` prefix and reduced 28 index rows to 4
- Exact lookup: the measured plan used the three-column `(user_id, chapter_id, lesson_id)` prefix and reduced 28 index rows to 1
- First lesson: the measured plan used the two-column prefix and stopped after the first row in the chapter range
- `ORDER BY lesson_id` no longer requires a filesort

No standalone user-only query was added; the saved plans directly verify only the two- and three-column prefixes used by the application workload.

Representative plans are saved under `results/explain/baseline/500k/` and `results/explain/optimized/500k/`.
