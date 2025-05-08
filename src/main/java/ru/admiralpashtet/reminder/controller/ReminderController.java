package ru.admiralpashtet.reminder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.service.ReminderService;

import java.time.LocalDate;
import java.time.LocalTime;

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
    public ResponseEntity<Page<ReminderResponse>> getAll(@Valid @RequestParam(name = "searchByText", required = false)
                                                         @Size(max = 255,
                                                                 message = "Search query must be less then 255 characters")
                                                         @Parameter(description = "Search query. " +
                                                                 "Several words must be connected by a plus. " +
                                                                 "The search in performed on all words in sentence.",
                                                                 example = "searchByText=sanya+birthday")
                                                         String searchByText,
                                                         @RequestParam(name = "searchByDate", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         @Parameter(description = "ISO format is used.",
                                                                 example = "searchByDate=2025-05-06")
                                                         LocalDate date,
                                                         @RequestParam(name = "searchByTime", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                                         @Parameter(description = "ISO format is used.",
                                                                 example = "searchByTime=15:50")
                                                         LocalTime time,
                                                         @RequestParam(name = "sort", defaultValue = "remind")
                                                         @Parameter(description = "Sort condition parameter.",
                                                                 schema = @Schema(allowableValues =
                                                                         {"title", "description", "remind"}))
                                                         String sortBy,
                                                         @RequestParam(name = "asc", defaultValue = "true")
                                                         @Parameter(description = "Sorting direction")
                                                         boolean ascending,
                                                         @RequestParam(name = "page", defaultValue = "0")
                                                         @Parameter(description = "Current page number")
                                                         int page,
                                                         @RequestParam(name = "size", defaultValue = "10")
                                                         @Parameter(description = "Number of elements on one page")
                                                         int size,
                                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        Page<ReminderResponse> reminderPage = reminderService.findAll(principal.getId(),
                searchByText, date, time, sortBy, ascending, page, size);
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
