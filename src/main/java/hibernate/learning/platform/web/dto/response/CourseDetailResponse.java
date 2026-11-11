package hibernate.learning.platform.web.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hibernate.learning.platform.domain.entity.Course;

public record CourseDetailResponse(Long id, String title, String description, LocalDate startDate, int durationWeeks,
        CategoryResponse category, UserResponse teacher, List<ModuleSummaryResponse> modules, Set<TagResponse> tags) {
    public static CourseDetailResponse from(Course course) {
        List<ModuleSummaryResponse> modules = course.getModules().stream().map(ModuleSummaryResponse::from).toList();

        Set<TagResponse> tags = course.getTags().stream().map(TagResponse::from)
                .collect(Collectors.toUnmodifiableSet());

        return new CourseDetailResponse(course.getId(), course.getTitle(), course.getDescription(),
                course.getStartDate(), course.getDurationWeeks(), CategoryResponse.from(course.getCategory()),
                UserResponse.from(course.getTeacher()), modules, tags);
    }
}
