package hibernate.learning.platform.web.dto.response;

import java.time.LocalDateTime;

import hibernate.learning.platform.domain.entity.QuizSubmission;

public record QuizSubmissionResponse(Long id, QuizReferenceResponse quiz, UserResponse student, int score,
        boolean passed, LocalDateTime takenAt) {
    public static QuizSubmissionResponse from(QuizSubmission quizSubmission) {
        return new QuizSubmissionResponse(quizSubmission.getId(), QuizReferenceResponse.from(quizSubmission.getQuiz()),
                UserResponse.from(quizSubmission.getStudent()), quizSubmission.getScore(), quizSubmission.isPassed(),
                quizSubmission.getTakenAt());
    }
}
