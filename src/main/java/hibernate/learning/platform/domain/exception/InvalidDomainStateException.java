package hibernate.learning.platform.domain.exception;

public class InvalidDomainStateException extends DomainException {
    public InvalidDomainStateException(String message) {
        super(message);
    }
}
