package ru.admiralpashtet.reminder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReminderRequest(
        @Size(max = 255, message = "The title size should not exceed 255 characters")
        String title,
        @Size(max = 4096, message = "The description size should not exceed 4096 characters")
        String description,
        @NotNull(message = "Date and time cannot be null. ISO format expected")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        // TODO: check with different timezone
        LocalDateTime remind) {
}