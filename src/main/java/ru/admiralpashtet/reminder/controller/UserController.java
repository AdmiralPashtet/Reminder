package ru.admiralpashtet.reminder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.dto.response.UserResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.service.UserService;

@Tag(name = "User controller", description = "Controller for working with users")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get user info")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UserResponse userResponse = userService.findById(principal.getId());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PatchMapping
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponse> update(@RequestBody @Valid UserSettingsRequest userSettingsRequest,
                                               @AuthenticationPrincipal CustomUserPrincipal principal) {
        UserResponse userResponse = userService.updateNotificationSettings(userSettingsRequest, principal.getId());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @DeleteMapping
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.deleteById(principal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}