package ru.admiralpashtet.reminder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record ReminderRequest(
        @Size(max = 255, message = "The title size should not exceed 255 characters")
        String title,
        @Size(max = 4096, message = "The description size should not exceed 4096 characters")
        String description,
        @NotNull(message = "Date and time cannot be null. ISO format expected")
        @Schema(
                description = "ISO‑8601 format",
                example = "2025-05-06T18:41:00+03:00"
        )
        OffsetDateTime remind) {
}