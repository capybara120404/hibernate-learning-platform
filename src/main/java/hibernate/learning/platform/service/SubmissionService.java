package hibernate.learning.platform.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.domain.entity.Submission;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.AssignmentRepository;
import hibernate.learning.platform.repository.EnrollmentRepository;
import hibernate.learning.platform.repository.SubmissionRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public Submission submit(Long studentId, Long assignmentId, String content) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Assignment assignment = assignmentRepository.findByIdWithLesson(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can submit assignment");
        }

        Long courseId = assignment.getLesson().getModule().getCourse().getId();

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleViolationException("Student is not enrolled to course");
        }

        if (submissionRepository.existsByStudentIdAndAssignmentId(studentId, assignmentId)) {
            throw new DuplicateResourceException("Submission already exists for student and assignment");
        }

        Submission submission = Submission.create(assignment, student, content, LocalDateTime.now(clock));
        Submission saved = submissionRepository.save(submission);

        LOGGER.info("event=submission_created id={} studentId={} assignmentId={}", saved.getId(), studentId,
                assignmentId);
        return saved;
    }

    public Submission grade(Long submissionId, Long teacherId, int score, String feedback) {
        Submission submission = submissionRepository.findByIdWithDetails(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (teacher.getRole() != UserRole.TEACHER && teacher.getRole() != UserRole.ADMIN) {
            throw new BusinessRuleViolationException("Only TEACHER or ADMIN can grade submission");
        }

        Long courseId = submission.getAssignment().getLesson().getModule().getCourse().getId();
        Long courseTeacherId = submission.getAssignment().getLesson().getModule().getCourse().getTeacher().getId();

        if (teacher.getRole() == UserRole.TEACHER && !courseTeacherId.equals(teacherId)) {
            throw new BusinessRuleViolationException("Teacher is not assigned to course");
        }

        submission.grade(score, feedback, submission.getAssignment().getMaxScore());
        Submission saved = submissionRepository.save(submission);

        LOGGER.info("event=submission_graded id={} teacherId={} courseId={}", saved.getId(), teacherId, courseId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Submission getById(Long id) {
        return submissionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    @Transactional(readOnly = true)
    public List<Submission> getByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> getByStudent(Long studentId) {
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
    }
}
