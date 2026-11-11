package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    List<Tag> findAllByOrderByNameAsc();

    @Query("select count(c) > 0 from Course c join c.tags t where t.id = :tagId")
    boolean existsCourseAssigned(@Param("tagId") Long tagId);
}
