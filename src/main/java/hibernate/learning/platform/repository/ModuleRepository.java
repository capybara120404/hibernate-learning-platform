package hibernate.learning.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Module;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    boolean existsByCourseIdAndOrderIndex(Long courseId, int orderIndex);
    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, int orderIndex, Long id);

    @EntityGraph(attributePaths = {"course"})
    @Query("select m from Module m where m.id = :id")
    Optional<Module> findByIdWithCourse(@Param("id") Long id);

    @EntityGraph(attributePaths = {"course", "lessons"})
    @Query("select m from Module m where m.id = :id")
    Optional<Module> findByIdWithLessons(@Param("id") Long id);
}
