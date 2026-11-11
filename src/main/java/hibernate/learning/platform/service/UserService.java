package hibernate.learning.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;

    public User create(String name, String email, UserRole role) {
        if (role == null) {
            throw new BusinessRuleViolationException("User role must not be null");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email already exists");
        }
        User user = switch (role) {
            case STUDENT -> User.createStudent(name, email);
            case TEACHER -> User.createTeacher(name, email);
            case ADMIN -> User.createAdmin(name, email);
        };
        User saved = userRepository.save(user);
        LOGGER.info("event=user_created id={} role={}", saved.getId(), saved.getRole());
        return saved;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getByRole(UserRole role) {
        return userRepository.findByRoleOrderByNameAsc(role);
    }

    public User update(Long id, String name, String email, UserRole role) {
        if (role == null) {
            throw new BusinessRuleViolationException("User role must not be null");
        }
        User user = getById(id);
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email already exists");
        }
        if (user.getRole() != role) {
            requireNoLearningDependencies(id);
        }
        user.updateName(name);
        user.updateEmail(email);
        user.updateRole(role);
        User saved = userRepository.save(user);
        LOGGER.info("event=user_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        User user = getById(id);
        requireNoLearningDependencies(id);
        userRepository.delete(user);
        LOGGER.info("event=user_deleted id={}", id);
    }

    private void requireNoLearningDependencies(Long userId) {
        if (courseRepository.existsByTeacherId(userId) || enrollmentRepository.existsByStudentId(userId)
                || submissionRepository.existsByStudentId(userId) || quizSubmissionRepository.existsByStudentId(userId)
                || courseReviewRepository.existsByStudentId(userId)) {
            throw new BusinessRuleViolationException("User has dependent learning data");
        }
    }
}
