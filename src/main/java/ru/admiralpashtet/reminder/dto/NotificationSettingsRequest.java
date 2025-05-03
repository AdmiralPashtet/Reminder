package ru.admiralpashtet.reminder.dto;

import jakarta.validation.constraints.Email;

public record NotificationSettingsRequest(
        @Email(message = "The email must be in the correct format.")
        String reminderEmail,
        String telegramUsername
) {
}
