package ru.admiralpashtet.reminder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.admiralpashtet.reminder.dto.NotificationSettingsRequest;
import ru.admiralpashtet.reminder.dto.UserResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PatchMapping
    public ResponseEntity<UserResponse> update(@RequestBody @Valid NotificationSettingsRequest notificationSettingsRequest,
                                               @AuthenticationPrincipal CustomUserPrincipal principal) {
        UserResponse userResponse = userService.updateNotificationSettings(notificationSettingsRequest, principal.getId());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.deleteById(principal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
