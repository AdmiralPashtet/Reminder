package ru.admiralpashtet.reminder.dto.request;

import jakarta.validation.constraints.Email;

public record UserSettingsRequest(
        @Email(message = "The email must be in the correct format.")
        String reminderEmail,
        String telegramUsername,
        String timeZone
) {
}
