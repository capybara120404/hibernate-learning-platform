package hibernate.learning.platform.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Enrollment;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.repository.CourseReviewRepository;
import hibernate.learning.platform.repository.EnrollmentRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;
import hibernate.learning.platform.repository.SubmissionRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final Clock clock;

    public Enrollment enroll(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can enroll to course");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("Student is already enrolled to course");
        }

        Enrollment enrollment = Enrollment.create(student, course, LocalDate.now(clock));
        Enrollment saved = enrollmentRepository.save(enrollment);

        LOGGER.info("event=enrollment_created id={} studentId={} courseId={}", saved.getId(), studentId, courseId);
        return saved;
    }

    public void unenroll(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (submissionRepository.existsByStudentIdAndCourseId(studentId, courseId)
                || quizSubmissionRepository.existsByStudentIdAndQuizModuleCourseId(studentId, courseId)
                || courseReviewRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleViolationException("Student has dependent learning data for course");
        }

        enrollmentRepository.delete(enrollment);
        LOGGER.info("event=enrollment_deleted studentId={} courseId={}", studentId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getByStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByEnrollDateDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getByCourse(Long courseId) {
        return enrollmentRepository.findByCourseIdOrderByEnrollDateDesc(courseId);
    }
}
