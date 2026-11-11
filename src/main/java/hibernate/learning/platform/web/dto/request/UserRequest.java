package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import hibernate.learning.platform.domain.enums.UserRole;

public record UserRequest(@NotBlank @Size(max = 255) String name, @NotBlank @Email @Size(max = 255) String email,
        @NotNull UserRole role) {
}
