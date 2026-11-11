package hibernate.learning.platform.integration;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hibernate.learning.platform.domain.entity.AnswerOption;
import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.repository.AnswerOptionRepository;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.repository.QuestionRepository;
import hibernate.learning.platform.repository.QuizRepository;
import hibernate.learning.platform.service.AnswerOptionService;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.service.ModuleService;
import hibernate.learning.platform.service.QuestionService;
import hibernate.learning.platform.service.QuizService;
import hibernate.learning.platform.service.UserService;
import hibernate.learning.platform.testsupport.AbstractIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class QuizManagementIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL_DOMAIN = "@example.com";

    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;
    private final ModuleRepository moduleRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    @Test
    void quizUpdateChangesDetailsIncludingNullableTimeLimit() {
        QuizFixture fixture = createQuizFixture();

        Quiz updated = quizService.update(fixture.quizId(), "Renamed Quiz", 45, 80);
        assertThat(updated.getTitle()).isEqualTo("Renamed Quiz");
        assertThat(updated.getTimeLimitMinutes()).isEqualTo(45);
        assertThat(updated.getPassingScore()).isEqualTo(80);

        Quiz withoutTimeLimit = quizService.update(fixture.quizId(), "Renamed Quiz", null, 80);
        assertThat(withoutTimeLimit.getTimeLimitMinutes()).isNull();
        assertThat(quizService.getById(fixture.quizId()).getTimeLimitMinutes()).isNull();
    }

    @Test
    void questionUpdateChangesTextPointsAndType() {
        QuizFixture fixture = createQuizFixture();
        Question multipleChoice = quizService.addQuestion(fixture.quizId(), "Multiple Question",
                QuestionType.MULTIPLE_CHOICE, 2);
        answerOptionService.add(multipleChoice.getId(), "First correct", true);
        answerOptionService.add(multipleChoice.getId(), "Second correct", true);

        Question updated = questionService.update(multipleChoice.getId(), "Renamed Question",
                QuestionType.MULTIPLE_CHOICE, 5);
        assertThat(updated.getText()).isEqualTo("Renamed Question");
        assertThat(updated.getPoints()).isEqualTo(5);

        assertThatThrownBy(
                () -> questionService.update(multipleChoice.getId(), "Renamed Question", QuestionType.SINGLE_CHOICE, 5))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void answerOptionUpdateChangesTextAndRejectsSecondCorrectForSingleChoice() {
        QuizFixture fixture = createQuizFixture();

        AnswerOption updated = answerOptionService.update(fixture.correctOptionId(), "Updated correct text", true);
        assertThat(updated.getText()).isEqualTo("Updated correct text");
        assertThat(updated.isCorrect()).isTrue();

        assertThatThrownBy(() -> answerOptionService.update(fixture.incorrectOptionId(), "Now also correct", true))
                .isInstanceOf(BusinessRuleViolationException.class);

        AnswerOption unchanged = answerOptionService.getById(fixture.incorrectOptionId());
        assertThat(unchanged.isCorrect()).isFalse();
        assertThat(unchanged.getText()).startsWith("Incorrect ");
        assertThat(answerOptionService.getById(fixture.correctOptionId()).getText()).isEqualTo("Updated correct text");
    }

    @Test
    void answerOptionCorrectFlagCanBeRedistributedForMultipleChoice() {
        QuizFixture fixture = createQuizFixture();
        Question multipleChoice = quizService.addQuestion(fixture.quizId(), "Redistributed Question",
                QuestionType.MULTIPLE_CHOICE, 2);
        AnswerOption firstCorrect = answerOptionService.add(multipleChoice.getId(), "First correct", true);
        AnswerOption secondOption = answerOptionService.add(multipleChoice.getId(), "Second option", false);

        AnswerOption markedCorrect = answerOptionService.update(secondOption.getId(), "Second correct", true);
        assertThat(markedCorrect.isCorrect()).isTrue();

        AnswerOption markedIncorrect = answerOptionService.update(firstCorrect.getId(), "First incorrect", false);
        assertThat(markedIncorrect.isCorrect()).isFalse();

        assertThat(answerOptionService.getById(firstCorrect.getId()).isCorrect()).isFalse();
        assertThat(answerOptionService.getById(secondOption.getId()).isCorrect()).isTrue();
    }

    @Test
    void deletingIncorrectAnswerOptionKeepsQuestionValid() {
        QuizFixture fixture = createQuizFixture();

        answerOptionService.delete(fixture.incorrectOptionId());

        assertThat(answerOptionRepository.findById(fixture.incorrectOptionId())).isEmpty();
        assertThat(questionService.getById(fixture.questionId()).getAnswerOptions()).hasSize(1);
    }

    @Test
    void deletingLastCorrectAnswerOptionIsRejected() {
        QuizFixture fixture = createQuizFixture();

        assertThatThrownBy(() -> answerOptionService.delete(fixture.correctOptionId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(answerOptionRepository.findById(fixture.correctOptionId())).isPresent();
    }

    @Test
    void deletingQuestionRemovesItsAnswerOptions() {
        QuizFixture fixture = createQuizFixture();

        questionService.delete(fixture.questionId());

        assertThat(questionRepository.findById(fixture.questionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.correctOptionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.incorrectOptionId())).isEmpty();
        assertThat(quizRepository.findById(fixture.quizId())).isPresent();
    }

    @Test
    void deletingQuizRemovesQuestionsAndAnswerOptions() {
        QuizFixture fixture = createQuizFixture();

        quizService.delete(fixture.quizId());

        assertThat(quizRepository.findById(fixture.quizId())).isEmpty();
        assertThat(questionRepository.findById(fixture.questionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.correctOptionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.incorrectOptionId())).isEmpty();
        assertThat(moduleRepository.findById(fixture.moduleId())).isPresent();
    }

    @Test
    void deletingModuleRemovesQuizQuestionsAndAnswerOptions() {
        QuizFixture fixture = createQuizFixture();

        moduleService.delete(fixture.moduleId());

        assertThat(moduleRepository.findById(fixture.moduleId())).isEmpty();
        assertThat(quizRepository.findById(fixture.quizId())).isEmpty();
        assertThat(questionRepository.findById(fixture.questionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.correctOptionId())).isEmpty();
        assertThat(answerOptionRepository.findById(fixture.incorrectOptionId())).isEmpty();
    }

    private QuizFixture createQuizFixture() {
        String suffix = UUID.randomUUID().toString();
        User teacher = userService.create("Quiz Teacher " + suffix, "quiz-teacher-" + suffix + EMAIL_DOMAIN,
                UserRole.TEACHER);
        Category category = categoryService.create("Quiz Category " + suffix);
        Course course = courseService.create(teacher.getId(), category.getId(), "Quiz Course " + suffix,
                "Description " + suffix, LocalDate.now().plusDays(1), 4);
        Module module = courseService.addModule(course.getId(), "Quiz Module " + suffix, 1, "Module description");
        Quiz quiz = quizService.create(module.getId(), "Quiz " + suffix, 30, 60);
        Question question = quizService.addQuestion(quiz.getId(), "Question " + suffix, QuestionType.SINGLE_CHOICE, 1);
        AnswerOption correctOption = answerOptionService.add(question.getId(), "Correct " + suffix, true);
        AnswerOption incorrectOption = answerOptionService.add(question.getId(), "Incorrect " + suffix, false);
        return new QuizFixture(module.getId(), quiz.getId(), question.getId(), correctOption.getId(),
                incorrectOption.getId());
    }

    private record QuizFixture(Long moduleId, Long quizId, Long questionId, Long correctOptionId,
            Long incorrectOptionId) {
    }
}
