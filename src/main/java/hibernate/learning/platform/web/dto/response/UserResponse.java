package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;

public record UserResponse(Long id, String name, String email, UserRole role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
