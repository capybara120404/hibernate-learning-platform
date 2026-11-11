package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record QuizRequest(@NotBlank @Size(max = 255) String title, @Positive Integer timeLimitMinutes,
        @Min(0) @Max(100) int passingScore) {
}
