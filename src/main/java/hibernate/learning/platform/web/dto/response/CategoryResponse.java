package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
