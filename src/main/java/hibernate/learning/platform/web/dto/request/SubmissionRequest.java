package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(@NotNull Long studentId, @NotBlank @Size(max = 5000) String content) {
}
