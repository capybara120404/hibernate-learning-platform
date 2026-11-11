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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons", uniqueConstraints = @UniqueConstraint(name = "uk_lesson_module_order", columnNames = {
        "module_id", "order_index"}))
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 5000)
    private String content;

    @Column(name = "video_url", length = 512)
    private String videoUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE}, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Assignment> assignments = new ArrayList<>();

    private Lesson(Module module, String title, String content, String videoUrl, int orderIndex) {
        this.module = DomainPreconditions.requireNonNull(module, "Lesson module must not be null");
        this.title = DomainPreconditions.requireNotBlank(title, "Lesson title must not be blank");
        this.content = content;
        this.videoUrl = videoUrl;
        this.orderIndex = DomainPreconditions.requirePositive(orderIndex, "Lesson order index must be positive");
    }

    static Lesson create(Module module, String title, String content, String videoUrl, int orderIndex) {
        return new Lesson(module, title, content, videoUrl, orderIndex);
    }

    public void updateDetails(String title, String content, String videoUrl, int orderIndex) {
        this.title = DomainPreconditions.requireNotBlank(title, "Lesson title must not be blank");
        this.content = content;
        this.videoUrl = videoUrl;
        this.orderIndex = DomainPreconditions.requirePositive(orderIndex, "Lesson order index must be positive");
    }

    public Assignment addAssignment(String title, String description, java.time.LocalDate dueDate, int maxScore) {
        Assignment assignment = Assignment.create(this, title, description, dueDate, maxScore);
        assignments.add(assignment);
        return assignment;
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }
}
