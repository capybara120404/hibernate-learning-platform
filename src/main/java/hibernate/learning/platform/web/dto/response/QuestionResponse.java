package hibernate.learning.platform.web.dto.response;

import java.util.List;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.enums.QuestionType;

public record QuestionResponse(Long id, QuizReferenceResponse quiz, String text, QuestionType type, int points,
        List<AnswerOptionSummaryResponse> answerOptions) {
    public static QuestionResponse from(Question question) {
        List<AnswerOptionSummaryResponse> answerOptions = question.getAnswerOptions().stream()
                .map(AnswerOptionSummaryResponse::from).toList();
        return new QuestionResponse(question.getId(), QuizReferenceResponse.from(question.getQuiz()),
                question.getText(), question.getType(), question.getPoints(), answerOptions);
    }
}
