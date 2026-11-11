package hibernate.learning.platform.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.AssignmentRepository;
import hibernate.learning.platform.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public Assignment getById(Long id) {
        return assignmentRepository.findByIdWithLesson(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    @Transactional(readOnly = true)
    public List<Assignment> getByLesson(Long lessonId) {
        return assignmentRepository.findByLessonIdOrderByDueDateAsc(lessonId);
    }

    public Assignment update(Long id, String title, String description, LocalDate dueDate, int maxScore) {
        Assignment assignment = getById(id);
        assignment.updateDetails(title, description, dueDate, maxScore);
        Assignment saved = assignmentRepository.save(assignment);

        LOGGER.info("event=assignment_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Assignment assignment = getById(id);

        if (submissionRepository.existsByAssignmentId(id)) {
            throw new BusinessRuleViolationException("Assignment has dependent submissions");
        }

        assignmentRepository.delete(assignment);
        LOGGER.info("event=assignment_deleted id={}", id);
    }
}
