package hibernate.learning.platform.web.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.service.SubmissionService;
import hibernate.learning.platform.web.dto.request.GradeSubmissionRequest;
import hibernate.learning.platform.web.dto.request.SubmissionRequest;
import hibernate.learning.platform.web.dto.response.SubmissionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping("/api/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionRequest request) {
        Submission submission = submissionService.submit(request.studentId(), assignmentId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubmissionResponse.from(submission));
    }

    @GetMapping("/api/submissions/{id}")
    public ResponseEntity<SubmissionResponse> getById(@PathVariable Long id) {
        Submission submission = submissionService.getById(id);
        return ResponseEntity.ok(SubmissionResponse.from(submission));
    }

    @PutMapping("/api/submissions/{id}/grade")
    public ResponseEntity<SubmissionResponse> grade(@PathVariable Long id,
            @Valid @RequestBody GradeSubmissionRequest request) {
        Submission submission = submissionService.grade(id, request.teacherId(), request.score(), request.feedback());
        return ResponseEntity.ok(SubmissionResponse.from(submission));
    }

    @GetMapping("/api/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getByAssignment(@PathVariable Long assignmentId) {
        List<SubmissionResponse> submissions = submissionService.getByAssignment(assignmentId).stream()
                .map(SubmissionResponse::from).toList();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/api/users/{studentId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getByStudent(@PathVariable Long studentId) {
        List<SubmissionResponse> submissions = submissionService.getByStudent(studentId).stream()
                .map(SubmissionResponse::from).toList();
        return ResponseEntity.ok(submissions);
    }
}
