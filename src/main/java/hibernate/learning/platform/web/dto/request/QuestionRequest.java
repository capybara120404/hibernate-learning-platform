package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import hibernate.learning.platform.domain.enums.QuestionType;

public record QuestionRequest(@NotBlank @Size(max = 2000) String text, @NotNull QuestionType type,
        @Positive int points) {
}
