package hibernate.learning.platform.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quizzes")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends AbstractEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private Module module;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "passing_score", nullable = false)
    private int passingScore;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<QuizSubmission> quizSubmissions = new ArrayList<>();

    private Quiz(Module module, String title, Integer timeLimitMinutes, int passingScore) {
        this.module = DomainPreconditions.requireNonNull(module, "Quiz module must not be null");
        this.title = DomainPreconditions.requireNotBlank(title, "Quiz title must not be blank");
        this.passingScore = DomainPreconditions.requireRange(passingScore, 0, 100,
                "Quiz passing score must be between 0 and 100");
        if (timeLimitMinutes != null) {
            this.timeLimitMinutes = DomainPreconditions.requirePositive(timeLimitMinutes,
                    "Quiz time limit minutes must be positive");
        }
        module.attachQuiz(this);
    }

    public static Quiz create(Module module, String title, Integer timeLimitMinutes, int passingScore) {
        return new Quiz(module, title, timeLimitMinutes, passingScore);
    }

    public void updateDetails(String title, Integer timeLimitMinutes, int passingScore) {
        this.title = DomainPreconditions.requireNotBlank(title, "Quiz title must not be blank");
        this.passingScore = DomainPreconditions.requireRange(passingScore, 0, 100,
                "Quiz passing score must be between 0 and 100");
        if (timeLimitMinutes != null) {
            this.timeLimitMinutes = DomainPreconditions.requirePositive(timeLimitMinutes,
                    "Quiz time limit minutes must be positive");
        } else {
            this.timeLimitMinutes = null;
        }
    }

    public Question addQuestion(String text, hibernate.learning.platform.domain.enums.QuestionType type, int points) {
        Question question = Question.create(this, text, type, points);
        questions.add(question);
        return question;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public List<QuizSubmission> getQuizSubmissions() {
        return Collections.unmodifiableList(quizSubmissions);
    }

    void linkQuizSubmission(QuizSubmission quizSubmission) {
        quizSubmissions.add(quizSubmission);
    }
}
