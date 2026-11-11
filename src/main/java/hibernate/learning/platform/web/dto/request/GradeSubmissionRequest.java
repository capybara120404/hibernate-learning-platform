package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeSubmissionRequest(@NotNull Long teacherId, @Min(0) int score, @Size(max = 2000) String feedback) {
}
