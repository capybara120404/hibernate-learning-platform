package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Profile;

public record ProfileResponse(Long id, Long userId, String bio, String avatarUrl) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(profile.getId(), profile.getUser().getId(), profile.getBio(),
                profile.getAvatarUrl());
    }
}
