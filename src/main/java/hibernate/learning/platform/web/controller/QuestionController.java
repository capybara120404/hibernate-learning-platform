package hibernate.learning.platform.web.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.service.QuestionService;
import hibernate.learning.platform.web.dto.request.QuestionRequest;
import hibernate.learning.platform.web.dto.response.QuestionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        return ResponseEntity.ok(QuestionResponse.from(question));
    }

    @PutMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        Question question = questionService.update(id, request.text(), request.type(), request.points());
        return ResponseEntity.ok(QuestionResponse.from(question));
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
