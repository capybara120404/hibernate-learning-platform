package hibernate.learning.platform.web.dto.response;

import java.util.List;

import hibernate.learning.platform.domain.entity.Module;

public record ModuleResponse(Long id, CourseReferenceResponse course, String title, int orderIndex, String description,
        List<LessonSummaryResponse> lessons) {
    public static ModuleResponse from(Module module) {
        List<LessonSummaryResponse> lessons = module.getLessons().stream().map(LessonSummaryResponse::from).toList();

        return new ModuleResponse(module.getId(), CourseReferenceResponse.from(module.getCourse()), module.getTitle(),
                module.getOrderIndex(), module.getDescription(), lessons);
    }
}
