package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.QuizSubmission;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    @EntityGraph(attributePaths = {"student", "quiz", "quiz.module", "quiz.module.course"})
    List<QuizSubmission> findByStudentIdOrderByTakenAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "quiz", "quiz.module", "quiz.module.course"})
    List<QuizSubmission> findByQuizIdOrderByTakenAtDesc(Long quizId);

    @EntityGraph(attributePaths = {"student", "quiz", "quiz.module", "quiz.module.course"})
    @Query("select qs from QuizSubmission qs where qs.id = :id")
    Optional<QuizSubmission> findByIdWithDetails(@Param("id") Long id);

    @Query("select count(qs) > 0 from QuizSubmission qs where qs.student.id = :studentId")
    boolean existsByStudentId(@Param("studentId") Long studentId);

    @Query("select count(qs) > 0 from QuizSubmission qs where qs.quiz.id = :quizId")
    boolean existsByQuizId(@Param("quizId") Long quizId);

    @Query("select count(qs) > 0 from QuizSubmission qs where qs.quiz.module.id = :moduleId")
    boolean existsByQuizModuleId(@Param("moduleId") Long moduleId);

    @Query("select count(qs) > 0 from QuizSubmission qs where qs.quiz.module.course.id = :courseId")
    boolean existsByQuizModuleCourseId(@Param("courseId") Long courseId);

    @Query("select count(qs) > 0 from QuizSubmission qs where qs.student.id = :studentId and qs.quiz.module.course.id = :courseId")
    boolean existsByStudentIdAndQuizModuleCourseId(@Param("studentId") Long studentId,
            @Param("courseId") Long courseId);
}
