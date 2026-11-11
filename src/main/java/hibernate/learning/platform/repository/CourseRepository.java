package hibernate.learning.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hibernate.learning.platform.domain.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"category", "teacher"})
    @Query("select c from Course c order by c.id asc")
    List<Course> findAllWithBasicReferences();

    @EntityGraph(attributePaths = {"category", "teacher"})
    List<Course> findByTeacherIdOrderByIdAsc(Long teacherId);

    @EntityGraph(attributePaths = {"category", "teacher"})
    List<Course> findByCategoryIdOrderByIdAsc(Long categoryId);

    Optional<Course> findByTitleAndTeacherId(String title, Long teacherId);

    boolean existsByTitleAndTeacherId(String title, Long teacherId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "teacher", "modules"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdWithModules(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "teacher", "tags"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdWithTags(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "teacher"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdWithBasicReferences(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "teacher", "modules", "tags"})
    @Query("select distinct c from Course c where c.id = :id")
    Optional<Course> findByIdWithStructure(@Param("id") Long id);

    @Query("""
            select distinct c from Course c
            join fetch c.category
            join fetch c.teacher
            left join fetch c.modules m
            left join fetch c.tags
            where c.id = :id
            order by m.orderIndex asc
            """)
    Optional<Course> findByIdWithStructureFetch(@Param("id") Long id);
}
