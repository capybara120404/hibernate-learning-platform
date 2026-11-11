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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "modules", uniqueConstraints = @UniqueConstraint(name = "uk_module_course_order", columnNames = {
        "course_id", "order_index"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Module extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Lesson> lessons = new ArrayList<>();

    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Quiz quiz;

    private Module(Course course, String title, int orderIndex, String description) {
        this.course = DomainPreconditions.requireNonNull(course, "Module course must not be null");
        this.title = DomainPreconditions.requireNotBlank(title, "Module title must not be blank");
        this.orderIndex = DomainPreconditions.requirePositive(orderIndex, "Module order index must be positive");
        this.description = description;
    }

    static Module create(Course course, String title, int orderIndex, String description) {
        return new Module(course, title, orderIndex, description);
    }

    public void updateDetails(String title, String description, int orderIndex) {
        this.title = DomainPreconditions.requireNotBlank(title, "Module title must not be blank");
        this.description = description;
        this.orderIndex = DomainPreconditions.requirePositive(orderIndex, "Module order index must be positive");
    }

    public Lesson addLesson(String title, String content, String videoUrl, int orderIndex) {
        Lesson lesson = Lesson.create(this, title, content, videoUrl, orderIndex);
        lessons.add(lesson);
        return lesson;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public void detachQuiz() {
        this.quiz = null;
    }

    void attachQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
