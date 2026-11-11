package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Quiz;

public record QuizResponse(Long id, ModuleReferenceResponse module, String title, Integer timeLimitMinutes,
        int passingScore) {
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(quiz.getId(), ModuleReferenceResponse.from(quiz.getModule()), quiz.getTitle(),
                quiz.getTimeLimitMinutes(), quiz.getPassingScore());
    }
}
