package hibernate.learning.platform.domain;

import hibernate.learning.platform.domain.exception.InvalidDomainStateException;

public final class DomainPreconditions {
    private DomainPreconditions() {
    }

    public static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidDomainStateException(message);
        }
        return value;
    }

    public static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainStateException(message);
        }
        return value;
    }

    public static int requirePositive(int value, String message) {
        if (value <= 0) {
            throw new InvalidDomainStateException(message);
        }
        return value;
    }

    public static int requireRange(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new InvalidDomainStateException(message);
        }
        return value;
    }
}
