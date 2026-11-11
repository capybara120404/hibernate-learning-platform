package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Quiz;

public record QuizReferenceResponse(Long id, String title) {
    public static QuizReferenceResponse from(Quiz quiz) {
        return new QuizReferenceResponse(quiz.getId(), quiz.getTitle());
    }
}
