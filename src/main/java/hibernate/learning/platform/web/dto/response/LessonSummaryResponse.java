package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Lesson;

public record LessonSummaryResponse(Long id, String title, String content, String videoUrl, int orderIndex) {
    public static LessonSummaryResponse from(Lesson lesson) {
        return new LessonSummaryResponse(lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getVideoUrl(),
                lesson.getOrderIndex());
    }
}
