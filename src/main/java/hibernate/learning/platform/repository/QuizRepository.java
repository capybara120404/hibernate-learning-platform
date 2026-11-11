package hibernate.learning.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByModuleId(Long moduleId);

    @EntityGraph(attributePaths = {"module", "questions"})
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    @EntityGraph(attributePaths = {"module"})
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> findByIdWithModule(@Param("id") Long id);

    @EntityGraph(attributePaths = {"module", "module.course"})
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> findByIdForGrading(@Param("id") Long id);
}
