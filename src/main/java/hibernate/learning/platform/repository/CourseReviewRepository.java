package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.CourseReview;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    @EntityGraph(attributePaths = {"course", "course.teacher", "course.category", "student",})
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @EntityGraph(attributePaths = {"course", "course.teacher", "course.category", "student",})
    List<CourseReview> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"course", "course.teacher", "course.category", "student",})
    Optional<CourseReview> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentId(Long studentId);

    boolean existsByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"course", "course.teacher", "course.category", "student",})
    @Query("select cr from CourseReview cr where cr.id = :id")
    Optional<CourseReview> findByIdWithDetails(@Param("id") Long id);
}
