package hibernate.learning.platform.integration;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.service.TagService;
import hibernate.learning.platform.service.UserService;
import hibernate.learning.platform.testsupport.AbstractIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class LazyLoadingIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL_DOMAIN = "@example.com";

    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    @Test
    void lazyCollectionThrowsOutsideTransaction() {
        Long courseId = createCourseWithStructure();
        Course detached = courseRepository.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> detached.getModules().size()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void entityGraphPreventsLazyInitializationException() {
        Long courseId = createCourseWithStructure();
        Course fetched = courseRepository.findByIdWithModules(courseId).orElseThrow();

        assertThatCode(() -> fetched.getModules().size()).doesNotThrowAnyException();
    }

    @Test
    void serviceFetchGraphPreventsLazyInitializationException() {
        Long courseId = createCourseWithStructure();
        Course structure = courseService.getStructure(courseId);

        assertThatCode(() -> {
            structure.getModules().size();
            structure.getTags().size();
        }).doesNotThrowAnyException();
    }

    @Test
    void jpqlJoinFetchPreventsLazyInitializationException() {
        Long courseId = createCourseWithStructure();
        Course fetched = courseRepository.findByIdWithStructureFetch(courseId).orElseThrow();

        assertThatCode(() -> {
            fetched.getModules().size();
            fetched.getTags().size();
            fetched.getCategory().getName();
            fetched.getTeacher().getEmail();
        }).doesNotThrowAnyException();
        assertThat(fetched.getModules()).hasSize(2);
        assertThat(fetched.getModules().get(0).getOrderIndex()).isEqualTo(1);
        assertThat(fetched.getModules().get(1).getOrderIndex()).isEqualTo(2);
    }

    private Long createCourseWithStructure() {
        String suffix = UUID.randomUUID().toString();
        User teacher = userService.create("Teacher " + suffix, "teacher-" + suffix + EMAIL_DOMAIN, UserRole.TEACHER);
        Category category = categoryService.create("Category " + suffix);
        Tag tag = tagService.create("Tag " + suffix);
        Course course = courseService.create(teacher.getId(), category.getId(), "Lazy Course " + suffix,
                "Description " + suffix, LocalDate.now().plusDays(1), 4);
        courseService.addModule(course.getId(), "Lazy Module One " + suffix, 1, "Module description");
        courseService.addModule(course.getId(), "Lazy Module Two " + suffix, 2, "Module description");
        courseService.addTag(course.getId(), tag.getId());
        return course.getId();
    }
}
