package hibernate.learning.platform.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignments")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Submission> submissions = new ArrayList<>();

    private Assignment(Lesson lesson, String title, String description, LocalDate dueDate, int maxScore) {
        this.lesson = DomainPreconditions.requireNonNull(lesson, "Assignment lesson must not be null");
        this.title = DomainPreconditions.requireNotBlank(title, "Assignment title must not be blank");
        this.description = DomainPreconditions.requireNotBlank(description, "Assignment description must not be blank");
        this.dueDate = DomainPreconditions.requireNonNull(dueDate, "Assignment due date must not be null");
        this.maxScore = DomainPreconditions.requirePositive(maxScore, "Assignment max score must be positive");
    }

    static Assignment create(Lesson lesson, String title, String description, LocalDate dueDate, int maxScore) {
        return new Assignment(lesson, title, description, dueDate, maxScore);
    }

    public void updateDetails(String title, String description, LocalDate dueDate, int maxScore) {
        this.title = DomainPreconditions.requireNotBlank(title, "Assignment title must not be blank");
        this.description = DomainPreconditions.requireNotBlank(description, "Assignment description must not be blank");
        this.dueDate = DomainPreconditions.requireNonNull(dueDate, "Assignment due date must not be null");
        this.maxScore = DomainPreconditions.requirePositive(maxScore, "Assignment max score must be positive");
    }

    public List<Submission> getSubmissions() {
        return Collections.unmodifiableList(submissions);
    }

    void linkSubmission(Submission submission) {
        submissions.add(submission);
    }
}
