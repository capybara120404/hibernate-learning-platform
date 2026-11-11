package hibernate.learning.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    boolean existsByModuleIdAndOrderIndex(Long moduleId, int orderIndex);
    boolean existsByModuleIdAndOrderIndexAndIdNot(Long moduleId, int orderIndex, Long id);

    @EntityGraph(attributePaths = {"module"})
    @Query("select l from Lesson l where l.id = :id")
    Optional<Lesson> findByIdWithModule(@Param("id") Long id);
}
