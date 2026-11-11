package hibernate.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.ModuleRepository;
import hibernate.learning.platform.repository.QuestionRepository;
import hibernate.learning.platform.repository.QuizRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuestionRepository questionRepository;

    public Quiz create(Long moduleId, String title, Integer timeLimitMinutes, int passingScore) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new DuplicateResourceException("Quiz already exists for module");
        }
        Quiz quiz = Quiz.create(module, title, timeLimitMinutes, passingScore);
        Quiz saved = quizRepository.save(quiz);
        LOGGER.info("event=quiz_created id={} moduleId={}", saved.getId(), moduleId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Quiz getById(Long id) {
        return quizRepository.findByIdWithModule(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    @Transactional(readOnly = true)
    public Quiz getWithQuestions(Long id) {
        return quizRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    public Quiz update(Long id, String title, Integer timeLimitMinutes, int passingScore) {
        Quiz quiz = getById(id);
        quiz.updateDetails(title, timeLimitMinutes, passingScore);
        Quiz saved = quizRepository.save(quiz);
        LOGGER.info("event=quiz_updated id={}", saved.getId());
        return saved;
    }

    public Question addQuestion(Long quizId, String text, QuestionType type, int points) {
        Quiz quiz = getWithQuestions(quizId);
        Question question = quiz.addQuestion(text, type, points);
        question = questionRepository.saveAndFlush(question);
        LOGGER.info("event=question_created id={} quizId={}", question.getId(), quizId);
        return question;
    }

    public void delete(Long id) {
        Quiz quiz = getById(id);
        if (quizSubmissionRepository.existsByQuizId(id)) {
            throw new BusinessRuleViolationException("Quiz has dependent submissions");
        }
        Module module = quiz.getModule();
        module.detachQuiz();
        moduleRepository.save(module);
        quizRepository.delete(quiz);
        LOGGER.info("event=quiz_deleted id={}", id);
    }
}
