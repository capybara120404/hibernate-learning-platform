package hibernate.learning.platform.domain.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizScoreCalculatorTest {
    @Test
    void emptyQuestionsThrows() {
        assertThatThrownBy(() -> QuizScoreCalculator.calculate(List.of(), Map.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void nullAnswersThrows() {
        Question question = createQuestion(QuestionType.SINGLE_CHOICE);

        assertThatThrownBy(() -> QuizScoreCalculator.calculate(List.of(question), null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void answersMustContainExactlyAllQuestions() {
        Question question = createQuestion(QuestionType.SINGLE_CHOICE);
        Map<Long, Set<Long>> answers = new HashMap<>();
        answers.put(1L, Set.of(1L));

        assertThatThrownBy(() -> QuizScoreCalculator.calculate(List.of(question), answers))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void singleChoiceRejectsMultipleSelectedOptions() {
        Question question = createQuestion(QuestionType.SINGLE_CHOICE);
        Map<Long, Set<Long>> answers = new HashMap<>();
        answers.put(question.getId(), Set.of(1L, 2L));

        assertThatThrownBy(() -> QuizScoreCalculator.calculate(List.of(question), answers))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void multipleChoiceRejectsEmptySelectedOptions() {
        Question question = createQuestion(QuestionType.MULTIPLE_CHOICE);
        Map<Long, Set<Long>> answers = new HashMap<>();
        answers.put(question.getId(), Set.of());

        assertThatThrownBy(() -> QuizScoreCalculator.calculate(List.of(question), answers))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Question createQuestion(QuestionType type) {
        User teacher = User.createTeacher("Calculator Teacher", "calculator-teacher@example.com");
        Category category = Category.create("Calculator Category");
        Course course = Course.create(teacher, category, "Calculator Course", "Description", LocalDate.now(), 1);
        Module module = course.addModule("Calculator Module", 1, "Description");
        Quiz quiz = Quiz.create(module, "Calculator Quiz", 10, 60);
        return quiz.addQuestion("Calculator Question", type, 1);
    }
}
