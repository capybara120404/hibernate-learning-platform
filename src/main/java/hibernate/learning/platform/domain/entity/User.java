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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.UserRole;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AbstractEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Profile profile;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Course> coursesTaught = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<QuizSubmission> quizSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<CourseReview> reviews = new ArrayList<>();

    private User(String name, String email, UserRole role) {
        this.name = DomainPreconditions.requireNotBlank(name, "User name must not be blank");
        this.email = DomainPreconditions.requireNotBlank(email, "User email must not be blank");
        this.role = DomainPreconditions.requireNonNull(role, "User role must not be null");
    }

    public static User createStudent(String name, String email) {
        return new User(name, email, UserRole.STUDENT);
    }

    public static User createTeacher(String name, String email) {
        return new User(name, email, UserRole.TEACHER);
    }

    public static User createAdmin(String name, String email) {
        return new User(name, email, UserRole.ADMIN);
    }

    public void updateName(String name) {
        this.name = DomainPreconditions.requireNotBlank(name, "User name must not be blank");
    }

    public void updateEmail(String email) {
        this.email = DomainPreconditions.requireNotBlank(email, "User email must not be blank");
    }

    public void updateRole(UserRole role) {
        this.role = DomainPreconditions.requireNonNull(role, "User role must not be null");
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Course> getCoursesTaught() {
        return Collections.unmodifiableList(coursesTaught);
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public List<Submission> getSubmissions() {
        return Collections.unmodifiableList(submissions);
    }

    public List<QuizSubmission> getQuizSubmissions() {
        return Collections.unmodifiableList(quizSubmissions);
    }

    public List<CourseReview> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    void linkProfile(Profile profile) {
        this.profile = profile;
    }

    void linkCourse(Course course) {
        coursesTaught.add(course);
    }

    void unlinkCourse(Course course) {
        coursesTaught.remove(course);
    }

    void linkEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    void linkSubmission(Submission submission) {
        submissions.add(submission);
    }

    void linkQuizSubmission(QuizSubmission quizSubmission) {
        quizSubmissions.add(quizSubmission);
    }

    void linkReview(CourseReview review) {
        reviews.add(review);
    }
}
