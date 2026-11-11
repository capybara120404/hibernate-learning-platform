package hibernate.learning.platform.web.dto.response;

import java.time.LocalDate;

import hibernate.learning.platform.domain.entity.Assignment;

public record AssignmentResponse(Long id, LessonReferenceResponse lesson, String title, String description,
        LocalDate dueDate, int maxScore) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(assignment.getId(), LessonReferenceResponse.from(assignment.getLesson()),
                assignment.getTitle(), assignment.getDescription(), assignment.getDueDate(), assignment.getMaxScore());
    }
}
