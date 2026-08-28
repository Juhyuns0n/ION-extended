# Workbook Query Benchmark

This benchmark tests whether a composite index improves the main Workbook query patterns on local MySQL 8.

The original RDS instance no longer exists, so this is not a production performance claim. The schema was reconstructed from the `Workbook` entity, and both phases use the same generated data and query inputs.

## Indexes under test

Baseline:

```sql
PRIMARY KEY (workbook_id)
INDEX idx_workbook_user (user_id)
```

Optimized:

```sql
CREATE INDEX idx_workbook_user_chapter_lesson
ON workbook (user_id, chapter_id, lesson_id);
```

The original `idx_workbook_user` index remains present in both phases.

## Running the benchmark

The scripts default to Homebrew MySQL 8.4. Set `MYSQL_HOME_DIR` if MySQL is installed elsewhere.

Run these commands from the repository root:

```bash
perf/scripts/start_mysql.sh
perf/scripts/reset_datasets.sh

perf/scripts/run_benchmark.sh baseline
perf/scripts/run_jpa_benchmark.sh baseline

perf/scripts/apply_optimization.sh

perf/scripts/run_benchmark.sh optimized
perf/scripts/run_jpa_benchmark.sh optimized

perf/scripts/capture_hibernate_sql.sh
python3 perf/scripts/compare_results.py
perf/scripts/stop_mysql.sh
```

`apply_optimization.sh` refuses to add the composite index unless the baseline results and execution plans already exist.

## Datasets

- 9,996 rows: 357 users
- 99,988 rows: 3,571 users
- 499,996 rows: 17,857 users
- 7 chapters and 4 lessons per user
- Fixed generator and query seeds
- Representative TEXT fields and incomplete answer/feedback values

The default run uses 500 warm-up queries and 5,000 measured queries per workload.

## Project layout

- `sql/`: baseline schema, data generator, and index DDL
- `src/workbook_benchmark.c`: raw SQL benchmark runner
- `scripts/`: database setup, benchmark, and comparison scripts
- `results/baseline.json` and `optimized.json`: raw SQL results
- `results/jpa_baseline.json` and `jpa_optimized.json`: Spring Data JPA results
- `results/explain/*/500k/`: representative execution plans
- `REPORT.md`: result summary

MySQL data files are created only under `perf/.runtime/`, which is excluded from Git. The application uses `ddl-auto: none`, so deploying this index would require a separate schema migration.
