package hibernate.learning.platform.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Question;
import hibernate.learning.platform.domain.entity.Quiz;
import hibernate.learning.platform.domain.entity.QuizSubmission;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.domain.service.QuizScoreCalculator;
import hibernate.learning.platform.repository.EnrollmentRepository;
import hibernate.learning.platform.repository.QuestionRepository;
import hibernate.learning.platform.repository.QuizRepository;
import hibernate.learning.platform.repository.QuizSubmissionRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizSubmissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizSubmissionService.class);

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public QuizSubmission takeQuiz(Long quizId, Long studentId, Map<Long, Set<Long>> selectedOptionIdsByQuestionId) {
        Quiz quiz = quizRepository.findByIdForGrading(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can take quiz");
        }

        Long courseId = quiz.getModule().getCourse().getId();

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleViolationException("Student is not enrolled to course");
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByPointsAsc(quizId);
        int score = QuizScoreCalculator.calculate(questions, selectedOptionIdsByQuestionId);

        QuizSubmission submission = QuizSubmission.create(quiz, student, score, LocalDateTime.now(clock));
        QuizSubmission saved = quizSubmissionRepository.save(submission);

        LOGGER.info("event=quiz_submission_created id={} studentId={} quizId={} score={}", saved.getId(), studentId,
                quizId, score);
        return saved;
    }

    @Transactional(readOnly = true)
    public QuizSubmission getById(Long id) {
        return quizSubmissionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz submission not found"));
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentIdOrderByTakenAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getByQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuizIdOrderByTakenAtDesc(quizId);
    }
}
