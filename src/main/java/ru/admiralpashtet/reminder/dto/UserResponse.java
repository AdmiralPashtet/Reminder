package ru.admiralpashtet.reminder.dto;

public record UserResponse(
        String reminderEmail,
        String telegramUsername
) {
}