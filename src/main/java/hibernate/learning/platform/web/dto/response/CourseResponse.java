package hibernate.learning.platform.web.dto.response;

import java.time.LocalDate;

import hibernate.learning.platform.domain.entity.Course;

public record CourseResponse(Long id, String title, String description, LocalDate startDate, int durationWeeks,
        CategoryResponse category, UserResponse teacher) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getStartDate(),
                course.getDurationWeeks(), CategoryResponse.from(course.getCategory()),
                UserResponse.from(course.getTeacher()));
    }
}
