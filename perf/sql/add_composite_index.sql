-- Apply only after perf/results/baseline.json has been produced and checked.
CREATE INDEX idx_workbook_user_chapter_lesson
    ON workbook (user_id, chapter_id, lesson_id);

ANALYZE TABLE workbook;
