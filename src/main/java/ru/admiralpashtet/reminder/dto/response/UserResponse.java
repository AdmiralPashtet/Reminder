package ru.admiralpashtet.reminder.dto.response;

public record UserResponse(
        String reminderEmail,
        String telegramUsername
) {
}