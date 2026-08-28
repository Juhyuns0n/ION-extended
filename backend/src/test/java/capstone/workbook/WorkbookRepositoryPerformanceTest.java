package capstone.workbook;

import capstone.workbook.repository.WorkbookRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Explicit opt-in JPA benchmark. Normal test runs skip it, and it never creates
 * or changes schema. The perf scripts provide an isolated local MySQL datasource.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "ION_PERF_ENABLED", matches = "true")
class WorkbookRepositoryPerformanceTest {

    @Autowired
    private WorkbookRepository workbookRepository;

    @Autowired
    private EntityManager entityManager;

    private record Stats(double meanMs, double p50Ms, double p95Ms, double p99Ms) {}

    @Test
    void benchmarkRealRepositoryMethods() throws Exception {
        int users = Integer.parseInt(requiredEnvironment("ION_PERF_USERS"));
        int warmup = Integer.parseInt(System.getenv().getOrDefault("ION_PERF_JPA_WARMUP", "200"));
        int iterations = Integer.parseInt(System.getenv().getOrDefault("ION_PERF_JPA_ITERATIONS", "1000"));
        String phase = requiredEnvironment("ION_PERF_PHASE");
        Path output = Path.of(requiredEnvironment("ION_PERF_OUTPUT"));

        Stats lessons = measure(users, warmup, iterations, 0, (user, chapter, lesson) ->
                workbookRepository.findLessons(user, chapter).size());
        Stats exact = measure(users, warmup, iterations, 1, (user, chapter, lesson) -> {
            assertNotNull(workbookRepository.findByUserIdAndChapterIdAndLessonId(user, chapter, lesson));
            return 1;
        });
        Stats first = measure(users, warmup, iterations, 2, (user, chapter, lesson) -> {
            assertNotNull(workbookRepository.findTopByUserIdAndChapterIdOrderByLessonIdAsc(user, chapter));
            return 1;
        });

        Files.createDirectories(output.getParent());
        Files.writeString(output, String.format(Locale.ROOT, """
                {
                  "phase": "%s",
                  "benchmark_kind": "Spring Data JPA repository calls",
                  "dataset": "500k",
                  "users": %d,
                  "warmup_iterations_per_workload": %d,
                  "measured_iterations_per_workload": %d,
                  "workloads": [
                    %s,
                    %s,
                    %s
                  ]
                }
                """, phase, users, warmup, iterations,
                json("findLessons", lessons), json("findByUserIdAndChapterIdAndLessonId", exact),
                json("findTopByUserIdAndChapterIdOrderByLessonIdAsc", first)));
    }

    private Stats measure(int users, int warmup, int iterations, int workload,
                          RepositoryCall repositoryCall) {
        for (int i = 0; i < warmup; i++) {
            Key key = key(i, workload, users);
            repositoryCall.run(key.user(), key.chapter(), key.lesson());
            entityManager.clear();
        }
        double[] values = new double[iterations];
        for (int i = 0; i < iterations; i++) {
            Key key = key(i, workload, users);
            long start = System.nanoTime();
            repositoryCall.run(key.user(), key.chapter(), key.lesson());
            values[i] = (System.nanoTime() - start) / 1_000_000.0;
            entityManager.clear();
        }
        double mean = Arrays.stream(values).average().orElseThrow();
        Arrays.sort(values);
        return new Stats(mean, percentile(values, 0.50), percentile(values, 0.95), percentile(values, 0.99));
    }

    private static double percentile(double[] sorted, double quantile) {
        int index = Math.max(0, Math.min(sorted.length - 1,
                (int) Math.ceil(quantile * sorted.length) - 1));
        return sorted[index];
    }

    private static Key key(int iteration, int workload, int users) {
        long mixed = splitMix64(20260828L + iteration + workload * 10_000_019L);
        int user = (int) Long.remainderUnsigned(mixed, users) + 1;
        int chapter = (int) Long.remainderUnsigned(mixed >>> 21, 7) + 1;
        int lesson = (int) Long.remainderUnsigned(mixed >>> 42, 4) + 1;
        return new Key(user, chapter, lesson);
    }

    private static long splitMix64(long value) {
        value += 0x9e3779b97f4a7c15L;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private static String json(String name, Stats value) {
        return String.format(Locale.ROOT,
                "{\"name\": \"%s\", \"mean_ms\": %.6f, \"p50_ms\": %.6f, " +
                        "\"p95_ms\": %.6f, \"p99_ms\": %.6f}",
                name, value.meanMs(), value.p50Ms(), value.p95Ms(), value.p99Ms());
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required for the opt-in performance test");
        }
        return value;
    }

    private record Key(int user, int chapter, int lesson) {}

    @FunctionalInterface
    private interface RepositoryCall {
        int run(int user, int chapter, int lesson);
    }
}
