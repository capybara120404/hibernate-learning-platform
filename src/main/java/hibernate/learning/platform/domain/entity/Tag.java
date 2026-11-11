package hibernate.learning.platform.domain.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends AbstractEntity {
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private Set<Course> courses = new HashSet<>();

    private Tag(String name) {
        this.name = DomainPreconditions.requireNotBlank(name, "Tag name must not be blank");
    }

    public static Tag create(String name) {
        return new Tag(name);
    }

    public void updateName(String name) {
        this.name = DomainPreconditions.requireNotBlank(name, "Tag name must not be blank");
    }

    public Set<Course> getCourses() {
        return Collections.unmodifiableSet(courses);
    }

    void linkCourse(Course course) {
        courses.add(course);
    }

    void unlinkCourse(Course course) {
        courses.remove(course);
    }
}
