package hibernate.learning.platform.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Enrollment;
import hibernate.learning.platform.service.EnrollmentService;
import hibernate.learning.platform.web.dto.response.EnrollmentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/api/courses/{courseId}/enrollments/{studentId}")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long courseId, @PathVariable Long studentId) {
        Enrollment enrollment = enrollmentService.enroll(studentId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentResponse.from(enrollment));
    }

    @DeleteMapping("/api/courses/{courseId}/enrollments/{studentId}")
    public ResponseEntity<Void> unenroll(@PathVariable Long courseId, @PathVariable Long studentId) {
        enrollmentService.unenroll(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(@PathVariable Long courseId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getByCourse(courseId).stream()
                .map(EnrollmentResponse::from).toList();
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/api/users/{studentId}/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getByStudent(@PathVariable Long studentId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getByStudent(studentId).stream()
                .map(EnrollmentResponse::from).toList();
        return ResponseEntity.ok(enrollments);
    }
}
