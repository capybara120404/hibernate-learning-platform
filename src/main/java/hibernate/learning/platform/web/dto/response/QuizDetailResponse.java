package hibernate.learning.platform.web.dto.response;

import java.util.List;

import hibernate.learning.platform.domain.entity.Quiz;

public record QuizDetailResponse(Long id, ModuleReferenceResponse module, String title, Integer timeLimitMinutes,
        int passingScore, List<QuestionSummaryResponse> questions) {
    public static QuizDetailResponse from(Quiz quiz) {
        List<QuestionSummaryResponse> questions = quiz.getQuestions().stream().map(QuestionSummaryResponse::from)
                .toList();

        return new QuizDetailResponse(quiz.getId(), ModuleReferenceResponse.from(quiz.getModule()), quiz.getTitle(),
                quiz.getTimeLimitMinutes(), quiz.getPassingScore(), questions);
    }
}
