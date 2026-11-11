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

import hibernate.learning.platform.domain.entity.User;
import hibernate.learning.platform.domain.enums.UserRole;
import hibernate.learning.platform.service.ProfileService;
import hibernate.learning.platform.service.UserService;
import hibernate.learning.platform.web.dto.request.ProfileRequest;
import hibernate.learning.platform.web.dto.request.UserRequest;
import hibernate.learning.platform.web.dto.response.ProfileResponse;
import hibernate.learning.platform.web.dto.response.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping("/api/users")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll().stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/api/users/by-role/{role}")
    public ResponseEntity<List<UserResponse>> getByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getByRole(role).stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/api/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User user = userService.create(request.name(), request.email(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = userService.update(id, request.name(), request.email(), request.role());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users/{userId}/profile")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ProfileResponse.from(profileService.getByUserId(userId)));
    }

    @PutMapping("/api/users/{userId}/profile")
    public ResponseEntity<ProfileResponse> upsertProfile(@PathVariable Long userId,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity
                .ok(ProfileResponse.from(profileService.upsert(userId, request.bio(), request.avatarUrl())));
    }
}
