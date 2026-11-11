package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @EntityGraph(attributePaths = {"student", "course", "course.teacher", "course.category"})
    List<Enrollment> findByStudentIdOrderByEnrollDateDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher", "course.category"})
    List<Enrollment> findByCourseIdOrderByEnrollDateDesc(Long courseId);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher", "course.category"})
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentId(Long studentId);
    boolean existsByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher", "course.category"})
    @Query("select e from Enrollment e where e.id = :id")
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);
}
