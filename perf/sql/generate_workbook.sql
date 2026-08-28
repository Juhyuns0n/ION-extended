-- The caller must set @target_rows. Use multiples of 28 to preserve the domain's
-- seven chapters x four workbook lessons per user.
-- Fixed generator seed: ion-workbook-perf-v1.

INSERT INTO workbook (
    done, user_id, chapter_id, chapter_title, lesson_id, lesson_title,
    first_descriptive_form_question, first_descriptive_form_answer, first_descriptive_form_example,
    second_descriptive_form_question, second_descriptive_form_answer, second_descriptive_form_example,
    first_selective_question, first_selective_options, first_selective_answer, first_selective_example,
    second_selective_question, second_selective_options, second_selective_answer, second_selective_example,
    third_selective_question, third_selective_options, third_selective_answer, third_selective_example,
    workbook_feedback
)
WITH digits AS (
    SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
)
SELECT
    MOD(n, 3) = 0,
    FLOOR(n / 28) + 1,
    FLOOR(MOD(n, 28) / 4) + 1,
    CONCAT('Chapter ', FLOOR(MOD(n, 28) / 4) + 1),
    MOD(n, 4) + 1,
    CONCAT('Lesson ', MOD(n, 4) + 1),
    CONCAT('dq1-', digest, '-', digest),
    IF(MOD(n, 3) = 0, CONCAT('da1-', digest), NULL),
    CONCAT('de1-', digest, '-', digest),
    CONCAT('dq2-', digest, '-', digest),
    IF(MOD(n, 3) = 0, CONCAT('da2-', digest), NULL),
    CONCAT('de2-', digest, '-', digest),
    CONCAT('sq1-', digest, '-', digest),
    CONCAT('["', digest, '","option-b","option-c"]'),
    IF(MOD(n, 3) = 0, 'option-b', NULL),
    CONCAT('se1-', digest, '-', digest),
    CONCAT('sq2-', digest, '-', digest),
    CONCAT('["', digest, '","option-b","option-c"]'),
    IF(MOD(n, 3) = 0, 'option-c', NULL),
    CONCAT('se2-', digest, '-', digest),
    CONCAT('sq3-', digest, '-', digest),
    CONCAT('["', digest, '","option-b","option-c"]'),
    IF(MOD(n, 3) = 0, 'option-a', NULL),
    CONCAT('se3-', digest, '-', digest),
    IF(MOD(n, 3) = 0, CONCAT('feedback-', digest, '-', digest), NULL)
FROM (
    SELECT n, SHA2(CONCAT('ion-workbook-perf-v1:', n), 256) AS digest
    FROM (
        SELECT a.d + 10*b.d + 100*c.d + 1000*d.d + 10000*e.d + 100000*f.d AS n
        FROM digits a
        CROSS JOIN digits b
        CROSS JOIN digits c
        CROSS JOIN digits d
        CROSS JOIN digits e
        CROSS JOIN digits f
    ) numbers
    WHERE n < @target_rows
) seeded
ORDER BY n;

ANALYZE TABLE workbook;
