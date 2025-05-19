package ru.admiralpashtet.reminder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.request.SearchRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.service.ReminderService;

@Tag(name = "Reminder controller", description = "Controller for working with reminders")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping
    @Operation(summary = "Create a new reminder")
    public ResponseEntity<ReminderResponse> create(@RequestBody @Valid ReminderRequest reminderRequest,
                                                   @AuthenticationPrincipal CustomUserPrincipal principal) {
        ReminderResponse reminderResponse = reminderService.create(reminderRequest, principal.getId());
        return new ResponseEntity<>(reminderResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update exists reminder")
    public ResponseEntity<ReminderResponse> update(@RequestBody @Valid ReminderRequest reminderRequest,
                                                   @PathVariable("id") long reminderId,
                                                   @AuthenticationPrincipal CustomUserPrincipal principal) {
        ReminderResponse updated = reminderService.update(reminderRequest, reminderId, principal.getId());
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get all reminders by search filters")
    public ResponseEntity<Page<ReminderResponse>> getAll(@RequestBody @Valid SearchRequest searchRequest,
                                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        Page<ReminderResponse> reminderPage = reminderService.findAll(principal.getId(), searchRequest);
        return new ResponseEntity<>(reminderPage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reminder by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long reminderId,
                                       @AuthenticationPrincipal CustomUserPrincipal principal) {
        reminderService.deleteById(reminderId, principal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}