package hibernate.learning.platform.web.dto.request;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record TakeQuizRequest(@NotNull Long studentId,
        @NotNull Map<@NotNull Long, @NotNull Set<@NotNull Long>> answers) {
}
