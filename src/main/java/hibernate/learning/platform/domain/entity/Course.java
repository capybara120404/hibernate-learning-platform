package hibernate.learning.platform.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import hibernate.learning.platform.domain.DomainPreconditions;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses", uniqueConstraints = @UniqueConstraint(name = "uk_course_teacher_title", columnNames = {
        "teacher_id", "title"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AbstractEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE,}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Module> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<CourseReview> reviews = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_tag", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Getter(AccessLevel.NONE)
    private Set<Tag> tags = new HashSet<>();

    private Course(User teacher, Category category, String title, String description, LocalDate startDate,
            Integer durationWeeks) {
        this.teacher = DomainPreconditions.requireNonNull(teacher, "Course teacher must not be null");
        this.category = DomainPreconditions.requireNonNull(category, "Course category must not be null");
        this.title = DomainPreconditions.requireNotBlank(title, "Course title must not be blank");
        this.description = DomainPreconditions.requireNotBlank(description, "Course description must not be blank");
        this.startDate = DomainPreconditions.requireNonNull(startDate, "Course start date must not be null");
        DomainPreconditions.requireNonNull(durationWeeks, "Course duration weeks must not be null");
        this.durationWeeks = DomainPreconditions.requirePositive(durationWeeks,
                "Course duration weeks must be positive");
        requireTeachingRole(this.teacher);
        teacher.linkCourse(this);
        category.linkCourse(this);
    }

    public static Course create(User teacher, Category category, String title, String description, LocalDate startDate,
            Integer durationWeeks) {
        return new Course(teacher, category, title, description, startDate, durationWeeks);
    }

    public void updateDetails(String title, String description, LocalDate startDate, Integer durationWeeks) {
        this.title = DomainPreconditions.requireNotBlank(title, "Course title must not be blank");
        this.description = DomainPreconditions.requireNotBlank(description, "Course description must not be blank");
        this.startDate = DomainPreconditions.requireNonNull(startDate, "Course start date must not be null");
        DomainPreconditions.requireNonNull(durationWeeks, "Course duration weeks must not be null");
        this.durationWeeks = DomainPreconditions.requirePositive(durationWeeks,
                "Course duration weeks must be positive");
    }

    public void changeTeacher(User teacher) {
        User validatedTeacher = DomainPreconditions.requireNonNull(teacher, "Course teacher must not be null");
        requireTeachingRole(validatedTeacher);
        this.teacher.unlinkCourse(this);
        this.teacher = validatedTeacher;
        validatedTeacher.linkCourse(this);
    }

    public void changeCategory(Category category) {
        Category validatedCategory = DomainPreconditions.requireNonNull(category, "Course category must not be null");
        this.category.unlinkCourse(this);
        this.category = validatedCategory;
        validatedCategory.linkCourse(this);
    }

    public Module addModule(String title, int orderIndex, String description) {
        Module module = Module.create(this, title, orderIndex, description);
        modules.add(module);
        return module;
    }

    public void addTag(Tag tag) {
        Tag validatedTag = DomainPreconditions.requireNonNull(tag, "Tag must not be null");
        if (tags.add(validatedTag)) {
            validatedTag.linkCourse(this);
        }
    }

    public void removeTag(Tag tag) {
        if (tags.remove(tag)) {
            tag.unlinkCourse(this);
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public List<CourseReview> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    void linkEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    void linkReview(CourseReview review) {
        reviews.add(review);
    }

    private static void requireTeachingRole(User teacher) {
        if (teacher.getRole() != UserRole.TEACHER && teacher.getRole() != UserRole.ADMIN) {
            throw new BusinessRuleViolationException("Course teacher must have TEACHER or ADMIN role");
        }
    }
}
