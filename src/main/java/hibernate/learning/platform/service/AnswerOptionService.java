package hibernate.learning.platform.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.AnswerOption;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.AnswerOptionRepository;
import hibernate.learning.platform.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerOptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerOptionService.class);

    private final AnswerOptionRepository answerOptionRepository;
    private final QuestionRepository questionRepository;

    public AnswerOption add(Long questionId, String text, boolean correct) {
        Question question = getQuestion(questionId);
        AnswerOption option = question.addAnswerOption(text, correct);
        try {
            question.validateAnswerOptions();
        } catch (RuntimeException validationFailure) {
            question.removeAnswerOption(option);
            throw validationFailure;
        }
        AnswerOption saved = answerOptionRepository.saveAndFlush(option);
        LOGGER.info("event=answer_option_created id={} questionId={}", saved.getId(), questionId);
        return saved;
    }

    @Transactional(readOnly = true)
    public AnswerOption getById(Long id) {
        return answerOptionRepository.findByIdWithQuestion(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer option not found"));
    }

    public AnswerOption update(Long id, String text, boolean correct) {
        Question question = getQuestionWithOption(id);
        AnswerOption option = findOption(question, id);
        option.updateText(text);
        option.markCorrect(correct);
        question.validateAnswerOptions();
        questionRepository.save(question);
        LOGGER.info("event=answer_option_updated id={} correct={}", id, correct);
        return option;
    }

    public void delete(Long id) {
        Question question = getQuestionWithOption(id);
        AnswerOption option = findOption(question, id);
        question.removeAnswerOption(option);
        question.validateAnswerOptions();
        questionRepository.save(question);
        LOGGER.info("event=answer_option_deleted id={}", id);
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findByIdWithAnswerOptions(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    private Question getQuestionWithOption(Long optionId) {
        AnswerOption option = getById(optionId);
        return getQuestion(option.getQuestion().getId());
    }

    private AnswerOption findOption(Question question, Long optionId) {
        return question.getAnswerOptions().stream().filter(opt -> Objects.equals(opt.getId(), optionId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Answer option not found"));
    }
}
