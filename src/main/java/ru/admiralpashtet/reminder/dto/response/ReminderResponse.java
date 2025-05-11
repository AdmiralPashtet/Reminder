package ru.admiralpashtet.reminder.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ReminderResponse(
        Long id,
        String title,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime remind,
        Integer userId) {
}
