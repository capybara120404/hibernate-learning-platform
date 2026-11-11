package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.enums.QuestionType;

public record QuestionSummaryResponse(Long id, String text, QuestionType type, int points) {
    public static QuestionSummaryResponse from(Question question) {
        return new QuestionSummaryResponse(question.getId(), question.getText(), question.getType(),
                question.getPoints());
    }
}
