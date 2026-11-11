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

import hibernate.learning.platform.domain.entity.Lesson;
import hibernate.learning.platform.domain.entity.Module;
import hibernate.learning.platform.service.ModuleService;
import hibernate.learning.platform.web.dto.request.LessonRequest;
import hibernate.learning.platform.web.dto.request.ModuleRequest;
import hibernate.learning.platform.web.dto.response.LessonSummaryResponse;
import hibernate.learning.platform.web.dto.response.ModuleResponse;
import hibernate.learning.platform.web.dto.response.ModuleSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;

    @GetMapping("/api/modules/{id}")
    public ResponseEntity<ModuleResponse> getById(@PathVariable Long id) {
        Module module = moduleService.getWithLessons(id);
        return ResponseEntity.ok(ModuleResponse.from(module));
    }

    @GetMapping("/api/modules/{id}/lessons")
    public ResponseEntity<List<LessonSummaryResponse>> getLessons(@PathVariable Long id) {
        List<LessonSummaryResponse> lessons = moduleService.getWithLessons(id).getLessons().stream()
                .map(LessonSummaryResponse::from).toList();
        return ResponseEntity.ok(lessons);
    }

    @PostMapping("/api/modules/{moduleId}/lessons")
    public ResponseEntity<LessonSummaryResponse> addLesson(@PathVariable Long moduleId,
            @Valid @RequestBody LessonRequest request) {
        Lesson lesson = moduleService.addLesson(moduleId, request.title(), request.content(), request.videoUrl(),
                request.orderIndex());
        return ResponseEntity.status(HttpStatus.CREATED).body(LessonSummaryResponse.from(lesson));
    }

    @PutMapping("/api/modules/{id}")
    public ResponseEntity<ModuleSummaryResponse> update(@PathVariable Long id,
            @Valid @RequestBody ModuleRequest request) {
        Module module = moduleService.update(id, request.title(), request.description(), request.orderIndex());
        return ResponseEntity.ok(ModuleSummaryResponse.from(module));
    }

    @DeleteMapping("/api/modules/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
