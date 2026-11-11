package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseReviewRequest(@NotNull Long studentId, @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 2000) String comment) {
}
