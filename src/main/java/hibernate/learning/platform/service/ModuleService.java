package hibernate.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.LessonRepository;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;
import hibernate.learning.platform.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleService.class);
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Transactional(readOnly = true)
    public Module getById(Long id) {
        return moduleRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    @Transactional(readOnly = true)
    public Module getWithLessons(Long id) {
        return moduleRepository.findByIdWithLessons(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    public Lesson addLesson(Long moduleId, String title, String content, String videoUrl, int orderIndex) {
        Module module = getWithLessons(moduleId);
        if (lessonRepository.existsByModuleIdAndOrderIndex(moduleId, orderIndex)) {
            throw new DuplicateResourceException("Lesson order index already exists in module");
        }
        Lesson lesson = module.addLesson(title, content, videoUrl, orderIndex);
        lesson = lessonRepository.saveAndFlush(lesson);
        LOGGER.info("event=lesson_created id={} moduleId={}", lesson.getId(), moduleId);
        return lesson;
    }

    public Module update(Long id, String title, String description, int orderIndex) {
        Module module = getById(id);
        Long courseId = module.getCourse().getId();
        if (module.getOrderIndex() != orderIndex
                && moduleRepository.existsByCourseIdAndOrderIndexAndIdNot(courseId, orderIndex, id)) {
            throw new DuplicateResourceException("Module order index already exists in course");
        }
        module.updateDetails(title, description, orderIndex);
        Module saved = moduleRepository.save(module);
        LOGGER.info("event=module_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Module module = getById(id);
        if (submissionRepository.existsByModuleId(id) || quizSubmissionRepository.existsByQuizModuleId(id)) {
            throw new BusinessRuleViolationException("Module has dependent learning data");
        }
        moduleRepository.delete(module);
        LOGGER.info("event=module_deleted id={}", id);
    }
}
