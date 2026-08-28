package capstone.workbook;

import capstone.workbook.repository.WorkbookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=" +
                "capstone.workbook.HibernateSqlCaptureInspector"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "ION_PERF_ENABLED", matches = "true")
class WorkbookRepositorySqlShapeTest {

    @Autowired
    private WorkbookRepository repository;

    @BeforeEach
    void resetCapture() {
        HibernateSqlCaptureInspector.clear();
    }

    @Test
    void capturesTheThreeRepositorySqlPatterns() throws Exception {
        repository.findLessons(1, 1);
        repository.findByUserIdAndChapterIdAndLessonId(1, 1, 1);
        repository.findTopByUserIdAndChapterIdOrderByLessonIdAsc(1, 1);

        Set<String> captured = HibernateSqlCaptureInspector.statements();
        assertEquals(3, captured.size());
        assertTrue(captured.stream().anyMatch(sql ->
                sql.contains("lesson_id") && sql.contains("lesson_title") &&
                        sql.contains("done") && sql.contains("order by")));
        assertTrue(captured.stream().anyMatch(sql ->
                sql.contains("chapter_id=?") && sql.contains("lesson_id=?") &&
                        !sql.contains("order by")));
        assertTrue(captured.stream().anyMatch(sql ->
                sql.contains("order by") && (sql.contains("limit") || sql.contains("fetch first"))));

        Path output = Path.of(requiredEnvironment("ION_PERF_SQL_OUTPUT"));
        Files.createDirectories(output.getParent());
        List<String> sorted = captured.stream().sorted(Comparator.naturalOrder()).toList();
        Files.writeString(output, String.join("\n\n", sorted) + "\n");
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }
        return value;
    }
}
