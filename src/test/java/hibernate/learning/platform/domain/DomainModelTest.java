package hibernate.learning.platform.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.CourseReview;
import hibernate.learning.platform.domain.entity.Enrollment;
import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.QuizSubmission;
import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.InvalidDomainStateException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelTest {
    @Test
    void userFactoryRejectsBlankName() {
        assertThatThrownBy(() -> User.createStudent(" ", "student@example.com"))
                .isInstanceOf(InvalidDomainStateException.class);
    }

    @Test
    void courseFactoryRejectsNonTeacher() {
        User student = User.createStudent("Student", "student-domain@example.com");
        Category category = Category.create("Domain Category");

        assertThatThrownBy(() -> Course.create(student, category, "Course", "Description", LocalDate.now(), 1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void enrollmentRejectsNonStudent() {
        User teacher = User.createTeacher("Teacher", "teacher-domain@example.com");
        Category category = Category.create("Enrollment Category");
        Course course = Course.create(teacher, category, "Course", "Description", LocalDate.now(), 1);

        assertThatThrownBy(() -> Enrollment.create(teacher, course, LocalDate.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void submissionGradeRejectsOutOfRangeScore() {
        User teacher = User.createTeacher("Teacher", "grade-teacher@example.com");
        User student = User.createStudent("Student", "grade-student@example.com");
        Category category = Category.create("Grade Category");
        Course course = Course.create(teacher, category, "Grade Course", "Description", LocalDate.now(), 1);
        Module module = course.addModule("Grade Module", 1, "Description");
        Lesson lesson = module.addLesson("Grade Lesson", "Content", "video", 1);
        var assignment = lesson.addAssignment("Grade Assignment", "Description", LocalDate.now().plusDays(1), 10);
        Submission submission = Submission.create(assignment, student, "Answer", LocalDateTime.now());

        assertThatThrownBy(() -> submission.grade(-1, "feedback", 10)).isInstanceOf(InvalidDomainStateException.class);
        assertThatThrownBy(() -> submission.grade(11, "feedback", 10)).isInstanceOf(InvalidDomainStateException.class);

        submission.grade(10, "feedback", 10);
        assertThat(submission.getScore()).isEqualTo(10);
    }

    @Test
    void questionValidationRequiresCorrectAnswerOptions() {
        User teacher = User.createTeacher("Teacher", "question-teacher@example.com");
        Category category = Category.create("Question Category");
        Course course = Course.create(teacher, category, "Question Course", "Description", LocalDate.now(), 1);
        Module module = course.addModule("Question Module", 1, "Description");
        Quiz quiz = Quiz.create(module, "Question Quiz", 10, 60);

        Question singleChoice = quiz.addQuestion("Single", QuestionType.SINGLE_CHOICE, 1);
        singleChoice.addAnswerOption("Wrong", false);
        assertThatThrownBy(singleChoice::validateAnswerOptions).isInstanceOf(BusinessRuleViolationException.class);

        singleChoice.addAnswerOption("Right", true);
        singleChoice.validateAnswerOptions();

        Question multipleChoice = quiz.addQuestion("Multiple", QuestionType.MULTIPLE_CHOICE, 1);
        multipleChoice.addAnswerOption("Wrong", false);
        assertThatThrownBy(multipleChoice::validateAnswerOptions).isInstanceOf(BusinessRuleViolationException.class);

        multipleChoice.addAnswerOption("Right", true);
        multipleChoice.validateAnswerOptions();
    }

    @Test
    void quizSubmissionRejectsScoreOutOfRange() {
        User teacher = User.createTeacher("Teacher", "quiz-teacher@example.com");
        User student = User.createStudent("Student", "quiz-student@example.com");
        Category category = Category.create("Quiz Category");
        Course course = Course.create(teacher, category, "Quiz Course", "Description", LocalDate.now(), 1);
        Module module = course.addModule("Quiz Module", 1, "Description");
        Quiz quiz = Quiz.create(module, "Quiz", 10, 60);

        assertThatThrownBy(() -> QuizSubmission.create(quiz, student, 101, LocalDateTime.now()))
                .isInstanceOf(InvalidDomainStateException.class);
    }

    @Test
    void courseReviewRejectsRatingOutOfRange() {
        User teacher = User.createTeacher("Teacher", "review-teacher@example.com");
        User student = User.createStudent("Student", "review-student@example.com");
        Category category = Category.create("Review Category");
        Course course = Course.create(teacher, category, "Review Course", "Description", LocalDate.now(), 1);

        assertThatThrownBy(() -> CourseReview.create(course, student, 0, "Comment", LocalDateTime.now()))
                .isInstanceOf(InvalidDomainStateException.class);
        assertThatThrownBy(() -> CourseReview.create(course, student, 6, "Comment", LocalDateTime.now()))
                .isInstanceOf(InvalidDomainStateException.class);
    }
}
