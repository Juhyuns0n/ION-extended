-- Reconstructed from backend/src/main/java/capstone/workbook/entity/Workbook.java.
-- This is an experimental baseline, not a claim about the deleted RDS schema.
-- Intentionally absent: (user_id, chapter_id, lesson_id) composite index.

DROP TABLE IF EXISTS workbook;

CREATE TABLE workbook (
    workbook_id INT NOT NULL AUTO_INCREMENT,
    done INT NOT NULL,
    user_id INT NOT NULL,
    chapter_id INT NOT NULL,
    chapter_title VARCHAR(255),
    lesson_id INT NOT NULL,
    lesson_title VARCHAR(255),
    first_descriptive_form_question TEXT,
    first_descriptive_form_answer TEXT,
    first_descriptive_form_example TEXT,
    second_descriptive_form_question TEXT,
    second_descriptive_form_answer TEXT,
    second_descriptive_form_example TEXT,
    first_selective_question TEXT,
    first_selective_options TEXT,
    first_selective_answer TEXT,
    first_selective_example TEXT,
    second_selective_question TEXT,
    second_selective_options TEXT,
    second_selective_answer TEXT,
    second_selective_example TEXT,
    third_selective_question TEXT,
    third_selective_options TEXT,
    third_selective_answer TEXT,
    third_selective_example TEXT,
    workbook_feedback TEXT,
    PRIMARY KEY (workbook_id),
    KEY idx_workbook_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
