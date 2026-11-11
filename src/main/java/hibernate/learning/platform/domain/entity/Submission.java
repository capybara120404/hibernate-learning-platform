package hibernate.learning.platform.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.SubmissionStatus;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(name = "uk_submission_student_assignment", columnNames = {
        "assignment_id", "student_id"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column
    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubmissionStatus status;

    private Submission(Assignment assignment, User student, String content, LocalDateTime submittedAt) {
        this.assignment = DomainPreconditions.requireNonNull(assignment, "Submission assignment must not be null");
        this.student = DomainPreconditions.requireNonNull(student, "Submission student must not be null");
        this.content = DomainPreconditions.requireNotBlank(content, "Submission content must not be blank");
        this.submittedAt = DomainPreconditions.requireNonNull(submittedAt, "Submission submitted at must not be null");
        this.status = SubmissionStatus.SUBMITTED;
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can submit assignment");
        }
        assignment.linkSubmission(this);
        student.linkSubmission(this);
    }

    public static Submission create(Assignment assignment, User student, String content, LocalDateTime submittedAt) {
        return new Submission(assignment, student, content, submittedAt);
    }

    public void grade(int score, String feedback, int maxScore) {
        DomainPreconditions.requirePositive(maxScore, "Assignment max score must be positive");
        this.score = DomainPreconditions.requireRange(score, 0, maxScore,
                "Submission score must be between 0 and max score");
        this.feedback = feedback;
        this.status = SubmissionStatus.GRADED;
    }
}
