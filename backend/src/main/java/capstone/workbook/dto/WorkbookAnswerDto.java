package capstone.workbook.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkbookAnswerDto {
    private String firstDescriptiveFormAnswer;
    private String secondDescriptiveFormAnswer;

    private String firstSelectiveAnswer;
    private String secondSelectiveAnswer;
    private String thirdSelectiveAnswer;
}
