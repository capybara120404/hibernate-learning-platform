package hibernate.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Profile;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.ProfileRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Profile getByUserId(Long userId) {
        return profileRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public Profile upsert(Long userId, String bio, String avatarUrl) {
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = user.getProfile();

        if (profile == null) {
            profile = Profile.create(user, bio, avatarUrl);
        } else {
            profile.updateBio(bio);
            profile.updateAvatarUrl(avatarUrl);
        }

        return profileRepository.save(profile);
    }
}
