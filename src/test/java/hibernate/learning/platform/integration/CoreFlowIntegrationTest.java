package hibernate.learning.platform.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.CourseReview;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Profile;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.QuizSubmission;
import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.enums.SubmissionStatus;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.InvalidDomainStateException;
import hibernate.learning.platform.repository.AssignmentRepository;
import hibernate.learning.platform.repository.CategoryRepository;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.repository.LessonRepository;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.repository.ProfileRepository;
import hibernate.learning.platform.repository.TagRepository;
import hibernate.learning.platform.repository.UserRepository;
import hibernate.learning.platform.service.AnswerOptionService;
import hibernate.learning.platform.service.AssignmentService;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.service.CourseReviewService;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.service.EnrollmentService;
import hibernate.learning.platform.service.LessonService;
import hibernate.learning.platform.service.ModuleService;
import hibernate.learning.platform.service.ProfileService;
import hibernate.learning.platform.service.QuestionService;
import hibernate.learning.platform.service.QuizService;
import hibernate.learning.platform.service.QuizSubmissionService;
import hibernate.learning.platform.service.SubmissionService;
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
class CoreFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL_DOMAIN = "@example.com";
    private static final Set<String> EXPECTED_TABLES = Set.of("users", "profiles", "categories", "courses",
            "enrollments", "modules", "lessons", "assignments", "submissions", "quizzes", "questions", "answer_options",
            "quiz_submissions", "course_reviews", "tags", "course_tag");

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final ProfileService profileService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;
    private final QuizSubmissionService quizSubmissionService;
    private final CourseReviewService courseReviewService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;

    @Test
    void schemaContainsExpectedTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'", String.class);
        assertThat(tables).containsAll(EXPECTED_TABLES);
    }

    @Test
    void userProfileCrudAndCascadeDelete() {
        String suffix = newSuffix();
        User teacher = userService.create("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Profile profile = profileService.upsert(teacher.getId(), "Bio " + suffix, "avatar-" + suffix);
        assertThat(profile.getUser().getId()).isEqualTo(teacher.getId());

        User updated = userService.update(teacher.getId(), "Updated " + suffix, email("updated", suffix),
                UserRole.ADMIN);
        assertThat(updated.getEmail()).isEqualTo(email("updated", suffix));

        userService.delete(updated.getId());
        assertThat(userRepository.findById(updated.getId())).isEmpty();
        assertThat(profileRepository.findByUserId(updated.getId())).isEmpty();
    }

    @Test
    void categoryAndTagCrudWithDuplicates() {
        String suffix = newSuffix();
        Category category = categoryService.create("Category " + suffix);
        assertThatThrownBy(() -> categoryService.create("Category " + suffix))
                .isInstanceOf(DuplicateResourceException.class);

        Category updatedCategory = categoryService.update(category.getId(), "Updated " + suffix);
        Tag tag = tagService.create("Tag " + suffix);
        assertThatThrownBy(() -> tagService.create("Tag " + suffix)).isInstanceOf(DuplicateResourceException.class);

        tagService.delete(tag.getId());
        categoryService.delete(updatedCategory.getId());
        assertThat(categoryRepository.findById(updatedCategory.getId())).isEmpty();
        assertThat(tagRepository.findById(tag.getId())).isEmpty();
    }

    @Test
    void courseStructureCascadeRead() {
        CourseFixture fixture = createCourseFixture(newSuffix());

        Course course = courseService.getStructure(fixture.courseId());
        assertThat(course.getModules()).hasSize(1);

        Module module = moduleService.getWithLessons(fixture.moduleId());
        assertThat(module.getLessons()).hasSize(1);
        assertThat(assignmentService.getByLesson(fixture.lessonId())).hasSize(1);
    }

    @Test
    void enrollmentUniqueListsAndUnenroll() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);

        enrollmentService.enroll(fixture.studentId(), fixture.courseId());
        assertThatThrownBy(() -> enrollmentService.enroll(fixture.studentId(), fixture.courseId()))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(enrollmentService.getByCourse(fixture.courseId())).hasSize(1);
        assertThat(enrollmentService.getByStudent(fixture.studentId())).hasSize(1);

        enrollmentService.unenroll(fixture.studentId(), fixture.courseId());
        assertThat(enrollmentService.getByCourse(fixture.courseId())).isEmpty();
        assertThat(enrollmentService.getByStudent(fixture.studentId())).isEmpty();
    }

    @Test
    void submissionUniqueGradeAndQueries() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());

        Submission submission = submissionService.submit(fixture.studentId(), fixture.assignmentId(),
                "Answer " + suffix);
        assertThatThrownBy(() -> submissionService.submit(fixture.studentId(), fixture.assignmentId(), "Duplicate"))
                .isInstanceOf(DuplicateResourceException.class);

        Submission graded = submissionService.grade(submission.getId(), fixture.teacherId(), 90, "Feedback " + suffix);
        assertThat(graded.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(graded.getScore()).isEqualTo(90);
        assertThatThrownBy(() -> submissionService.grade(submission.getId(), fixture.teacherId(), 101, "Bad"))
                .isInstanceOf(InvalidDomainStateException.class);

        assertThat(submissionService.getByAssignment(fixture.assignmentId())).hasSize(1);
        assertThat(submissionService.getByStudent(fixture.studentId())).hasSize(1);
    }

    @Test
    void quizScoringPersistsResults() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());

        Quiz quiz = quizService.create(fixture.moduleId(), "Quiz " + suffix, 30, 60);
        Question questionOne = quizService.addQuestion(quiz.getId(), "Q1 " + suffix, QuestionType.SINGLE_CHOICE, 1);
        var questionOneCorrect = answerOptionService.add(questionOne.getId(), "A " + suffix, true);
        answerOptionService.add(questionOne.getId(), "B " + suffix, false);

        Question questionTwo = quizService.addQuestion(quiz.getId(), "Q2 " + suffix, QuestionType.MULTIPLE_CHOICE, 2);
        var questionTwoFirstCorrect = answerOptionService.add(questionTwo.getId(), "C " + suffix, true);
        var questionTwoSecondCorrect = answerOptionService.add(questionTwo.getId(), "D " + suffix, true);
        answerOptionService.add(questionTwo.getId(), "E " + suffix, false);

        Map<Long, Set<Long>> perfectAnswers = Map.of(questionOne.getId(), Set.of(questionOneCorrect.getId()),
                questionTwo.getId(), Set.of(questionTwoFirstCorrect.getId(), questionTwoSecondCorrect.getId()));
        Map<Long, Set<Long>> partialAnswers = Map.of(questionOne.getId(), Set.of(questionOneCorrect.getId()),
                questionTwo.getId(), Set.of(questionTwoFirstCorrect.getId()));

        QuizSubmission perfectSubmission = quizSubmissionService.takeQuiz(quiz.getId(), fixture.studentId(),
                perfectAnswers);
        assertThat(perfectSubmission.getScore()).isEqualTo(100);
        assertThat(perfectSubmission.isPassed()).isTrue();

        QuizSubmission partialSubmission = quizSubmissionService.takeQuiz(quiz.getId(), fixture.studentId(),
                partialAnswers);
        assertThat(partialSubmission.getScore()).isEqualTo(33);
        assertThat(partialSubmission.isPassed()).isFalse();

        assertThat(quizSubmissionService.getByStudent(fixture.studentId())).hasSize(2);
        assertThat(quizSubmissionService.getByQuiz(quiz.getId())).hasSize(2);
    }

    @Test
    void courseReviewUniqueUpdateAndDelete() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());

        CourseReview review = courseReviewService.create(fixture.studentId(), fixture.courseId(), 5, "Great " + suffix);
        assertThatThrownBy(() -> courseReviewService.create(fixture.studentId(), fixture.courseId(), 5, "Duplicate"))
                .isInstanceOf(DuplicateResourceException.class);

        User anotherStudent = createUser("another-student", newSuffix(), UserRole.STUDENT);
        assertThatThrownBy(
                () -> courseReviewService.create(anotherStudent.getId(), fixture.courseId(), 5, "Not enrolled"))
                .isInstanceOf(BusinessRuleViolationException.class);

        CourseReview updated = courseReviewService.update(review.getId(), fixture.studentId(), 4, "Ok " + suffix);
        assertThat(updated.getRating()).isEqualTo(4);

        courseReviewService.delete(updated.getId(), fixture.studentId());
        assertThat(courseReviewService.getByCourse(fixture.courseId())).isEmpty();
    }

    @Test
    void deletionIsBlockedByLearningDependencies() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());
        submissionService.submit(fixture.studentId(), fixture.assignmentId(), "Answer " + suffix);

        assertThatThrownBy(() -> courseService.delete(fixture.courseId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> moduleService.delete(fixture.moduleId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> lessonService.delete(fixture.lessonId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> assignmentService.delete(fixture.assignmentId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void userDeletionIsBlockedByEnrollment() {
        CourseFixture fixture = createCourseFixture(newSuffix());
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());

        assertThatThrownBy(() -> userService.delete(fixture.studentId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void courseGraphIsPersistedAndRemovedByCascade() {
        String suffix = newSuffix();
        User teacher = createUser("cascade-teacher", suffix, UserRole.TEACHER);
        Category category = categoryService.create("Cascade Category " + suffix);

        Course course = Course.create(teacher, category, "Cascade Course " + suffix, "Description " + suffix,
                LocalDate.now().plusDays(1), 4);
        Module module = course.addModule("Cascade Module " + suffix, 1, "Module description");
        var lesson = module.addLesson("Cascade Lesson " + suffix, "Lesson content", "video", 1);
        var assignment = lesson.addAssignment("Cascade Assignment " + suffix, "Assignment description",
                LocalDate.now().plusDays(2), 100);

        Course persisted = courseRepository.saveAndFlush(course);
        Long courseId = persisted.getId();
        Long moduleId = module.getId();
        Long lessonId = lesson.getId();
        Long assignmentId = assignment.getId();

        assertThat(moduleId).isNotNull();
        assertThat(lessonId).isNotNull();
        assertThat(assignmentId).isNotNull();
        assertThat(moduleRepository.findById(moduleId)).isPresent();
        assertThat(lessonRepository.findById(lessonId)).isPresent();
        assertThat(assignmentRepository.findById(assignmentId)).isPresent();

        courseService.delete(courseId);

        assertThat(courseRepository.findById(courseId)).isEmpty();
        assertThat(moduleRepository.findById(moduleId)).isEmpty();
        assertThat(lessonRepository.findById(lessonId)).isEmpty();
        assertThat(assignmentRepository.findById(assignmentId)).isEmpty();
    }

    @Test
    void courseUpdateRejectsDuplicateTitleOfSameTeacher() {
        String suffix = newSuffix();
        CourseFixture first = createCourseFixture(suffix);
        User teacher = userService.getById(first.teacherId());
        Category category = categoryService.getById(first.categoryId());
        Course second = courseService.create(teacher.getId(), category.getId(), "Second Course " + suffix,
                "Description " + suffix, LocalDate.now().plusDays(1), 4);

        assertThatThrownBy(() -> courseService.update(second.getId(), "Course " + suffix, "Description " + suffix,
                LocalDate.now().plusDays(1), 4)).isInstanceOf(DuplicateResourceException.class);

        Course updated = courseService.update(second.getId(), "Renamed Course " + suffix, "New description " + suffix,
                LocalDate.now().plusDays(3), 6);
        assertThat(updated.getTitle()).isEqualTo("Renamed Course " + suffix);
        assertThat(updated.getDurationWeeks()).isEqualTo(6);
    }

    @Test
    void userRoleChangeIsRejectedWhenLearningDataExists() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        enrollmentService.enroll(fixture.studentId(), fixture.courseId());
        User student = userService.getById(fixture.studentId());

        assertThatThrownBy(
                () -> userService.update(student.getId(), student.getName(), student.getEmail(), UserRole.TEACHER))
                .isInstanceOf(BusinessRuleViolationException.class);

        User renamed = userService.update(student.getId(), "Renamed " + suffix, student.getEmail(), UserRole.STUDENT);
        assertThat(renamed.getName()).isEqualTo("Renamed " + suffix);
    }

    @Test
    void singleChoiceQuestionRejectsSecondCorrectOption() {
        String suffix = newSuffix();
        CourseFixture fixture = createCourseFixture(suffix);
        Quiz quiz = quizService.create(fixture.moduleId(), "Single Choice Quiz " + suffix, 30, 60);
        Question question = quizService.addQuestion(quiz.getId(), "Q " + suffix, QuestionType.SINGLE_CHOICE, 1);
        answerOptionService.add(question.getId(), "Correct " + suffix, true);

        assertThatThrownBy(() -> answerOptionService.add(question.getId(), "Another correct " + suffix, true))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(questionService.getById(question.getId()).getAnswerOptions()).hasSize(1);
    }

    private CourseFixture createCourseFixture(String suffix) {
        User teacher = createUser("teacher", suffix, UserRole.TEACHER);
        User student = createUser("student", suffix, UserRole.STUDENT);
        Category category = categoryService.create("Category " + suffix);
        Course course = courseService.create(teacher.getId(), category.getId(), "Course " + suffix,
                "Description " + suffix, LocalDate.now().plusDays(1), 4);
        Module module = courseService.addModule(course.getId(), "Module " + suffix, 1, "Module description");
        var lesson = moduleService.addLesson(module.getId(), "Lesson " + suffix, "Lesson content", "video", 1);
        var assignment = lessonService.addAssignment(lesson.getId(), "Assignment " + suffix, "Assignment description",
                LocalDate.now().plusDays(2), 100);
        return new CourseFixture(teacher.getId(), student.getId(), category.getId(), course.getId(), module.getId(),
                lesson.getId(), assignment.getId());
    }

    private User createUser(String prefix, String suffix, UserRole role) {
        return userService.create(prefix.charAt(0) + prefix.substring(1) + " " + suffix, email(prefix, suffix), role);
    }

    private String newSuffix() {
        return UUID.randomUUID().toString();
    }

    private String email(String prefix, String suffix) {
        return prefix + "-" + suffix + EMAIL_DOMAIN;
    }

    private record CourseFixture(Long teacherId, Long studentId, Long categoryId, Long courseId, Long moduleId,
            Long lessonId, Long assignmentId) {
    }
}
