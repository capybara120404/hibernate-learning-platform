package hibernate.learning.platform.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.CategoryRepository;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.repository.CourseReviewRepository;
import hibernate.learning.platform.repository.EnrollmentRepository;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;
import hibernate.learning.platform.repository.SubmissionRepository;
import hibernate.learning.platform.repository.TagRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;

    public Course create(Long teacherId, Long categoryId, String title, String description, LocalDate startDate,
            Integer durationWeeks) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (courseRepository.existsByTitleAndTeacherId(title, teacherId)) {
            throw new DuplicateResourceException("Course with title already exists for teacher");
        }
        Course course = Course.create(teacher, category, title, description, startDate, durationWeeks);
        Course saved = courseRepository.save(course);
        LOGGER.info("event=course_created id={} teacherId={}", saved.getId(), teacherId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepository.findByIdWithBasicReferences(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional(readOnly = true)
    public Course getWithModules(Long id) {
        return courseRepository.findByIdWithModules(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional(readOnly = true)
    public Course getWithTags(Long id) {
        return courseRepository.findByIdWithTags(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional(readOnly = true)
    public Course getStructure(Long id) {
        return courseRepository.findByIdWithStructureFetch(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional(readOnly = true)
    public List<Course> getAll() {
        return courseRepository.findAllWithBasicReferences();
    }

    @Transactional(readOnly = true)
    public List<Course> getByTeacher(Long teacherId) {
        return courseRepository.findByTeacherIdOrderByIdAsc(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Course> getByCategory(Long categoryId) {
        return courseRepository.findByCategoryIdOrderByIdAsc(categoryId);
    }

    public Course update(Long id, String title, String description, LocalDate startDate, Integer durationWeeks) {
        Course course = getById(id);
        Long teacherId = course.getTeacher().getId();
        if (!course.getTitle().equals(title) && courseRepository.existsByTitleAndTeacherId(title, teacherId)) {
            throw new DuplicateResourceException("Course with title already exists for teacher");
        }
        course.updateDetails(title, description, startDate, durationWeeks);
        Course saved = courseRepository.save(course);
        LOGGER.info("event=course_updated id={}", saved.getId());
        return saved;
    }

    public Course changeTeacher(Long courseId, Long teacherId) {
        Course course = getById(courseId);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        if (!course.getTeacher().getId().equals(teacherId)
                && courseRepository.existsByTitleAndTeacherId(course.getTitle(), teacherId)) {
            throw new DuplicateResourceException("Course with title already exists for teacher");
        }
        course.changeTeacher(teacher);
        Course saved = courseRepository.save(course);
        LOGGER.info("event=course_teacher_changed courseId={} teacherId={}", courseId, teacherId);
        return saved;
    }

    public Course changeCategory(Long courseId, Long categoryId) {
        Course course = getById(courseId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        course.changeCategory(category);
        Course saved = courseRepository.save(course);
        LOGGER.info("event=course_category_changed courseId={} categoryId={}", courseId, categoryId);
        return saved;
    }

    public Module addModule(Long courseId, String title, int orderIndex, String description) {
        Course course = courseRepository.findByIdWithModules(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (moduleRepository.existsByCourseIdAndOrderIndex(courseId, orderIndex)) {
            throw new DuplicateResourceException("Module order index already exists in course");
        }
        Module module = course.addModule(title, orderIndex, description);
        module = moduleRepository.saveAndFlush(module);
        LOGGER.info("event=module_created id={} courseId={}", module.getId(), courseId);
        return module;
    }

    public Course addTag(Long courseId, Long tagId) {
        Course course = courseRepository.findByIdWithTags(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        course.addTag(tag);
        Course saved = courseRepository.saveAndFlush(course);
        LOGGER.info("event=course_tag_added courseId={} tagId={}", courseId, tagId);
        return saved;
    }

    public Course removeTag(Long courseId, Long tagId) {
        Course course = courseRepository.findByIdWithTags(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        course.removeTag(tag);
        Course saved = courseRepository.saveAndFlush(course);
        LOGGER.info("event=course_tag_removed courseId={} tagId={}", courseId, tagId);
        return saved;
    }

    public void delete(Long id) {
        Course course = getById(id);
        if (enrollmentRepository.existsByCourseId(id) || submissionRepository.existsByCourseId(id)
                || quizSubmissionRepository.existsByQuizModuleCourseId(id)
                || courseReviewRepository.existsByCourseId(id)) {
            throw new BusinessRuleViolationException("Course has dependent learning data");
        }
        courseRepository.delete(course);
        LOGGER.info("event=course_deleted id={}", id);
    }
}
