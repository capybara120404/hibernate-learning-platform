package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.AnswerOption;

public record AnswerOptionSummaryResponse(Long id, String text) {
    public static AnswerOptionSummaryResponse from(AnswerOption answerOption) {
        return new AnswerOptionSummaryResponse(answerOption.getId(), answerOption.getText());
    }
}
