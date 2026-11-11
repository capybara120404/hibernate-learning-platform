package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Course;

public record CourseReferenceResponse(Long id, String title) {
    public static CourseReferenceResponse from(Course course) {
        return new CourseReferenceResponse(course.getId(), course.getTitle());
    }
}
