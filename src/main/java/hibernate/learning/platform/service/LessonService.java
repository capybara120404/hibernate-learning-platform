package hibernate.learning.platform.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Assignment;
import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.AssignmentRepository;
import hibernate.learning.platform.repository.LessonRepository;
import hibernate.learning.platform.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonService.class);
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public Lesson getById(Long id) {
        return lessonRepository.findByIdWithModule(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }

    public Assignment addAssignment(Long lessonId, String title, String description, LocalDate dueDate, int maxScore) {
        Lesson lesson = getById(lessonId);
        Assignment assignment = lesson.addAssignment(title, description, dueDate, maxScore);
        assignment = assignmentRepository.saveAndFlush(assignment);
        LOGGER.info("event=assignment_created id={} lessonId={}", assignment.getId(), lessonId);
        return assignment;
    }

    public Lesson update(Long id, String title, String content, String videoUrl, int orderIndex) {
        Lesson lesson = getById(id);
        Long moduleId = lesson.getModule().getId();
        if (lesson.getOrderIndex() != orderIndex
                && lessonRepository.existsByModuleIdAndOrderIndexAndIdNot(moduleId, orderIndex, id)) {
            throw new DuplicateResourceException("Lesson order index already exists in module");
        }
        lesson.updateDetails(title, content, videoUrl, orderIndex);
        Lesson saved = lessonRepository.save(lesson);
        LOGGER.info("event=lesson_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Lesson lesson = getById(id);
        if (submissionRepository.existsByLessonId(id)) {
            throw new BusinessRuleViolationException("Lesson has dependent submissions");
        }
        lessonRepository.delete(lesson);
        LOGGER.info("event=lesson_deleted id={}", id);
    }
}
