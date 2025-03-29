package ru.admiralpashtet.reminder.controller;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.service.ReminderService;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderResponse> create(@RequestBody ReminderRequest reminderRequest) {
        ReminderResponse reminderResponse = reminderService.create(reminderRequest);
        return new ResponseEntity<>(reminderResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReminderResponse> update(@RequestBody ReminderRequest reminderRequest, @PathVariable("id") long id) {
        ReminderResponse updated = reminderService.update(reminderRequest, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<ReminderResponse>> getAll(@RequestParam(name = "searchByText", required = false)
                                                         @Size(max = 255,
                                                                 message = "Search query must be less then 255 characters")
                                                         String searchByText,
                                                         @RequestParam(name = "searchByDate", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                         @RequestParam(name = "searchByTime", required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                                                         @RequestParam(name = "sort", defaultValue = "remind") String sortBy,
                                                         @RequestParam(name = "asc", defaultValue = "true") boolean ascending,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<ReminderResponse> reminderPage = reminderService.findAll(searchByText, date, time, sortBy, ascending, page, size);
        return new ResponseEntity<>(reminderPage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long reminderId) {
        reminderService.deleteById(reminderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
