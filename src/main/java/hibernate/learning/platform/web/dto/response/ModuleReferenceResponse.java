package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Module;

public record ModuleReferenceResponse(Long id, String title, int orderIndex) {
    public static ModuleReferenceResponse from(Module module) {
        return new ModuleReferenceResponse(module.getId(), module.getTitle(), module.getOrderIndex());
    }
}
