package hibernate.learning.platform.web.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(@NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description, @NotNull LocalDate startDate,
        @NotNull @Positive Integer durationWeeks) {
}
