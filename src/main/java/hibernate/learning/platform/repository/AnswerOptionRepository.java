package hibernate.learning.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    @EntityGraph(attributePaths = {"question"})
    @Query("select ao from AnswerOption ao where ao.id = :id")
    Optional<AnswerOption> findByIdWithQuestion(@Param("id") Long id);
}
