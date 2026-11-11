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

import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.service.TagService;
import hibernate.learning.platform.web.dto.request.TagRequest;
import hibernate.learning.platform.web.dto.response.TagResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping("/api/tags")
    public ResponseEntity<List<TagResponse>> getAll() {
        List<TagResponse> tags = tagService.getAll().stream().map(TagResponse::from).toList();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/api/tags/{id}")
    public ResponseEntity<TagResponse> getById(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        return ResponseEntity.ok(TagResponse.from(tag));
    }

    @PostMapping("/api/tags")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        Tag tag = tagService.create(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(TagResponse.from(tag));
    }

    @PutMapping("/api/tags/{id}")
    public ResponseEntity<TagResponse> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        Tag tag = tagService.update(id, request.name());
        return ResponseEntity.ok(TagResponse.from(tag));
    }

    @DeleteMapping("/api/tags/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
