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
import jakarta.persistence.UniqueConstraint;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_course_review_student_course", columnNames = {
        "course_id", "student_id"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReview extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private CourseReview(Course course, User student, int rating, String comment, LocalDateTime createdAt) {
        this.course = DomainPreconditions.requireNonNull(course, "Course review course must not be null");
        this.student = DomainPreconditions.requireNonNull(student, "Course review student must not be null");
        this.rating = DomainPreconditions.requireRange(rating, 1, 5, "Course review rating must be between 1 and 5");
        this.comment = DomainPreconditions.requireNotBlank(comment, "Course review comment must not be blank");
        this.createdAt = DomainPreconditions.requireNonNull(createdAt, "Course review created at must not be null");

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can review course");
        }

        course.linkReview(this);
        student.linkReview(this);
    }

    public static CourseReview create(Course course, User student, int rating, String comment,
            LocalDateTime createdAt) {
        return new CourseReview(course, student, rating, comment, createdAt);
    }

    public void updateRating(int rating) {
        this.rating = DomainPreconditions.requireRange(rating, 1, 5, "Course review rating must be between 1 and 5");
    }

    public void updateComment(String comment) {
        this.comment = DomainPreconditions.requireNotBlank(comment, "Course review comment must not be blank");
    }
}
