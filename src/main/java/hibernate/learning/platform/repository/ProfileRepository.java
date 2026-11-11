package hibernate.learning.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("select p from Profile p where p.user.id = :userId")
    Optional<Profile> findByUserIdWithUser(@Param("userId") Long userId);
}
