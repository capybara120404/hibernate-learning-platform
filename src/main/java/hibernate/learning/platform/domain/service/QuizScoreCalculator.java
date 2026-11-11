package hibernate.learning.platform.domain.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hibernate.learning.platform.domain.entity.AnswerOption;
import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;

public final class QuizScoreCalculator {
    private QuizScoreCalculator() {
    }

    public static int calculate(List<Question> questions, Map<Long, Set<Long>> selectedOptionIdsByQuestionId) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessRuleViolationException("Quiz must contain questions");
        }
        if (selectedOptionIdsByQuestionId == null) {
            throw new BusinessRuleViolationException("Quiz answers must not be null");
        }

        Set<Long> questionIds = new HashSet<>();
        for (Question question : questions) {
            questionIds.add(question.getId());
        }

        if (!selectedOptionIdsByQuestionId.keySet().equals(questionIds)) {
            throw new BusinessRuleViolationException("Answers must contain exactly all quiz questions");
        }

        long totalPoints = 0;
        long earnedPoints = 0;

        for (Question question : questions) {
            Set<Long> selectedOptionIds = selectedOptionIdsByQuestionId.get(question.getId());
            if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
                throw new BusinessRuleViolationException("Answer for question must not be empty");
            }

            if (question.getType() == QuestionType.SINGLE_CHOICE && selectedOptionIds.size() != 1) {
                throw new BusinessRuleViolationException("Single choice question requires exactly one selected option");
            }

            Set<Long> allOptionIds = new HashSet<>();
            Set<Long> correctOptionIds = new HashSet<>();

            for (AnswerOption option : question.getAnswerOptions()) {
                allOptionIds.add(option.getId());
                if (option.isCorrect()) {
                    correctOptionIds.add(option.getId());
                }
            }

            if (question.getType() == QuestionType.SINGLE_CHOICE && correctOptionIds.size() != 1) {
                throw new BusinessRuleViolationException("Single choice question must have exactly one correct option");
            }
            if (question.getType() == QuestionType.MULTIPLE_CHOICE && correctOptionIds.isEmpty()) {
                throw new BusinessRuleViolationException(
                        "Multiple choice question must have at least one correct option");
            }

            for (Long optionId : selectedOptionIds) {
                if (optionId == null || !allOptionIds.contains(optionId)) {
                    throw new BusinessRuleViolationException("Selected answer option does not belong to question");
                }
            }

            totalPoints += question.getPoints();
            if (selectedOptionIds.equals(correctOptionIds)) {
                earnedPoints += question.getPoints();
            }
        }

        if (totalPoints == 0) {
            throw new BusinessRuleViolationException("Quiz total points must be positive");
        }

        return (int) Math.round((double) earnedPoints * 100 / totalPoints);
    }
}
