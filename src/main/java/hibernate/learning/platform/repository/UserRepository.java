package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleOrderByNameAsc(UserRole role);

    @EntityGraph(attributePaths = {"profile"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);
}
