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

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.service.CategoryService;
import hibernate.learning.platform.web.dto.request.CategoryRequest;
import hibernate.learning.platform.web.dto.response.CategoryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<CategoryResponse> categories = categoryService.getAll().stream().map(CategoryResponse::from).toList();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    @PostMapping("/api/categories")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.create(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(category));
    }

    @PutMapping("/api/categories/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.update(id, request.name());
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
