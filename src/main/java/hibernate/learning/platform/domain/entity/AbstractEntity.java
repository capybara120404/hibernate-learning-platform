package hibernate.learning.platform.domain.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.Hibernate;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractEntity other)) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Long currentId = getId();
        Long otherId = other.getId();
        if (currentId == null || otherId == null) {
            return false;
        }
        return currentId.equals(otherId);
    }

    @Override
    public int hashCode() {
        Long currentId = getId();
        if (currentId == null) {
            return System.identityHashCode(this);
        }
        return currentId.hashCode();
    }
}
