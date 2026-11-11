package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @EntityGraph(attributePaths = {"quiz", "answerOptions"})
    List<Question> findByQuizIdOrderByPointsAsc(Long quizId);

    @EntityGraph(attributePaths = {"quiz", "answerOptions"})
    @Query("select q from Question q where q.id = :id")
    Optional<Question> findByIdWithAnswerOptions(@Param("id") Long id);
}
