package hibernate.learning.platform.web.dto.response;

import hibernate.learning.platform.domain.entity.Assignment;

public record AssignmentReferenceResponse(Long id, String title) {
    public static AssignmentReferenceResponse from(Assignment assignment) {
        return new AssignmentReferenceResponse(assignment.getId(), assignment.getTitle());
    }
}
