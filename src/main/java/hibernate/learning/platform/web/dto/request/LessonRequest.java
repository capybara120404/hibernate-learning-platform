package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LessonRequest(@NotBlank @Size(max = 255) String title, @Size(max = 5000) String content,
        @Size(max = 512) String videoUrl, @Positive int orderIndex) {
}
