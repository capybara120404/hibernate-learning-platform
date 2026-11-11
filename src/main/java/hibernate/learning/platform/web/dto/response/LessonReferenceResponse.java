package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Lesson;

public record LessonReferenceResponse(Long id, String title) {
    public static LessonReferenceResponse from(Lesson lesson) {
        return new LessonReferenceResponse(lesson.getId(), lesson.getTitle());
    }
}
