package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ModuleRequest(@NotBlank @Size(max = 255) String title, @Positive int orderIndex,
        @Size(max = 2000) String description) {
}
