package hibernate.learning.platform.web.controller;

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

import hibernate.learning.platform.domain.entity.AnswerOption;
import hibernate.learning.platform.service.AnswerOptionService;
import hibernate.learning.platform.web.dto.request.AnswerOptionRequest;
import hibernate.learning.platform.web.dto.response.AnswerOptionResponse;
import hibernate.learning.platform.web.dto.response.AnswerOptionSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnswerOptionController {
    private final AnswerOptionService answerOptionService;

    @PostMapping("/api/questions/{questionId}/answer-options")
    public ResponseEntity<AnswerOptionResponse> add(@PathVariable Long questionId,
            @Valid @RequestBody AnswerOptionRequest request) {
        AnswerOption option = answerOptionService.add(questionId, request.text(), request.correct());
        return ResponseEntity.status(HttpStatus.CREATED).body(AnswerOptionResponse.from(option));
    }

    @GetMapping("/api/answer-options/{id}")
    public ResponseEntity<AnswerOptionSummaryResponse> getById(@PathVariable Long id) {
        AnswerOption option = answerOptionService.getById(id);
        return ResponseEntity.ok(AnswerOptionSummaryResponse.from(option));
    }

    @PutMapping("/api/answer-options/{id}")
    public ResponseEntity<AnswerOptionResponse> update(@PathVariable Long id,
            @Valid @RequestBody AnswerOptionRequest request) {
        AnswerOption option = answerOptionService.update(id, request.text(), request.correct());
        return ResponseEntity.ok(AnswerOptionResponse.from(option));
    }

    @DeleteMapping("/api/answer-options/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        answerOptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
