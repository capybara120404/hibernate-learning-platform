package hibernate.learning.platform.domain.entity;

import java.time.LocalDate;

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
import hibernate.learning.platform.domain.enums.EnrollmentStatus;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {
        "student_id", "course_id"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enroll_date", nullable = false)
    private LocalDate enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EnrollmentStatus status;

    private Enrollment(User student, Course course, LocalDate enrollDate, EnrollmentStatus status) {
        this.student = DomainPreconditions.requireNonNull(student, "Enrollment student must not be null");
        this.course = DomainPreconditions.requireNonNull(course, "Enrollment course must not be null");
        this.enrollDate = DomainPreconditions.requireNonNull(enrollDate, "Enrollment date must not be null");
        this.status = DomainPreconditions.requireNonNull(status, "Enrollment status must not be null");
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can enroll to course");
        }
        student.linkEnrollment(this);
        course.linkEnrollment(this);
    }

    public static Enrollment create(User student, Course course, LocalDate enrollDate) {
        return new Enrollment(student, course, enrollDate, EnrollmentStatus.ACTIVE);
    }

    public static Enrollment create(User student, Course course, LocalDate enrollDate, EnrollmentStatus status) {
        return new Enrollment(student, course, enrollDate, status);
    }
}
