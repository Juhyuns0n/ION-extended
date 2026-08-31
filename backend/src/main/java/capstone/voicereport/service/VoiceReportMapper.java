package capstone.voicereport.service;

import capstone.user.entity.User;
import capstone.user.repository.UserRepository;
import capstone.voicereport.dto.VoiceReportResponse;
import capstone.voicereport.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoiceReportMapper {

    private final UserRepository userRepository;

    public void applyAnalysis(VoiceReport report, VoiceReportResponse analysis) {
        if (analysis == null) throw new IllegalArgumentException("Analysis response is empty");
        report.setSubTitle(analysis.getSubTitle());
        report.setDay(analysis.getDay());
        report.setConversationSummary(analysis.getConversationSummary());
        report.setOverallFeedback(analysis.getOverallFeedback());

        if (analysis.getExpression() != null) {
            report.setExpression(Expression.builder()
                    .parentExpression(analysis.getExpression().getParentExpression())
                    .kidExpression(analysis.getExpression().getKidExpression())
                    .parentConditions(analysis.getExpression().getParentConditions())
                    .kidConditions(analysis.getExpression().getKidConditions())
                    .expressionFeedback(analysis.getExpression().getExpressionFeedback())
                    .build());
        }
        if (analysis.getChangeProposal() != null) {
            report.setChangeProposals(analysis.getChangeProposal().stream()
                    .map(item -> ChangeProposal.builder()
                            .existingExpression(item.getExistingExpression())
                            .proposalExpression(item.getProposalExpression())
                            .build())
                    .toList());
        }
        if (analysis.getEmotion() != null) {
            report.setEmotionFeedback(analysis.getEmotion().getEmotionFeedback());
            if (analysis.getEmotion().getTimeline() != null) {
                report.setEmotionTimeline(analysis.getEmotion().getTimeline().stream()
                        .map(item -> EmotionPoint.builder()
                                .time(item.getTime())
                                .momentEmotion(item.getMomentEmotion())
                                .build())
                        .toList());
            }
        }
        report.setKidAttitude(analysis.getKidAttitude());
        if (analysis.getFrequency() != null) {
            report.setFrequency(Frequency.builder()
                    .parentFrequency(analysis.getFrequency().getParentFrequency())
                    .kidFrequency(analysis.getFrequency().getKidFrequency())
                    .frequencyFeedback(analysis.getFrequency().getFrequencyFeedback())
                    .build());
        }
        report.setStrength(analysis.getStrength());
    }

    public VoiceReportResponse toDto(VoiceReport report) {
        List<EmotionPoint> timeline = report.getEmotionTimeline() == null ? List.of() : report.getEmotionTimeline();
        List<ChangeProposal> proposals = report.getChangeProposals() == null ? List.of() : report.getChangeProposals();
        User user = userRepository.findById(report.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + report.getUserId()));

        VoiceReportResponse response = new VoiceReportResponse();
        response.setKidsNickname(user.getKidsNickname());
        response.setReportId(report.getReportId());
        response.setSubTitle(report.getSubTitle());
        response.setDay(report.getDay());
        response.setConversationSummary(report.getConversationSummary());
        response.setOverallFeedback(report.getOverallFeedback());

        VoiceReportResponse.ExpressionDto expression = new VoiceReportResponse.ExpressionDto();
        if (report.getExpression() != null) {
            expression.setParentExpression(report.getExpression().getParentExpression());
            expression.setKidExpression(report.getExpression().getKidExpression());
            expression.setParentConditions(report.getExpression().getParentConditions());
            expression.setKidConditions(report.getExpression().getKidConditions());
            expression.setExpressionFeedback(report.getExpression().getExpressionFeedback());
        }
        response.setExpression(expression);
        response.setChangeProposal(proposals.stream().map(item -> {
            VoiceReportResponse.ChangeProposalDto dto = new VoiceReportResponse.ChangeProposalDto();
            dto.setExistingExpression(item.getExistingExpression());
            dto.setProposalExpression(item.getProposalExpression());
            return dto;
        }).toList());

        VoiceReportResponse.EmotionDto emotion = new VoiceReportResponse.EmotionDto();
        emotion.setTimeline(timeline.stream().map(item -> {
            VoiceReportResponse.EmotionPointDto dto = new VoiceReportResponse.EmotionPointDto();
            dto.setTime(item.getTime());
            dto.setMomentEmotion(item.getMomentEmotion());
            return dto;
        }).toList());
        emotion.setEmotionFeedback(report.getEmotionFeedback());
        response.setEmotion(emotion);
        response.setKidAttitude(report.getKidAttitude());

        VoiceReportResponse.FrequencyDto frequency = new VoiceReportResponse.FrequencyDto();
        if (report.getFrequency() != null) {
            frequency.setParentFrequency(report.getFrequency().getParentFrequency());
            frequency.setKidFrequency(report.getFrequency().getKidFrequency());
            frequency.setFrequencyFeedback(report.getFrequency().getFrequencyFeedback());
        }
        response.setFrequency(frequency);
        response.setStrength(report.getStrength());
        return response;
    }
}
