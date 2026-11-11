package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    List<Assignment> findByLessonIdOrderByDueDateAsc(Long lessonId);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    @Query("select a from Assignment a where a.id = :id")
    Optional<Assignment> findByIdWithLesson(@Param("id") Long id);
}
