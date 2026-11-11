package hibernate.learning.platform.domain.exception;

public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
