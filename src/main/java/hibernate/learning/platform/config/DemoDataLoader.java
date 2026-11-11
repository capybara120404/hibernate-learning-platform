package hibernate.learning.platform.config;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.repository.CategoryRepository;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.repository.TagRepository;
import hibernate.learning.platform.repository.UserRepository;
import hibernate.learning.platform.service.AnswerOptionService;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.service.CourseReviewService;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.service.EnrollmentService;
import hibernate.learning.platform.service.LessonService;
import hibernate.learning.platform.service.ModuleService;
import hibernate.learning.platform.service.ProfileService;
import hibernate.learning.platform.service.QuizService;
import hibernate.learning.platform.service.QuizSubmissionService;
import hibernate.learning.platform.service.SubmissionService;
import hibernate.learning.platform.service.TagService;
import hibernate.learning.platform.service.UserService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataLoader.class);

    private static final String TEACHER_NAME = "Андрей Лисовой";
    private static final String TEACHER_EMAIL = "teacher@example.com";
    private static final String TEACHER_BIO = "Преподаватель курса по JPA и Hibernate.";
    private static final String TEACHER_AVATAR_URL = "https://example.com/avatars/teacher.png";

    private static final String STUDENT_ONE_NAME = "Иван Студентов";
    private static final String STUDENT_ONE_EMAIL = "student1@example.com";
    private static final String STUDENT_ONE_BIO = "Студент, изучает ORM.";
    private static final String STUDENT_ONE_AVATAR_URL = "https://example.com/avatars/student1.png";

    private static final String STUDENT_TWO_NAME = "Мария Тестова";
    private static final String STUDENT_TWO_EMAIL = "student2@example.com";
    private static final String STUDENT_TWO_BIO = "Студентка, готовится к экзамену по Hibernate.";
    private static final String STUDENT_TWO_AVATAR_URL = "https://example.com/avatars/student2.png";

    private static final String CATEGORY_NAME = "Programming";

    private static final String TAG_JAVA = "Java";
    private static final String TAG_HIBERNATE = "Hibernate";
    private static final String TAG_JPA = "JPA";
    private static final String TAG_POSTGRESQL = "PostgreSQL";

    private static final String COURSE_TITLE = "ORM и Hibernate";
    private static final String COURSE_DESCRIPTION = "Практический курс по JPA, Hibernate и PostgreSQL.";
    private static final int COURSE_DURATION_WEEKS = 8;
    private static final int COURSE_START_OFFSET_DAYS = 7;

    private static final String MODULE_ONE_TITLE = "Основы JPA";
    private static final String MODULE_ONE_DESCRIPTION = "Сущности, идентификаторы, маппинг и базовые связи.";
    private static final int MODULE_ONE_ORDER = 1;

    private static final String MODULE_TWO_TITLE = "Hibernate и ленивая загрузка";
    private static final String MODULE_TWO_DESCRIPTION = "Прокси, LazyInitializationException, fetch-стратегии и графы сущностей.";
    private static final int MODULE_TWO_ORDER = 2;

    private static final String LESSON_ONE_TITLE = "Entities and IDs";
    private static final String LESSON_ONE_CONTENT = "Разбираем @Entity, @Id, @GeneratedValue и таблицы пользователей.";
    private static final String LESSON_ONE_VIDEO_URL = "https://example.com/videos/jpa-entities.mp4";
    private static final int LESSON_ONE_ORDER = 1;

    private static final String LESSON_TWO_TITLE = "Relationships";
    private static final String LESSON_TWO_CONTENT = "Разбираем @OneToOne, @OneToMany, @ManyToOne и @ManyToMany.";
    private static final String LESSON_TWO_VIDEO_URL = "https://example.com/videos/jpa-relationships.mp4";
    private static final int LESSON_TWO_ORDER = 2;

    private static final String LESSON_THREE_TITLE = "Lazy Loading";
    private static final String LESSON_THREE_CONTENT = "Разбираем ленивую загрузку, прокси и LazyInitializationException.";
    private static final String LESSON_THREE_VIDEO_URL = "https://example.com/videos/hibernate-lazy-loading.mp4";
    private static final int LESSON_THREE_ORDER = 1;

    private static final String ASSIGNMENT_ONE_TITLE = "Создать модель пользователя";
    private static final String ASSIGNMENT_ONE_DESCRIPTION = "Реализовать сущность User и Profile с связью один к одному.";
    private static final int ASSIGNMENT_ONE_MAX_SCORE = 100;
    private static final int ASSIGNMENT_ONE_DUE_OFFSET_DAYS = 7;

    private static final String ASSIGNMENT_TWO_TITLE = "Объяснить ленивую загрузку";
    private static final String ASSIGNMENT_TWO_DESCRIPTION = "Подготовить письменное объяснение LazyInitializationException и способов его устранения.";
    private static final int ASSIGNMENT_TWO_MAX_SCORE = 100;
    private static final int ASSIGNMENT_TWO_DUE_OFFSET_DAYS = 10;

    private static final String SUBMISSION_ONE_CONTENT = "Добавлены сущности User и Profile, настроена связь OneToOne.";
    private static final String SUBMISSION_TWO_CONTENT = "Подготовлен ответ с описанием прокси-объектов и транзакционного контекста.";
    private static final String SUBMISSION_THREE_CONTENT = "Описаны FetchType.LAZY, JOIN FETCH и open-in-view.";

    private static final String FEEDBACK_GOOD = "Отличная работа, структура связей корректна.";
    private static final String FEEDBACK_AVERAGE = "Хорошо, но стоит подробнее описать каскадные операции.";

    private static final String QUIZ_TITLE = "Тест по Hibernate";
    private static final int QUIZ_TIME_LIMIT_MINUTES = 30;
    private static final int QUIZ_PASSING_SCORE = 60;

    private static final String QUESTION_ONE_TEXT = "Какая стратегия загрузки по умолчанию для @OneToMany?";
    private static final int QUESTION_ONE_POINTS = 1;

    private static final String QUESTION_TWO_TEXT = "Какие способы помогают избежать LazyInitializationException вне транзакции?";
    private static final int QUESTION_TWO_POINTS = 2;

    private static final String ANSWER_ONE_CORRECT_TEXT = "LAZY";
    private static final String ANSWER_ONE_INCORRECT_TEXT = "EAGER";

    private static final String ANSWER_TWO_FIRST_CORRECT_TEXT = "JOIN FETCH";
    private static final String ANSWER_TWO_SECOND_CORRECT_TEXT = "@EntityGraph";
    private static final String ANSWER_TWO_INCORRECT_TEXT = "Удаление транзакции перед доступом к коллекции";

    private static final int REVIEW_RATING_HIGH = 5;
    private static final int REVIEW_RATING_MEDIUM = 4;
    private static final String REVIEW_COMMENT_HIGH = "Очень понятный курс, особенно раздел про ленивую загрузку.";
    private static final String REVIEW_COMMENT_MEDIUM = "Хороший курс, хотелось бы больше практических заданий.";

    private static final int GRADE_EXCELLENT = 95;
    private static final int GRADE_GOOD = 80;

    private final UserService userService;
    private final ProfileService profileService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final AnswerOptionService answerOptionService;
    private final QuizSubmissionService quizSubmissionService;
    private final CourseReviewService courseReviewService;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseRepository courseRepository;

    private final Clock clock;

    @Override
    public void run(String... args) {
        if (isDemoLoaded()) {
            LOGGER.info("event=demo_data_skipped reason=already_loaded");
            return;
        }

        User teacher = getOrCreateUser(TEACHER_NAME, TEACHER_EMAIL, UserRole.TEACHER);
        User studentOne = getOrCreateUser(STUDENT_ONE_NAME, STUDENT_ONE_EMAIL, UserRole.STUDENT);
        User studentTwo = getOrCreateUser(STUDENT_TWO_NAME, STUDENT_TWO_EMAIL, UserRole.STUDENT);

        upsertProfile(teacher.getId(), TEACHER_BIO, TEACHER_AVATAR_URL);
        upsertProfile(studentOne.getId(), STUDENT_ONE_BIO, STUDENT_ONE_AVATAR_URL);
        upsertProfile(studentTwo.getId(), STUDENT_TWO_BIO, STUDENT_TWO_AVATAR_URL);

        Category category = getOrCreateCategory(CATEGORY_NAME);

        Tag javaTag = getOrCreateTag(TAG_JAVA);
        Tag hibernateTag = getOrCreateTag(TAG_HIBERNATE);
        Tag jpaTag = getOrCreateTag(TAG_JPA);
        Tag postgreSqlTag = getOrCreateTag(TAG_POSTGRESQL);

        Course course = courseService.create(teacher.getId(), category.getId(), COURSE_TITLE, COURSE_DESCRIPTION,
                LocalDate.now(clock).plusDays(COURSE_START_OFFSET_DAYS), COURSE_DURATION_WEEKS);

        List<Tag> tags = List.of(javaTag, hibernateTag, jpaTag, postgreSqlTag);
        for (Tag tag : tags) {
            courseService.addTag(course.getId(), tag.getId());
        }

        Module moduleOne = courseService.addModule(course.getId(), MODULE_ONE_TITLE, MODULE_ONE_ORDER,
                MODULE_ONE_DESCRIPTION);

        Module moduleTwo = courseService.addModule(course.getId(), MODULE_TWO_TITLE, MODULE_TWO_ORDER,
                MODULE_TWO_DESCRIPTION);

        Lesson lessonOne = moduleService.addLesson(moduleOne.getId(), LESSON_ONE_TITLE, LESSON_ONE_CONTENT,
                LESSON_ONE_VIDEO_URL, LESSON_ONE_ORDER);

        Lesson lessonTwo = moduleService.addLesson(moduleOne.getId(), LESSON_TWO_TITLE, LESSON_TWO_CONTENT,
                LESSON_TWO_VIDEO_URL, LESSON_TWO_ORDER);

        Lesson lessonThree = moduleService.addLesson(moduleTwo.getId(), LESSON_THREE_TITLE, LESSON_THREE_CONTENT,
                LESSON_THREE_VIDEO_URL, LESSON_THREE_ORDER);

        var assignmentOne = lessonService.addAssignment(lessonOne.getId(), ASSIGNMENT_ONE_TITLE,
                ASSIGNMENT_ONE_DESCRIPTION, LocalDate.now(clock).plusDays(ASSIGNMENT_ONE_DUE_OFFSET_DAYS),
                ASSIGNMENT_ONE_MAX_SCORE);

        var assignmentTwo = lessonService.addAssignment(lessonThree.getId(), ASSIGNMENT_TWO_TITLE,
                ASSIGNMENT_TWO_DESCRIPTION, LocalDate.now(clock).plusDays(ASSIGNMENT_TWO_DUE_OFFSET_DAYS),
                ASSIGNMENT_TWO_MAX_SCORE);

        enrollmentService.enroll(studentOne.getId(), course.getId());
        enrollmentService.enroll(studentTwo.getId(), course.getId());

        Submission studentOneAssignmentOne = submissionService.submit(studentOne.getId(), assignmentOne.getId(),
                SUBMISSION_ONE_CONTENT);
        submissionService.grade(studentOneAssignmentOne.getId(), teacher.getId(), GRADE_EXCELLENT, FEEDBACK_GOOD);

        Submission studentTwoAssignmentOne = submissionService.submit(studentTwo.getId(), assignmentOne.getId(),
                SUBMISSION_TWO_CONTENT);
        submissionService.grade(studentTwoAssignmentOne.getId(), teacher.getId(), GRADE_GOOD, FEEDBACK_AVERAGE);

        Submission studentOneAssignmentTwo = submissionService.submit(studentOne.getId(), assignmentTwo.getId(),
                SUBMISSION_THREE_CONTENT);
        submissionService.grade(studentOneAssignmentTwo.getId(), teacher.getId(), GRADE_EXCELLENT, FEEDBACK_GOOD);

        Quiz quiz = quizService.create(moduleTwo.getId(), QUIZ_TITLE, QUIZ_TIME_LIMIT_MINUTES, QUIZ_PASSING_SCORE);

        Question questionOne = quizService.addQuestion(quiz.getId(), QUESTION_ONE_TEXT, QuestionType.SINGLE_CHOICE,
                QUESTION_ONE_POINTS);

        var questionOneCorrectOption = answerOptionService.add(questionOne.getId(), ANSWER_ONE_CORRECT_TEXT, true);
        answerOptionService.add(questionOne.getId(), ANSWER_ONE_INCORRECT_TEXT, false);

        Question questionTwo = quizService.addQuestion(quiz.getId(), QUESTION_TWO_TEXT, QuestionType.MULTIPLE_CHOICE,
                QUESTION_TWO_POINTS);

        var questionTwoFirstCorrectOption = answerOptionService.add(questionTwo.getId(), ANSWER_TWO_FIRST_CORRECT_TEXT,
                true);
        var questionTwoSecondCorrectOption = answerOptionService.add(questionTwo.getId(),
                ANSWER_TWO_SECOND_CORRECT_TEXT, true);
        answerOptionService.add(questionTwo.getId(), ANSWER_TWO_INCORRECT_TEXT, false);

        Map<Long, Set<Long>> perfectAnswers = Map.of(questionOne.getId(), Set.of(questionOneCorrectOption.getId()),
                questionTwo.getId(),
                Set.of(questionTwoFirstCorrectOption.getId(), questionTwoSecondCorrectOption.getId()));

        Map<Long, Set<Long>> partialAnswers = Map.of(questionOne.getId(), Set.of(questionOneCorrectOption.getId()),
                questionTwo.getId(), Set.of(questionTwoFirstCorrectOption.getId()));

        quizSubmissionService.takeQuiz(quiz.getId(), studentOne.getId(), perfectAnswers);
        quizSubmissionService.takeQuiz(quiz.getId(), studentTwo.getId(), partialAnswers);

        courseReviewService.create(studentOne.getId(), course.getId(), REVIEW_RATING_HIGH, REVIEW_COMMENT_HIGH);

        courseReviewService.create(studentTwo.getId(), course.getId(), REVIEW_RATING_MEDIUM, REVIEW_COMMENT_MEDIUM);

        LOGGER.info("event=demo_data_loaded courseId={}", course.getId());
    }

    private boolean isDemoLoaded() {
        return userRepository.findByEmail(TEACHER_EMAIL)
                .flatMap(teacher -> courseRepository.findByTitleAndTeacherId(COURSE_TITLE, teacher.getId()))
                .isPresent();
    }

    private User getOrCreateUser(String name, String email, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> userService.create(name, email, role));
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name).orElseGet(() -> categoryService.create(name));
    }

    private Tag getOrCreateTag(String name) {
        return tagRepository.findByName(name).orElseGet(() -> tagService.create(name));
    }

    private void upsertProfile(Long userId, String bio, String avatarUrl) {
        profileService.upsert(userId, bio, avatarUrl);
    }
}
