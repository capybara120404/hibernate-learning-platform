package hibernate.learning.platform.web.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.service.AssignmentService;
import hibernate.learning.platform.web.dto.request.AssignmentRequest;
import hibernate.learning.platform.web.dto.response.AssignmentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping("/api/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getById(@PathVariable Long id) {
        Assignment assignment = assignmentService.getById(id);
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @PutMapping("/api/assignments/{id}")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        Assignment assignment = assignmentService.update(id, request.title(), request.description(), request.dueDate(),
                request.maxScore());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @DeleteMapping("/api/assignments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
