package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Lesson;

public record LessonResponse(Long id, ModuleReferenceResponse module, String title, String content, String videoUrl,
        int orderIndex) {
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(lesson.getId(), ModuleReferenceResponse.from(lesson.getModule()), lesson.getTitle(),
                lesson.getContent(), lesson.getVideoUrl(), lesson.getOrderIndex());
    }
}
