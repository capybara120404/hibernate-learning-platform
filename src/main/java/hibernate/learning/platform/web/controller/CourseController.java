package hibernate.learning.platform.web.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hibernate.learning.platform.domain.entity.Course;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.service.CourseService;
import hibernate.learning.platform.web.dto.request.CourseCreateRequest;
import hibernate.learning.platform.web.dto.request.CourseUpdateRequest;
import hibernate.learning.platform.web.dto.request.ModuleRequest;
import hibernate.learning.platform.web.dto.response.CourseDetailResponse;
import hibernate.learning.platform.web.dto.response.CourseResponse;
import hibernate.learning.platform.web.dto.response.ModuleSummaryResponse;
import hibernate.learning.platform.web.dto.response.TagResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/api/courses")
    public ResponseEntity<List<CourseResponse>> getAll() {
        List<CourseResponse> courses = courseService.getAll().stream().map(CourseResponse::from).toList();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/api/courses/by-teacher/{teacherId}")
    public ResponseEntity<List<CourseResponse>> getByTeacher(@PathVariable Long teacherId) {
        List<CourseResponse> courses = courseService.getByTeacher(teacherId).stream().map(CourseResponse::from)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/api/courses/by-category/{categoryId}")
    public ResponseEntity<List<CourseResponse>> getByCategory(@PathVariable Long categoryId) {
        List<CourseResponse> courses = courseService.getByCategory(categoryId).stream().map(CourseResponse::from)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/api/courses/{id}")
    public ResponseEntity<CourseDetailResponse> getById(@PathVariable Long id) {
        Course course = courseService.getStructure(id);
        return ResponseEntity.ok(CourseDetailResponse.from(course));
    }

    @GetMapping("/api/courses/{id}/modules")
    public ResponseEntity<List<ModuleSummaryResponse>> getModules(@PathVariable Long id) {
        List<ModuleSummaryResponse> modules = courseService.getWithModules(id).getModules().stream()
                .map(ModuleSummaryResponse::from).toList();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/api/courses/{id}/tags")
    public ResponseEntity<List<TagResponse>> getTags(@PathVariable Long id) {
        List<TagResponse> tags = courseService.getWithTags(id).getTags().stream().map(TagResponse::from).toList();
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/api/courses")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
        Course course = courseService.create(request.teacherId(), request.categoryId(), request.title(),
                request.description(), request.startDate(), request.durationWeeks());
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse.from(course));
    }

    @PutMapping("/api/courses/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request) {
        Course course = courseService.update(id, request.title(), request.description(), request.startDate(),
                request.durationWeeks());
        return ResponseEntity.ok(CourseResponse.from(course));
    }

    @PutMapping("/api/courses/{courseId}/teacher/{teacherId}")
    public ResponseEntity<CourseResponse> changeTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        Course course = courseService.changeTeacher(courseId, teacherId);
        return ResponseEntity.ok(CourseResponse.from(course));
    }

    @PutMapping("/api/courses/{courseId}/category/{categoryId}")
    public ResponseEntity<CourseResponse> changeCategory(@PathVariable Long courseId, @PathVariable Long categoryId) {
        Course course = courseService.changeCategory(courseId, categoryId);
        return ResponseEntity.ok(CourseResponse.from(course));
    }

    @PostMapping("/api/courses/{courseId}/modules")
    public ResponseEntity<ModuleSummaryResponse> addModule(@PathVariable Long courseId,
            @Valid @RequestBody ModuleRequest request) {
        Module module = courseService.addModule(courseId, request.title(), request.orderIndex(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(ModuleSummaryResponse.from(module));
    }

    @PostMapping("/api/courses/{courseId}/tags/{tagId}")
    public ResponseEntity<CourseResponse> addTag(@PathVariable Long courseId, @PathVariable Long tagId) {
        Course course = courseService.addTag(courseId, tagId);
        return ResponseEntity.ok(CourseResponse.from(course));
    }

    @DeleteMapping("/api/courses/{courseId}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(@PathVariable Long courseId, @PathVariable Long tagId) {
        courseService.removeTag(courseId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
