package capstone.workbook;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateSqlCaptureInspector implements StatementInspector {
    private static final Set<String> STATEMENTS = ConcurrentHashMap.newKeySet();

    @Override
    public String inspect(String sql) {
        String normalized = sql.replaceAll("\\s+", " ").trim();
        if (normalized.startsWith("select") && normalized.contains(" workbook ")) {
            STATEMENTS.add(normalized);
        }
        return sql;
    }

    static void clear() {
        STATEMENTS.clear();
    }

    static Set<String> statements() {
        return Set.copyOf(STATEMENTS);
    }
}
