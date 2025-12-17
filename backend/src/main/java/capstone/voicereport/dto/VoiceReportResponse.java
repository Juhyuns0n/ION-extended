package capstone.voicereport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoiceReportResponse {
    @JsonProperty("reportId")
    private int reportId;
    private String subTitle;
    private String day;
    private String conversationSummary;
    private String overallFeedback;
    private ExpressionDto expression;
    private List<ChangeProposalDto> changeProposal;
    private EmotionDto emotion;
    private String kidAttitude;
    private FrequencyDto frequency;
    private String strength;
    private String kidsNickname;

    @Getter @Setter
    public static class ExpressionDto {
        private String parentExpression;
        private String kidExpression;
        private String parentConditions;
        private String kidConditions;
        private String expressionFeedback;
    }
    @Getter @Setter
    public static class ChangeProposalDto {
        private String existingExpression;
        private String proposalExpression;
    }
    @Getter @Setter
    public static class EmotionDto {
        private List<EmotionPointDto> timeline;
        private String emotionFeedback;
    }
    @Getter @Setter
    public static class EmotionPointDto {
        private String time;
        private String momentEmotion;
    }
    @Getter @Setter
    public static class FrequencyDto {
        private Integer parentFrequency;
        private Integer kidFrequency;
        private String frequencyFeedback;
    }
}