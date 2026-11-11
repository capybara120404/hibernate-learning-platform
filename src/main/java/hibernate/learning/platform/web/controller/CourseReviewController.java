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

import hibernate.learning.platform.domain.entity.CourseReview;
import hibernate.learning.platform.service.CourseReviewService;
import hibernate.learning.platform.web.dto.request.CourseReviewRequest;
import hibernate.learning.platform.web.dto.request.CourseReviewUpdateRequest;
import hibernate.learning.platform.web.dto.response.CourseReviewResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseReviewController {
    private final CourseReviewService courseReviewService;

    @PostMapping("/api/courses/{courseId}/reviews")
    public ResponseEntity<CourseReviewResponse> create(@PathVariable Long courseId,
            @Valid @RequestBody CourseReviewRequest request) {
        CourseReview review = courseReviewService.create(request.studentId(), courseId, request.rating(),
                request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseReviewResponse.from(review));
    }

    @GetMapping("/api/courses/{courseId}/reviews")
    public ResponseEntity<List<CourseReviewResponse>> getByCourse(@PathVariable Long courseId) {
        List<CourseReviewResponse> reviews = courseReviewService.getByCourse(courseId).stream()
                .map(CourseReviewResponse::from).toList();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/api/reviews/{id}")
    public ResponseEntity<CourseReviewResponse> getById(@PathVariable Long id) {
        CourseReview review = courseReviewService.getById(id);
        return ResponseEntity.ok(CourseReviewResponse.from(review));
    }

    @GetMapping("/api/users/{studentId}/reviews")
    public ResponseEntity<List<CourseReviewResponse>> getByStudent(@PathVariable Long studentId) {
        List<CourseReviewResponse> reviews = courseReviewService.getByStudent(studentId).stream()
                .map(CourseReviewResponse::from).toList();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/api/users/{studentId}/reviews/{reviewId}")
    public ResponseEntity<CourseReviewResponse> update(@PathVariable Long studentId, @PathVariable Long reviewId,
            @Valid @RequestBody CourseReviewUpdateRequest request) {
        CourseReview review = courseReviewService.update(reviewId, studentId, request.rating(), request.comment());
        return ResponseEntity.ok(CourseReviewResponse.from(review));
    }

    @DeleteMapping("/api/users/{studentId}/reviews/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long studentId, @PathVariable Long reviewId) {
        courseReviewService.delete(reviewId, studentId);
        return ResponseEntity.noContent().build();
    }
}
