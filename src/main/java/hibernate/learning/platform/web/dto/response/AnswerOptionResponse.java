package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.AnswerOption;

public record AnswerOptionResponse(Long id, String text, boolean correct) {
    public static AnswerOptionResponse from(AnswerOption answerOption) {
        return new AnswerOptionResponse(answerOption.getId(), answerOption.getText(), answerOption.isCorrect());
    }
}
