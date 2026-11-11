package hibernate.learning.platform.web.dto.response;

public record ValidationError(String field, String message) {
}
