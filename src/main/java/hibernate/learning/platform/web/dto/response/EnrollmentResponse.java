package hibernate.learning.platform.web.dto.response;

import java.time.LocalDate;

import hibernate.learning.platform.domain.entity.Enrollment;
import hibernate.learning.platform.domain.enums.EnrollmentStatus;

public record EnrollmentResponse(Long id, UserResponse student, CourseReferenceResponse course, LocalDate enrollDate,
        EnrollmentStatus status) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId(), UserResponse.from(enrollment.getStudent()),
                CourseReferenceResponse.from(enrollment.getCourse()), enrollment.getEnrollDate(),
                enrollment.getStatus());
    }
}
