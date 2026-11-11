package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Module;

public record ModuleSummaryResponse(Long id, String title, int orderIndex, String description) {
    public static ModuleSummaryResponse from(Module module) {
        return new ModuleSummaryResponse(module.getId(), module.getTitle(), module.getOrderIndex(),
                module.getDescription());
    }
}
