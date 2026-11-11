package hibernate.learning.platform.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_submissions")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmission extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    private QuizSubmission(Quiz quiz, User student, int score, int passingScore, LocalDateTime takenAt) {
        this.quiz = DomainPreconditions.requireNonNull(quiz, "Quiz submission quiz must not be null");
        this.student = DomainPreconditions.requireNonNull(student, "Quiz submission student must not be null");
        this.score = DomainPreconditions.requireRange(score, 0, 100, "Quiz submission score must be between 0 and 100");
        this.takenAt = DomainPreconditions.requireNonNull(takenAt, "Quiz submission taken at must not be null");
        this.passed = score >= passingScore;
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can take quiz");
        }
        quiz.linkQuizSubmission(this);
        student.linkQuizSubmission(this);
    }

    public static QuizSubmission create(Quiz quiz, User student, int score, LocalDateTime takenAt) {
        return new QuizSubmission(quiz, student, score, quiz.getPassingScore(), takenAt);
    }
}
