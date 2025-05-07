package ru.admiralpashtet.reminder.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(example = "2025-05-06T16:41")
        LocalDateTime remind) {
}