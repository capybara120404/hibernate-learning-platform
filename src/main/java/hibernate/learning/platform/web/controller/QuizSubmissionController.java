package hibernate.learning.platform.web.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.QuizSubmission;
import hibernate.learning.platform.service.QuizSubmissionService;
import hibernate.learning.platform.web.dto.request.TakeQuizRequest;
import hibernate.learning.platform.web.dto.response.QuizSubmissionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuizSubmissionController {
    private final QuizSubmissionService quizSubmissionService;

    @PostMapping("/api/quizzes/{quizId}/submissions")
    public ResponseEntity<QuizSubmissionResponse> takeQuiz(@PathVariable Long quizId,
            @Valid @RequestBody TakeQuizRequest request) {
        QuizSubmission submission = quizSubmissionService.takeQuiz(quizId, request.studentId(), request.answers());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuizSubmissionResponse.from(submission));
    }

    @GetMapping("/api/quiz-submissions/{id}")
    public ResponseEntity<QuizSubmissionResponse> getById(@PathVariable Long id) {
        QuizSubmission submission = quizSubmissionService.getById(id);
        return ResponseEntity.ok(QuizSubmissionResponse.from(submission));
    }

    @GetMapping("/api/users/{studentId}/quiz-submissions")
    public ResponseEntity<List<QuizSubmissionResponse>> getByStudent(@PathVariable Long studentId) {
        List<QuizSubmissionResponse> submissions = quizSubmissionService.getByStudent(studentId).stream()
                .map(QuizSubmissionResponse::from).toList();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/api/quizzes/{quizId}/submissions")
    public ResponseEntity<List<QuizSubmissionResponse>> getByQuiz(@PathVariable Long quizId) {
        List<QuizSubmissionResponse> submissions = quizSubmissionService.getByQuiz(quizId).stream()
                .map(QuizSubmissionResponse::from).toList();
        return ResponseEntity.ok(submissions);
    }
}
