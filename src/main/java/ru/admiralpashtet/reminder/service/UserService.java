package ru.admiralpashtet.reminder.service;

import ru.admiralpashtet.reminder.dto.NotificationSettingsRequest;
import ru.admiralpashtet.reminder.dto.UserResponse;
import ru.admiralpashtet.reminder.entity.User;

public interface UserService {
    User createOrGetByEmail(String email);

    User findByTelegramDataUserName(String telegramUserName);

    UserResponse updateNotificationSettings(NotificationSettingsRequest settingsRequest, Long userId);

    UserResponse update(User user, Long userId);

    void deleteById(Long userId);
}
