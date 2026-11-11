package hibernate.learning.platform.web.dto.request;

import jakarta.validation.constraints.Size;

public record ProfileRequest(@Size(max = 1000) String bio, @Size(max = 512) String avatarUrl) {
}
