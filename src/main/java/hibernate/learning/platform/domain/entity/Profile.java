package hibernate.learning.platform.domain.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends AbstractEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 1000)
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    private Profile(User user, String bio, String avatarUrl) {
        this.user = DomainPreconditions.requireNonNull(user, "Profile user must not be null");
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        user.linkProfile(this);
    }

    public static Profile create(User user, String bio, String avatarUrl) {
        return new Profile(user, bio, avatarUrl);
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
