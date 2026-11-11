package hibernate.learning.platform.web.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.service.QuizService;
import hibernate.learning.platform.web.dto.request.QuestionRequest;
import hibernate.learning.platform.web.dto.request.QuizRequest;
import hibernate.learning.platform.web.dto.response.QuestionSummaryResponse;
import hibernate.learning.platform.web.dto.response.QuizDetailResponse;
import hibernate.learning.platform.web.dto.response.QuizResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/api/modules/{moduleId}/quizzes")
    public ResponseEntity<QuizResponse> create(@PathVariable Long moduleId, @Valid @RequestBody QuizRequest request) {
        Quiz quiz = quizService.create(moduleId, request.title(), request.timeLimitMinutes(), request.passingScore());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuizResponse.from(quiz));
    }

    @GetMapping("/api/quizzes/{id}")
    public ResponseEntity<QuizDetailResponse> getById(@PathVariable Long id) {
        Quiz quiz = quizService.getWithQuestions(id);
        return ResponseEntity.ok(QuizDetailResponse.from(quiz));
    }

    @GetMapping("/api/quizzes/{id}/questions")
    public ResponseEntity<List<QuestionSummaryResponse>> getQuestions(@PathVariable Long id) {
        List<QuestionSummaryResponse> questions = quizService.getWithQuestions(id).getQuestions().stream()
                .map(QuestionSummaryResponse::from).toList();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/api/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionSummaryResponse> addQuestion(@PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request) {
        Question question = quizService.addQuestion(quizId, request.text(), request.type(), request.points());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionSummaryResponse.from(question));
    }

    @PutMapping("/api/quizzes/{id}")
    public ResponseEntity<QuizResponse> update(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        Quiz quiz = quizService.update(id, request.title(), request.timeLimitMinutes(), request.passingScore());
        return ResponseEntity.ok(QuizResponse.from(quiz));
    }

    @DeleteMapping("/api/quizzes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
