package hibernate.learning.platform.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.CourseReview;
import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.CourseRepository;
import hibernate.learning.platform.repository.CourseReviewRepository;
import hibernate.learning.platform.repository.EnrollmentRepository;
import hibernate.learning.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseReviewService.class);

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public CourseReview create(Long studentId, Long courseId, int rating, String comment) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessRuleViolationException("Only STUDENT can review course");
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleViolationException("Student is not enrolled to course");
        }

        if (courseReviewRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("Course review already exists for student and course");
        }

        CourseReview review = CourseReview.create(course, student, rating, comment, LocalDateTime.now(clock));
        CourseReview saved = courseReviewRepository.save(review);

        LOGGER.info("event=course_review_created id={} studentId={} courseId={}", saved.getId(), studentId, courseId);
        return saved;
    }

    @Transactional(readOnly = true)
    public CourseReview getById(Long id) {
        return courseReviewRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course review not found"));
    }

    @Transactional(readOnly = true)
    public List<CourseReview> getByCourse(Long courseId) {
        return courseReviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseReview> getByStudent(Long studentId) {
        return courseReviewRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public CourseReview update(Long reviewId, Long studentId, int rating, String comment) {
        CourseReview review = getById(reviewId);

        if (!review.getStudent().getId().equals(studentId)) {
            throw new BusinessRuleViolationException("Course review belongs to another student");
        }

        review.updateRating(rating);
        review.updateComment(comment);
        CourseReview saved = courseReviewRepository.save(review);

        LOGGER.info("event=course_review_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long reviewId, Long studentId) {
        CourseReview review = getById(reviewId);

        if (!review.getStudent().getId().equals(studentId)) {
            throw new BusinessRuleViolationException("Course review belongs to another student");
        }

        courseReviewRepository.delete(review);
        LOGGER.info("event=course_review_deleted id={}", reviewId);
    }
}
