package hibernate.learning.platform.integration;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.repository.AssignmentRepository;
import hibernate.learning.platform.repository.LessonRepository;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.service.AssignmentService;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.service.LessonService;
import hibernate.learning.platform.service.ModuleService;
import hibernate.learning.platform.service.TagService;
import hibernate.learning.platform.service.UserService;
import hibernate.learning.platform.testsupport.AbstractIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class ContentManagementIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL_DOMAIN = "@example.com";

    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final AssignmentService assignmentService;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;

    @Test
    void moduleUpdateChangesDetailsAndRejectsOccupiedOrderIndex() {
        ContentFixture fixture = createContentFixture();
        Module second = courseService.addModule(fixture.courseId(), "Second Module", 2, "Second description");

        assertThatThrownBy(() -> moduleService.update(second.getId(), "Second Module", "Second description", 1))
                .isInstanceOf(DuplicateResourceException.class);

        Module updated = moduleService.update(fixture.moduleId(), "Renamed Module", "Renamed description", 3);
        assertThat(updated.getTitle()).isEqualTo("Renamed Module");
        assertThat(updated.getDescription()).isEqualTo("Renamed description");
        assertThat(updated.getOrderIndex()).isEqualTo(3);
        assertThat(moduleService.getById(fixture.moduleId()).getTitle()).isEqualTo("Renamed Module");
    }

    @Test
    void lessonUpdateChangesDetailsAndRejectsOccupiedOrderIndex() {
        ContentFixture fixture = createContentFixture();
        Lesson second = moduleService.addLesson(fixture.moduleId(), "Second Lesson", "Second content", "video-2", 2);

        assertThatThrownBy(() -> lessonService.update(second.getId(), "Second Lesson", "Second content", "video-2", 1))
                .isInstanceOf(DuplicateResourceException.class);

        Lesson updated = lessonService.update(fixture.lessonId(), "Renamed Lesson", "Renamed content", "video-new", 5);
        assertThat(updated.getTitle()).isEqualTo("Renamed Lesson");
        assertThat(updated.getContent()).isEqualTo("Renamed content");
        assertThat(updated.getVideoUrl()).isEqualTo("video-new");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
        assertThat(lessonService.getById(fixture.lessonId()).getOrderIndex()).isEqualTo(5);
    }

    @Test
    void assignmentUpdateChangesDetails() {
        ContentFixture fixture = createContentFixture();
        LocalDate newDueDate = LocalDate.now().plusDays(10);

        Assignment updated = assignmentService.update(fixture.assignmentId(), "Renamed Assignment",
                "Renamed assignment description", newDueDate, 50);

        assertThat(updated.getTitle()).isEqualTo("Renamed Assignment");
        assertThat(updated.getDueDate()).isEqualTo(newDueDate);
        assertThat(updated.getMaxScore()).isEqualTo(50);
        assertThat(assignmentService.getById(fixture.assignmentId()).getMaxScore()).isEqualTo(50);
    }

    @Test
    void courseTeacherAndCategoryCanBeChangedAndTagRemoved() {
        ContentFixture fixture = createContentFixture();
        String suffix = UUID.randomUUID().toString();
        User anotherTeacher = userService.create("Another Teacher " + suffix,
                "another-teacher-" + suffix + EMAIL_DOMAIN, UserRole.TEACHER);
        Category anotherCategory = categoryService.create("Another Category " + suffix);
        var tag = tagService.create("Tag " + suffix);
        courseService.addTag(fixture.courseId(), tag.getId());

        Course reassigned = courseService.changeTeacher(fixture.courseId(), anotherTeacher.getId());
        assertThat(reassigned.getTeacher().getId()).isEqualTo(anotherTeacher.getId());

        Course recategorized = courseService.changeCategory(fixture.courseId(), anotherCategory.getId());
        assertThat(recategorized.getCategory().getId()).isEqualTo(anotherCategory.getId());

        Course withoutTag = courseService.removeTag(fixture.courseId(), tag.getId());
        assertThat(withoutTag.getTags()).isEmpty();
        assertThat(courseService.getWithTags(fixture.courseId()).getTags()).isEmpty();
    }

    @Test
    void deletingModuleRemovesItsLessonsAndAssignments() {
        ContentFixture fixture = createContentFixture();

        moduleService.delete(fixture.moduleId());

        assertThat(moduleRepository.findById(fixture.moduleId())).isEmpty();
        assertThat(lessonRepository.findById(fixture.lessonId())).isEmpty();
        assertThat(assignmentRepository.findById(fixture.assignmentId())).isEmpty();
    }

    @Test
    void deletingLessonRemovesItsAssignments() {
        ContentFixture fixture = createContentFixture();

        lessonService.delete(fixture.lessonId());

        assertThat(lessonRepository.findById(fixture.lessonId())).isEmpty();
        assertThat(assignmentRepository.findById(fixture.assignmentId())).isEmpty();
        assertThat(moduleRepository.findById(fixture.moduleId())).isPresent();
    }

    private ContentFixture createContentFixture() {
        String suffix = UUID.randomUUID().toString();
        User teacher = userService.create("Content Teacher " + suffix, "content-teacher-" + suffix + EMAIL_DOMAIN,
                UserRole.TEACHER);
        Category category = categoryService.create("Content Category " + suffix);
        Course course = courseService.create(teacher.getId(), category.getId(), "Content Course " + suffix,
                "Description " + suffix, LocalDate.now().plusDays(1), 4);
        Module module = courseService.addModule(course.getId(), "Content Module " + suffix, 1, "Module description");
        Lesson lesson = moduleService.addLesson(module.getId(), "Content Lesson " + suffix, "Lesson content", "video",
                1);
        Assignment assignment = lessonService.addAssignment(lesson.getId(), "Content Assignment " + suffix,
                "Assignment description", LocalDate.now().plusDays(2), 100);
        return new ContentFixture(course.getId(), module.getId(), lesson.getId(), assignment.getId());
    }

    private record ContentFixture(Long courseId, Long moduleId, Long lessonId, Long assignmentId) {
    }
}
