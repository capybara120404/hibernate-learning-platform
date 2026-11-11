package hibernate.learning.platform.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends AbstractEntity {
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    private List<Course> courses = new ArrayList<>();

    private Category(String name) {
        this.name = DomainPreconditions.requireNotBlank(name, "Category name must not be blank");
    }

    public static Category create(String name) {
        return new Category(name);
    }

    public void updateName(String name) {
        this.name = DomainPreconditions.requireNotBlank(name, "Category name must not be blank");
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    void linkCourse(Course course) {
        courses.add(course);
    }

    void unlinkCourse(Course course) {
        courses.remove(course);
    }
}
