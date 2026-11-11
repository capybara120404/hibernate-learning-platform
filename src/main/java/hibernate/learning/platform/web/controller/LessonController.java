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

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.service.AssignmentService;
import hibernate.learning.platform.service.LessonService;
import hibernate.learning.platform.web.dto.request.AssignmentRequest;
import hibernate.learning.platform.web.dto.request.LessonRequest;
import hibernate.learning.platform.web.dto.response.AssignmentResponse;
import hibernate.learning.platform.web.dto.response.LessonResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final AssignmentService assignmentService;

    @GetMapping("/api/lessons/{id}")
    public ResponseEntity<LessonResponse> getById(@PathVariable Long id) {
        Lesson lesson = lessonService.getById(id);
        return ResponseEntity.ok(LessonResponse.from(lesson));
    }

    @GetMapping("/api/lessons/{lessonId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@PathVariable Long lessonId) {
        List<AssignmentResponse> assignments = assignmentService.getByLesson(lessonId).stream()
                .map(AssignmentResponse::from).toList();
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/api/lessons/{lessonId}/assignments")
    public ResponseEntity<AssignmentResponse> addAssignment(@PathVariable Long lessonId,
            @Valid @RequestBody AssignmentRequest request) {
        Assignment assignment = lessonService.addAssignment(lessonId, request.title(), request.description(),
                request.dueDate(), request.maxScore());
        return ResponseEntity.status(HttpStatus.CREATED).body(AssignmentResponse.from(assignment));
    }

    @PutMapping("/api/lessons/{id}")
    public ResponseEntity<LessonResponse> update(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        Lesson lesson = lessonService.update(id, request.title(), request.content(), request.videoUrl(),
                request.orderIndex());
        return ResponseEntity.ok(LessonResponse.from(lesson));
    }

    @DeleteMapping("/api/lessons/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
