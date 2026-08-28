#include <mysql.h>
#include <errno.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

typedef struct {
    const char *name;
    const char *sql_template;
    int needs_lesson;
} workload_t;

typedef struct {
    double mean_ms;
    double p50_ms;
    double p95_ms;
    double p99_ms;
    double ops_per_second;
    unsigned long long rows_returned;
} stats_t;

static const workload_t WORKLOADS[] = {
    {
        "lessons_projection",
        "SELECT lesson_id, lesson_title, done FROM workbook "
        "WHERE user_id = %u AND chapter_id = %u ORDER BY lesson_id ASC",
        0
    },
    {
        "exact_workbook",
        "SELECT * FROM workbook WHERE user_id = %u AND chapter_id = %u AND lesson_id = %u",
        1
    },
    {
        "first_lesson",
        "SELECT * FROM workbook WHERE user_id = %u AND chapter_id = %u "
        "ORDER BY lesson_id ASC LIMIT 1",
        0
    }
};

static void fail_mysql(MYSQL *connection, const char *context) {
    fprintf(stderr, "%s: %s\n", context, mysql_error(connection));
    exit(1);
}

static void fail_errno(const char *context) {
    fprintf(stderr, "%s: %s\n", context, strerror(errno));
    exit(1);
}

static double now_ms(void) {
    struct timespec ts;
    if (clock_gettime(CLOCK_MONOTONIC, &ts) != 0) {
        fail_errno("clock_gettime");
    }
    return (double)ts.tv_sec * 1000.0 + (double)ts.tv_nsec / 1000000.0;
}

static uint64_t splitmix64(uint64_t value) {
    value += UINT64_C(0x9e3779b97f4a7c15);
    value = (value ^ (value >> 30)) * UINT64_C(0xbf58476d1ce4e5b9);
    value = (value ^ (value >> 27)) * UINT64_C(0x94d049bb133111eb);
    return value ^ (value >> 31);
}

static void deterministic_key(unsigned long iteration, unsigned workload_index,
                              unsigned users, unsigned *user_id,
                              unsigned *chapter_id, unsigned *lesson_id) {
    uint64_t mixed = splitmix64(UINT64_C(20260828) + iteration + workload_index * UINT64_C(10000019));
    *user_id = (unsigned)(mixed % users) + 1;
    *chapter_id = (unsigned)((mixed >> 21) % 7) + 1;
    *lesson_id = (unsigned)((mixed >> 42) % 4) + 1;
}

static unsigned long long execute_and_consume(MYSQL *connection, const char *sql) {
    if (mysql_real_query(connection, sql, (unsigned long)strlen(sql)) != 0) {
        fail_mysql(connection, sql);
    }
    MYSQL_RES *result = mysql_store_result(connection);
    if (result == NULL) {
        if (mysql_field_count(connection) != 0) {
            fail_mysql(connection, "mysql_store_result");
        }
        return 0;
    }
    unsigned long long rows = 0;
    while (mysql_fetch_row(result) != NULL) {
        rows++;
    }
    mysql_free_result(result);
    return rows;
}

static int compare_double(const void *left, const void *right) {
    double a = *(const double *)left;
    double b = *(const double *)right;
    return (a > b) - (a < b);
}

static double percentile(const double *sorted, unsigned long count, double quantile) {
    unsigned long index = (unsigned long)ceil(quantile * (double)count);
    if (index == 0) index = 1;
    if (index > count) index = count;
    return sorted[index - 1];
}

static stats_t benchmark_workload(MYSQL *connection, const workload_t *workload,
                                  unsigned workload_index, unsigned users,
                                  unsigned long warmup, unsigned long iterations) {
    char sql[2048];
    unsigned user_id, chapter_id, lesson_id;
    unsigned long long measured_rows = 0;

    for (unsigned long i = 0; i < warmup; i++) {
        deterministic_key(i, workload_index, users, &user_id, &chapter_id, &lesson_id);
        if (workload->needs_lesson) {
            snprintf(sql, sizeof(sql), workload->sql_template, user_id, chapter_id, lesson_id);
        } else {
            snprintf(sql, sizeof(sql), workload->sql_template, user_id, chapter_id);
        }
        execute_and_consume(connection, sql);
    }

    double *latencies = calloc(iterations, sizeof(double));
    double *sorted = calloc(iterations, sizeof(double));
    if (latencies == NULL || sorted == NULL) {
        fprintf(stderr, "Unable to allocate latency arrays.\n");
        exit(1);
    }

    double total_start = now_ms();
    for (unsigned long i = 0; i < iterations; i++) {
        deterministic_key(i, workload_index, users, &user_id, &chapter_id, &lesson_id);
        if (workload->needs_lesson) {
            snprintf(sql, sizeof(sql), workload->sql_template, user_id, chapter_id, lesson_id);
        } else {
            snprintf(sql, sizeof(sql), workload->sql_template, user_id, chapter_id);
        }
        double start = now_ms();
        measured_rows += execute_and_consume(connection, sql);
        latencies[i] = now_ms() - start;
    }
    double elapsed_ms = now_ms() - total_start;

    double sum = 0.0;
    for (unsigned long i = 0; i < iterations; i++) {
        sum += latencies[i];
        sorted[i] = latencies[i];
    }
    qsort(sorted, iterations, sizeof(double), compare_double);

    stats_t stats = {
        .mean_ms = sum / (double)iterations,
        .p50_ms = percentile(sorted, iterations, 0.50),
        .p95_ms = percentile(sorted, iterations, 0.95),
        .p99_ms = percentile(sorted, iterations, 0.99),
        .ops_per_second = (double)iterations / (elapsed_ms / 1000.0),
        .rows_returned = measured_rows
    };
    free(latencies);
    free(sorted);
    return stats;
}

static void write_result_set(FILE *output, MYSQL_RES *result) {
    unsigned fields = mysql_num_fields(result);
    MYSQL_FIELD *metadata = mysql_fetch_fields(result);
    for (unsigned i = 0; i < fields; i++) {
        fprintf(output, "%s%s", i == 0 ? "" : "\t", metadata[i].name);
    }
    fputc('\n', output);
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(result)) != NULL) {
        for (unsigned i = 0; i < fields; i++) {
            fprintf(output, "%s%s", i == 0 ? "" : "\t", row[i] == NULL ? "NULL" : row[i]);
        }
        fputc('\n', output);
    }
}

static void explain_query(MYSQL *connection, const char *path, const char *query) {
    FILE *output = fopen(path, "w");
    if (output == NULL) fail_errno(path);
    fprintf(output, "QUERY\n%s\n\nEXPLAIN\n", query);

    char sql[4096];
    snprintf(sql, sizeof(sql), "EXPLAIN %s", query);
    if (mysql_real_query(connection, sql, (unsigned long)strlen(sql)) != 0) fail_mysql(connection, sql);
    MYSQL_RES *result = mysql_store_result(connection);
    if (result == NULL) fail_mysql(connection, "EXPLAIN result");
    write_result_set(output, result);
    mysql_free_result(result);

    fprintf(output, "\nEXPLAIN ANALYZE\n");
    snprintf(sql, sizeof(sql), "EXPLAIN ANALYZE %s", query);
    if (mysql_real_query(connection, sql, (unsigned long)strlen(sql)) != 0) fail_mysql(connection, sql);
    result = mysql_store_result(connection);
    if (result == NULL) fail_mysql(connection, "EXPLAIN ANALYZE result");
    write_result_set(output, result);
    mysql_free_result(result);
    fclose(output);
}

static char *single_value(MYSQL *connection, const char *query) {
    if (mysql_real_query(connection, query, (unsigned long)strlen(query)) != 0) fail_mysql(connection, query);
    MYSQL_RES *result = mysql_store_result(connection);
    if (result == NULL) fail_mysql(connection, query);
    MYSQL_ROW row = mysql_fetch_row(result);
    char *value = strdup(row == NULL || row[0] == NULL ? "" : row[0]);
    mysql_free_result(result);
    return value;
}

static const char *arg_value(int argc, char **argv, const char *name) {
    for (int i = 1; i + 1 < argc; i++) {
        if (strcmp(argv[i], name) == 0) return argv[i + 1];
    }
    fprintf(stderr, "Missing required argument %s\n", name);
    exit(2);
}

int main(int argc, char **argv) {
    const char *socket_path = arg_value(argc, argv, "--socket");
    const char *database = arg_value(argc, argv, "--database");
    const char *phase = arg_value(argc, argv, "--phase");
    const char *dataset_label = arg_value(argc, argv, "--dataset-label");
    const char *json_path = arg_value(argc, argv, "--json");
    const char *explain_dir = arg_value(argc, argv, "--explain-dir");
    unsigned long expected_rows = strtoul(arg_value(argc, argv, "--rows"), NULL, 10);
    unsigned long warmup = strtoul(arg_value(argc, argv, "--warmup"), NULL, 10);
    unsigned long iterations = strtoul(arg_value(argc, argv, "--iterations"), NULL, 10);
    unsigned users = (unsigned)(expected_rows / 28);
    if (users == 0 || warmup == 0 || iterations < 100) {
        fprintf(stderr, "rows/warmup/iterations are outside safe benchmark bounds.\n");
        return 2;
    }

    MYSQL *connection = mysql_init(NULL);
    if (connection == NULL) {
        fprintf(stderr, "mysql_init failed.\n");
        return 1;
    }
    if (mysql_real_connect(connection, NULL, "root", NULL, database, 0, socket_path, 0) == NULL) {
        fail_mysql(connection, "mysql_real_connect");
    }

    char *server_version = single_value(connection, "SELECT VERSION()");
    char *actual_rows_text = single_value(connection, "SELECT COUNT(*) FROM workbook");
    unsigned long actual_rows = strtoul(actual_rows_text, NULL, 10);
    if (actual_rows != expected_rows) {
        fprintf(stderr, "Expected %lu rows but found %lu in %s.\n", expected_rows, actual_rows, database);
        return 1;
    }
    char *composite_count = single_value(
        connection,
        "SELECT COUNT(*) FROM information_schema.statistics "
        "WHERE table_schema = DATABASE() AND table_name = 'workbook' "
        "AND index_name = 'idx_workbook_user_chapter_lesson'"
    );
    int has_composite = atoi(composite_count) > 0;
    if ((strcmp(phase, "baseline") == 0 && has_composite) ||
        (strcmp(phase, "optimized") == 0 && !has_composite)) {
        fprintf(stderr, "Index state does not match phase %s for %s.\n", phase, database);
        return 1;
    }

    stats_t stats[3];
    for (unsigned i = 0; i < 3; i++) {
        stats[i] = benchmark_workload(connection, &WORKLOADS[i], i, users, warmup, iterations);
        printf("%s/%s %-20s p50=%8.4f ms p95=%8.4f ms\n",
               phase, dataset_label, WORKLOADS[i].name, stats[i].p50_ms, stats[i].p95_ms);
    }

    unsigned representative_user = users / 2 + 1;
    for (unsigned i = 0; i < 3; i++) {
        char query[2048];
        char path[2048];
        if (WORKLOADS[i].needs_lesson) {
            snprintf(query, sizeof(query), WORKLOADS[i].sql_template, representative_user, 4U, 2U);
        } else {
            snprintf(query, sizeof(query), WORKLOADS[i].sql_template, representative_user, 4U);
        }
        snprintf(path, sizeof(path), "%s/%s.txt", explain_dir, WORKLOADS[i].name);
        explain_query(connection, path, query);
    }

    FILE *json = fopen(json_path, "w");
    if (json == NULL) fail_errno(json_path);
    fprintf(json,
        "{\n"
        "  \"phase\": \"%s\",\n"
        "  \"dataset\": \"%s\",\n"
        "  \"database\": \"%s\",\n"
        "  \"mysql_version\": \"%s\",\n"
        "  \"rows\": %lu,\n"
        "  \"users\": %u,\n"
        "  \"chapters_per_user\": 7,\n"
        "  \"lessons_per_chapter\": 4,\n"
        "  \"seed\": \"ion-workbook-perf-v1 / 20260828\",\n"
        "  \"warmup_iterations_per_workload\": %lu,\n"
        "  \"measured_iterations_per_workload\": %lu,\n"
        "  \"composite_index_present\": %s,\n"
        "  \"workloads\": [\n",
        phase, dataset_label, database, server_version, actual_rows, users,
        warmup, iterations, has_composite ? "true" : "false");
    for (unsigned i = 0; i < 3; i++) {
        fprintf(json,
            "    {\"name\": \"%s\", \"samples\": %lu, \"mean_ms\": %.6f, "
            "\"p50_ms\": %.6f, \"p95_ms\": %.6f, \"p99_ms\": %.6f, "
            "\"ops_per_second\": %.3f, \"rows_returned\": %llu}%s\n",
            WORKLOADS[i].name, iterations, stats[i].mean_ms, stats[i].p50_ms,
            stats[i].p95_ms, stats[i].p99_ms, stats[i].ops_per_second,
            stats[i].rows_returned, i == 2 ? "" : ",");
    }
    fprintf(json, "  ]\n}\n");
    fclose(json);

    free(server_version);
    free(actual_rows_text);
    free(composite_count);
    mysql_close(connection);
    return 0;
}
