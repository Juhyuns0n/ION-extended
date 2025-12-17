package capstone.workbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "workbook",
        indexes = {
                @Index(name = "idx_workbook_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workbook_id")
    private int workbookId;
    @Column(name = "done")
    private int done; // 0->시작 전 1->워크북 활동 완료

    @Column(name = "user_id")
    private int userId;

    @Column(name = "chapter_id")
    private int chapterId;
    @Column(name = "chapter_title")
    private String chapterTitle;
    @Column(name = "lesson_id")
    private int lessonId;
    @Column(name = "lesson_title")
    private String lessonTitle;

    // ---------------- 작성형 ----------------
    @Lob
    @Column(name = "first_descriptive_form_question", columnDefinition = "TEXT")
    private String firstDescriptiveFormQuestion;
    @Lob
    @Column(name = "first_descriptive_form_answer", columnDefinition = "TEXT")
    private String firstDescriptiveFormAnswer;
    @Lob
    @Column(name = "first_descriptive_form_example", columnDefinition = "TEXT")
    private String firstDescriptiveFormExample;

    @Lob
    @Column(name = "second_descriptive_form_question", columnDefinition = "TEXT")
    private String secondDescriptiveFormQuestion;
    @Lob
    @Column(name = "second_descriptive_form_answer", columnDefinition = "TEXT")
    private String secondDescriptiveFormAnswer;
    @Lob
    @Column(name = "second_descriptive_form_example", columnDefinition = "TEXT")
    private String secondDescriptiveFormExample;

    // ---------------- 선택형 ----------------
    @Lob
    @Column(name = "first_selective_question", columnDefinition = "TEXT")
    private String firstSelectiveQuestion;
    @Lob
    @Column(name = "first_selective_options", columnDefinition = "TEXT")
    private String firstSelectiveOptions;
    @Lob
    @Column(name = "first_selective_answer", columnDefinition = "TEXT")
    private String firstSelectiveAnswer;
    @Lob
    @Column(name = "first_selective_example", columnDefinition = "TEXT")
    private String firstSelectiveExample;


    @Lob
    @Column(name = "second_selective_question", columnDefinition = "TEXT")
    private String secondSelectiveQuestion;
    @Lob
    @Column(name = "second_selective_options", columnDefinition = "TEXT")
    private String secondSelectiveOptions;
    @Lob
    @Column(name = "second_selective_answer", columnDefinition = "TEXT")
    private String secondSelectiveAnswer;
    @Lob
    @Column(name = "second_selective_example", columnDefinition = "TEXT")
    private String secondSelectiveExample;


    @Lob
    @Column(name = "third_selective_question", columnDefinition = "TEXT")
    private String thirdSelectiveQuestion;
    @Lob
    @Column(name = "third_selective_options", columnDefinition = "TEXT")
    private String thirdSelectiveOptions;
    @Lob
    @Column(name = "third_selective_answer", columnDefinition = "TEXT")
    private String thirdSelectiveAnswer;
    @Lob
    @Column(name = "third_selective_example", columnDefinition = "TEXT")
    private String thirdSelectiveExample;

    // ---------------- 피드백 ----------------
    @Lob
    @Column(name = "workbook_feedback", columnDefinition = "TEXT")
    private String workbookFeedback;
}
