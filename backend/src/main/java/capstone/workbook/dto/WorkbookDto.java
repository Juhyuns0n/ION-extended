package capstone.workbook.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkbookDto {
    private Integer workbookId;
    private Integer done; // 0->시작 전 1->워크북 활동 완료

    private Integer userId;

    private Integer chapterId;
    private String chapterTitle;
    private Integer lessonId;
    private String lessonTitle;

    // 작성형
    private String firstDescriptiveFormQuestion;
    private String firstDescriptiveFormAnswer;
    private String firstDescriptiveFormExample;

    private String secondDescriptiveFormQuestion;
    private String secondDescriptiveFormAnswer;
    private String secondDescriptiveFormExample;

    // 선택형
    private String firstSelectiveQuestion;
    private List<String> firstSelectiveOptions;
    private String firstSelectiveAnswer;
    private String firstSelectiveExample;

    private String secondSelectiveQuestion;
    private List<String> secondSelectiveOptions;
    private String secondSelectiveAnswer;
    private String secondSelectiveExample;

    private String thirdSelectiveQuestion;
    private List<String> thirdSelectiveOptions;
    private String thirdSelectiveAnswer;
    private String thirdSelectiveExample;

    private String workbookFeedback;


}
