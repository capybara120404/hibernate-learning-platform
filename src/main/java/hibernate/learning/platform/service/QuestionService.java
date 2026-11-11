package hibernate.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.QuestionRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Transactional(readOnly = true)
    public Question getById(Long id) {
        return questionRepository.findByIdWithAnswerOptions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    public Question update(Long id, String text, QuestionType type, int points) {
        Question question = getById(id);
        question.updateText(text);
        question.changeType(type);
        question.changePoints(points);
        question.validateAnswerOptions();
        Question saved = questionRepository.save(question);
        LOGGER.info("event=question_updated id={} type={} points={}", saved.getId(), type, points);
        return saved;
    }

    public void delete(Long id) {
        Question question = getById(id);
        Long quizId = question.getQuiz().getId();
        if (quizSubmissionRepository.existsByQuizId(quizId)) {
            throw new BusinessRuleViolationException("Question belongs to quiz with dependent submissions");
        }
        questionRepository.delete(question);
        LOGGER.info("event=question_deleted id={}", id);
    }
}
