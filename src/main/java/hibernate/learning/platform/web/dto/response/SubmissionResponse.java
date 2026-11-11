package hibernate.learning.platform.web.dto.response;

import java.time.LocalDateTime;

import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.domain.enums.SubmissionStatus;

public record SubmissionResponse(Long id, AssignmentReferenceResponse assignment, UserResponse student, String content,
        Integer score, String feedback, SubmissionStatus status, LocalDateTime submittedAt) {
    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(), AssignmentReferenceResponse.from(submission.getAssignment()),
                UserResponse.from(submission.getStudent()), submission.getContent(), submission.getScore(),
                submission.getFeedback(), submission.getStatus(), submission.getSubmittedAt());
    }
}
