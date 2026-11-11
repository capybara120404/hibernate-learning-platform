package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @EntityGraph(attributePaths = {"assignment", "assignment.lesson", "assignment.lesson.module",
            "assignment.lesson.module.course", "student",})
    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.lesson", "assignment.lesson.module",
            "assignment.lesson.module.course", "student",})
    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.lesson", "assignment.lesson.module",
            "assignment.lesson.module.course", "student",})
    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.lesson", "assignment.lesson.module",
            "assignment.lesson.module.course", "student",})
    @Query("select s from Submission s where s.id = :id")
    Optional<Submission> findByIdWithDetails(@Param("id") Long id);

    @Query("select count(s) > 0 from Submission s where s.student.id = :studentId")
    boolean existsByStudentId(@Param("studentId") Long studentId);

    @Query("select count(s) > 0 from Submission s where s.assignment.id = :assignmentId")
    boolean existsByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("select count(s) > 0 from Submission s where s.assignment.lesson.id = :lessonId")
    boolean existsByLessonId(@Param("lessonId") Long lessonId);

    @Query("select count(s) > 0 from Submission s where s.assignment.lesson.module.id = :moduleId")
    boolean existsByModuleId(@Param("moduleId") Long moduleId);

    @Query("select count(s) > 0 from Submission s where s.assignment.lesson.module.course.id = :courseId")
    boolean existsByCourseId(@Param("courseId") Long courseId);

    @Query("select count(s) > 0 from Submission s where s.student.id = :studentId and s.assignment.lesson.module.course.id = :courseId")
    boolean existsByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
