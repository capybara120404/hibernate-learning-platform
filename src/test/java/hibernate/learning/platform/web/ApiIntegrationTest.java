package hibernate.learning.platform.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import hibernate.learning.platform.domain.enums.QuestionType;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.testsupport.AbstractIntegrationTest;
import hibernate.learning.platform.web.dto.request.AnswerOptionRequest;
import hibernate.learning.platform.web.dto.request.AssignmentRequest;
import hibernate.learning.platform.web.dto.request.CategoryRequest;
import hibernate.learning.platform.web.dto.request.CourseCreateRequest;
import hibernate.learning.platform.web.dto.request.CourseReviewRequest;
import hibernate.learning.platform.web.dto.request.CourseReviewUpdateRequest;
import hibernate.learning.platform.web.dto.request.CourseUpdateRequest;
import hibernate.learning.platform.web.dto.request.GradeSubmissionRequest;
import hibernate.learning.platform.web.dto.request.LessonRequest;
import hibernate.learning.platform.web.dto.request.ModuleRequest;
import hibernate.learning.platform.web.dto.request.ProfileRequest;
import hibernate.learning.platform.web.dto.request.QuestionRequest;
import hibernate.learning.platform.web.dto.request.QuizRequest;
import hibernate.learning.platform.web.dto.request.SubmissionRequest;
import hibernate.learning.platform.web.dto.request.TagRequest;
import hibernate.learning.platform.web.dto.request.TakeQuizRequest;
import hibernate.learning.platform.web.dto.request.UserRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class ApiIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL_DOMAIN = "@example.com";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Test
    void unknownResourceReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/unknown-endpoint")).andExpect(status().isNotFound());
    }

    @Test
    void missingDurationWeeksReturnsBadRequest() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        String invalidJson = String.format("""
                {
                  "teacherId": %d,
                  "categoryId": %d,
                  "title": "Invalid Course",
                  "description": "Missing duration",
                  "startDate": "2026-12-01"
                }
                """, teacherId, categoryId);

        mockMvc.perform(post("/api/courses").contentType(APPLICATION_JSON).content(invalidJson))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations[?(@.field == 'durationWeeks')]").exists());
    }

    @Test
    void userValidationFailureReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content(json(new UserRequest("", "invalid-email", UserRole.STUDENT))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void userProfileCrudThroughApi() throws Exception {
        String suffix = newSuffix();
        Long userId = createUser("User " + suffix, email("user", suffix), UserRole.STUDENT);

        mockMvc.perform(put("/api/users/" + userId + "/profile").contentType(APPLICATION_JSON)
                .content(json(new ProfileRequest("Bio " + suffix, "avatar-" + suffix)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Bio " + suffix));

        mockMvc.perform(get("/api/users/" + userId + "/profile")).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void categoryDuplicateReturnsConflict() throws Exception {
        String name = "Category " + newSuffix();
        createCategory(name);

        mockMvc.perform(post("/api/categories").contentType(APPLICATION_JSON).content(json(new CategoryRequest(name))))
                .andExpect(status().isConflict());
    }

    @Test
    void courseDetailIncludesModulesAndTags() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        Long tagId = createTag("Tag " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        addModule(courseId, "Module " + suffix);

        mockMvc.perform(post("/api/courses/" + courseId + "/tags/" + tagId)).andExpect(status().isOk());

        mockMvc.perform(get("/api/courses/" + courseId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.modules", hasSize(1)))
                .andExpect(jsonPath("$.modules[0].title").value("Module " + suffix))
                .andExpect(jsonPath("$.tags", hasSize(1))).andExpect(jsonPath("$.tags[0].name").value("Tag " + suffix));
    }

    @Test
    void enrollmentDuplicateAndWrongRoleReturnErrors() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long studentId = createUser("Student " + suffix, email("student", suffix), UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        enroll(courseId, studentId);

        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments/" + studentId))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments/" + teacherId))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/courses/" + courseId + "/enrollments")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/users/" + studentId + "/enrollments")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void submissionAndGradeFlowThroughApi() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long studentId = createUser("Student " + suffix, email("student", suffix), UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        Long lessonId = addLesson(moduleId, "Lesson " + suffix);
        Long assignmentId = addAssignment(lessonId, "Assignment " + suffix);
        enroll(courseId, studentId);
        Long submissionId = submit(assignmentId, studentId, "Answer " + suffix);

        mockMvc.perform(post("/api/assignments/" + assignmentId + "/submissions").contentType(APPLICATION_JSON)
                .content(json(new SubmissionRequest(studentId, "Duplicate")))).andExpect(status().isConflict());

        mockMvc.perform(put("/api/submissions/" + submissionId + "/grade").contentType(APPLICATION_JSON)
                .content(json(new GradeSubmissionRequest(teacherId, 90, "Feedback " + suffix))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.score").value(90))
                .andExpect(jsonPath("$.status").value("GRADED"));

        mockMvc.perform(get("/api/assignments/" + assignmentId + "/submissions")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].score").value(90));
        mockMvc.perform(get("/api/users/" + studentId + "/submissions")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void quizTakeQuizFlowThroughApi() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long studentId = createUser("Student " + suffix, email("student", suffix), UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        enroll(courseId, studentId);
        Long quizId = createQuiz(moduleId, "Quiz " + suffix);

        Long questionOneId = addQuestion(quizId, "Q1 " + suffix, QuestionType.SINGLE_CHOICE);
        Long questionOneCorrectId = addAnswerOption(questionOneId, "A " + suffix, true);
        addAnswerOption(questionOneId, "B " + suffix, false);

        Long questionTwoId = addQuestion(quizId, "Q2 " + suffix, QuestionType.MULTIPLE_CHOICE);
        Long questionTwoFirstCorrectId = addAnswerOption(questionTwoId, "C " + suffix, true);
        Long questionTwoSecondCorrectId = addAnswerOption(questionTwoId, "D " + suffix, true);
        addAnswerOption(questionTwoId, "E " + suffix, false);

        Map<Long, Set<Long>> answers = Map.of(questionOneId, Set.of(questionOneCorrectId), questionTwoId,
                Set.of(questionTwoFirstCorrectId, questionTwoSecondCorrectId));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submissions").contentType(APPLICATION_JSON)
                .content(json(new TakeQuizRequest(studentId, answers)))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(100)).andExpect(jsonPath("$.passed").value(true));

        mockMvc.perform(get("/api/quizzes/" + quizId + "/submissions")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void courseReviewFlowThroughApi() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long studentId = createUser("Student " + suffix, email("student", suffix), UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        enroll(courseId, studentId);
        Long reviewId = createReview(courseId, studentId, 5, "Great " + suffix);

        mockMvc.perform(post("/api/courses/" + courseId + "/reviews").contentType(APPLICATION_JSON)
                .content(json(new CourseReviewRequest(studentId, 5, "Duplicate")))).andExpect(status().isConflict());

        mockMvc.perform(get("/api/courses/" + courseId + "/reviews")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(put("/api/users/" + studentId + "/reviews/" + reviewId).contentType(APPLICATION_JSON)
                .content(json(new CourseReviewUpdateRequest(4, "Updated " + suffix)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        mockMvc.perform(delete("/api/users/" + studentId + "/reviews/" + reviewId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/courses/" + courseId + "/reviews")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void missingResourceReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/999999999")).andExpect(status().isNotFound());
    }

    @Test
    void courseDeletionBlockedByEnrollmentReturnsUnprocessable() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long studentId = createUser("Student " + suffix, email("student", suffix), UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        enroll(courseId, studentId);

        mockMvc.perform(delete("/api/courses/" + courseId)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void contentUpdateEndpointsReturnUpdatedPayload() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        Long lessonId = addLesson(moduleId, "Lesson " + suffix);
        Long assignmentId = addAssignment(lessonId, "Assignment " + suffix);

        mockMvc.perform(put("/api/courses/" + courseId).contentType(APPLICATION_JSON)
                .content(json(new CourseUpdateRequest("Renamed Course " + suffix, "Renamed description " + suffix,
                        LocalDate.now().plusDays(5), 8))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Renamed Course " + suffix))
                .andExpect(jsonPath("$.durationWeeks").value(8));

        mockMvc.perform(put("/api/modules/" + moduleId).contentType(APPLICATION_JSON)
                .content(json(new ModuleRequest("Renamed Module " + suffix, 2, "Renamed module description"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.orderIndex").value(2));

        mockMvc.perform(put("/api/lessons/" + lessonId).contentType(APPLICATION_JSON)
                .content(json(new LessonRequest("Renamed Lesson " + suffix, "Renamed content", "video-new", 2))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Renamed Lesson " + suffix));

        mockMvc.perform(put("/api/assignments/" + assignmentId).contentType(APPLICATION_JSON)
                .content(json(new AssignmentRequest("Renamed Assignment " + suffix, "Renamed assignment description",
                        LocalDate.now().plusDays(9), 50))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.maxScore").value(50));
    }

    @Test
    void quizContentUpdateAndDeleteEndpointsWork() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        Long quizId = createQuiz(moduleId, "Quiz " + suffix);
        Long questionId = addQuestion(quizId, "Q " + suffix, QuestionType.SINGLE_CHOICE);
        Long correctOptionId = addAnswerOption(questionId, "Correct " + suffix, true);
        Long incorrectOptionId = addAnswerOption(questionId, "Incorrect " + suffix, false);

        mockMvc.perform(put("/api/quizzes/" + quizId).contentType(APPLICATION_JSON)
                .content(json(new QuizRequest("Renamed Quiz " + suffix, 45, 75)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.passingScore").value(75));

        mockMvc.perform(put("/api/questions/" + questionId).contentType(APPLICATION_JSON)
                .content(json(new QuestionRequest("Renamed Q " + suffix, QuestionType.SINGLE_CHOICE, 3))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.points").value(3));

        mockMvc.perform(put("/api/answer-options/" + incorrectOptionId).contentType(APPLICATION_JSON)
                .content(json(new AnswerOptionRequest("Second correct " + suffix, true))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(put("/api/answer-options/" + correctOptionId).contentType(APPLICATION_JSON)
                .content(json(new AnswerOptionRequest("Updated correct " + suffix, true)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated correct " + suffix));

        mockMvc.perform(delete("/api/answer-options/" + incorrectOptionId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/questions/" + questionId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/quizzes/" + quizId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/quizzes/" + quizId)).andExpect(status().isNotFound());
    }

    @Test
    void secondCorrectOptionForSingleChoiceReturnsUnprocessable() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        Long quizId = createQuiz(moduleId, "Quiz " + suffix);
        Long questionId = addQuestion(quizId, "Q " + suffix, QuestionType.SINGLE_CHOICE);
        addAnswerOption(questionId, "Correct " + suffix, true);

        mockMvc.perform(post("/api/questions/" + questionId + "/answer-options").contentType(APPLICATION_JSON)
                .content(json(new AnswerOptionRequest("Another correct " + suffix, true))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/questions/" + questionId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.answerOptions", hasSize(1)));
    }

    @Test
    void roleChangeWithEnrollmentReturnsUnprocessable() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        String studentEmail = email("student", suffix);
        Long studentId = createUser("Student " + suffix, studentEmail, UserRole.STUDENT);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        enroll(courseId, studentId);

        mockMvc.perform(put("/api/users/" + studentId).contentType(APPLICATION_JSON)
                .content(json(new UserRequest("Student " + suffix, studentEmail, UserRole.TEACHER))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void courseWithoutLearningDataIsDeletedWithContent() throws Exception {
        String suffix = newSuffix();
        Long teacherId = createUser("Teacher " + suffix, email("teacher", suffix), UserRole.TEACHER);
        Long categoryId = createCategory("Category " + suffix);
        Long courseId = createCourse(teacherId, categoryId, "Course " + suffix);
        Long moduleId = addModule(courseId, "Module " + suffix);
        Long lessonId = addLesson(moduleId, "Lesson " + suffix);
        Long assignmentId = addAssignment(lessonId, "Assignment " + suffix);

        mockMvc.perform(delete("/api/courses/" + courseId)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/courses/" + courseId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/modules/" + moduleId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/lessons/" + lessonId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/assignments/" + assignmentId)).andExpect(status().isNotFound());
    }

    private Long createUser(String name, String email, UserRole role) throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/users").contentType(APPLICATION_JSON).content(json(new UserRequest(name, email, role))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long createCategory(String name) throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/categories").contentType(APPLICATION_JSON).content(json(new CategoryRequest(name))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long createTag(String name) throws Exception {
        ResultActions result = mockMvc
                .perform(post("/api/tags").contentType(APPLICATION_JSON).content(json(new TagRequest(name))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long createCourse(Long teacherId, Long categoryId, String title) throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/courses").contentType(APPLICATION_JSON).content(json(new CourseCreateRequest(teacherId,
                        categoryId, title, "Description " + title, LocalDate.now().plusDays(1), 4))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long addModule(Long courseId, String title) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/courses/" + courseId + "/modules")
                .contentType(APPLICATION_JSON).content(json(new ModuleRequest(title, 1, "Module description"))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long addLesson(Long moduleId, String title) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/modules/" + moduleId + "/lessons")
                .contentType(APPLICATION_JSON).content(json(new LessonRequest(title, "Lesson content", "video", 1))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long addAssignment(Long lessonId, String title) throws Exception {
        ResultActions result = mockMvc
                .perform(post("/api/lessons/" + lessonId + "/assignments").contentType(APPLICATION_JSON).content(json(
                        new AssignmentRequest(title, "Assignment description", LocalDate.now().plusDays(2), 100))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private void enroll(Long courseId, Long studentId) throws Exception {
        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments/" + studentId)).andExpect(status().isCreated());
    }

    private Long submit(Long assignmentId, Long studentId, String content) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/assignments/" + assignmentId + "/submissions")
                .contentType(APPLICATION_JSON).content(json(new SubmissionRequest(studentId, content))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long createQuiz(Long moduleId, String title) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/modules/" + moduleId + "/quizzes")
                .contentType(APPLICATION_JSON).content(json(new QuizRequest(title, 30, 60))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long addQuestion(Long quizId, String text, QuestionType type) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                .contentType(APPLICATION_JSON).content(json(new QuestionRequest(text, type, 1))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long addAnswerOption(Long questionId, String text, boolean correct) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/questions/" + questionId + "/answer-options")
                .contentType(APPLICATION_JSON).content(json(new AnswerOptionRequest(text, correct))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long createReview(Long courseId, Long studentId, int rating, String comment) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/courses/" + courseId + "/reviews")
                .contentType(APPLICATION_JSON).content(json(new CourseReviewRequest(studentId, rating, comment))));
        result.andExpect(status().isCreated());
        return idFrom(result.andReturn().getResponse());
    }

    private Long idFrom(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8)).path("id").asLong();
    }

    private String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private String newSuffix() {
        return UUID.randomUUID().toString();
    }

    private String email(String prefix, String suffix) {
        return prefix + "-" + suffix + EMAIL_DOMAIN;
    }
}
