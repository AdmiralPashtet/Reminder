package ru.admiralpashtet.reminder.dto.response;

import java.time.OffsetDateTime;

public record ReminderResponse(
        Long id,
        String title,
        String description,
        OffsetDateTime remind,
        Integer userId) {
}
