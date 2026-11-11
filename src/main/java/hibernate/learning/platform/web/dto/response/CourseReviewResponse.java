package hibernate.learning.platform.web.dto.response;

import java.time.LocalDateTime;

import hibernate.learning.platform.domain.entity.CourseReview;

public record CourseReviewResponse(Long id, CourseReferenceResponse course, UserResponse student, int rating,
        String comment, LocalDateTime createdAt) {
    public static CourseReviewResponse from(CourseReview courseReview) {
        return new CourseReviewResponse(courseReview.getId(), CourseReferenceResponse.from(courseReview.getCourse()),
                UserResponse.from(courseReview.getStudent()), courseReview.getRating(), courseReview.getComment(),
                courseReview.getCreatedAt());
    }
}
