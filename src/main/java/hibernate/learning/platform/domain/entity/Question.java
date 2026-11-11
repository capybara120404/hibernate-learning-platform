package hibernate.learning.platform.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType type;

    @Column(nullable = false)
    private int points;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<AnswerOption> answerOptions = new ArrayList<>();

    private Question(Quiz quiz, String text, QuestionType type, int points) {
        this.quiz = DomainPreconditions.requireNonNull(quiz, "Question quiz must not be null");
        this.text = DomainPreconditions.requireNotBlank(text, "Question text must not be blank");
        this.type = DomainPreconditions.requireNonNull(type, "Question type must not be null");
        this.points = DomainPreconditions.requirePositive(points, "Question points must be positive");
    }

    static Question create(Quiz quiz, String text, QuestionType type, int points) {
        return new Question(quiz, text, type, points);
    }

    public void updateText(String text) {
        this.text = DomainPreconditions.requireNotBlank(text, "Question text must not be blank");
    }

    public void changeType(QuestionType type) {
        this.type = DomainPreconditions.requireNonNull(type, "Question type must not be null");
    }

    public void changePoints(int points) {
        this.points = DomainPreconditions.requirePositive(points, "Question points must be positive");
    }

    public AnswerOption addAnswerOption(String text, boolean correct) {
        AnswerOption answerOption = AnswerOption.create(this, text, correct);
        answerOptions.add(answerOption);
        return answerOption;
    }

    public void removeAnswerOption(AnswerOption answerOption) {
        answerOptions.remove(answerOption);
    }

    public void validateAnswerOptions() {
        long correctCount = answerOptions.stream().filter(AnswerOption::isCorrect).count();
        if (type == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw new BusinessRuleViolationException(
                    "Single choice question must have exactly one correct answer option");
        }
        if (type == QuestionType.MULTIPLE_CHOICE && correctCount < 1) {
            throw new BusinessRuleViolationException(
                    "Multiple choice question must have at least one correct answer option");
        }
    }

    public List<AnswerOption> getAnswerOptions() {
        return Collections.unmodifiableList(answerOptions);
    }
}
