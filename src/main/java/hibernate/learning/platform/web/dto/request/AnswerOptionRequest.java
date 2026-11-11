package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerOptionRequest(@NotBlank @Size(max = 1000) String text, boolean correct) {
}
