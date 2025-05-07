package ru.admiralpashtet.reminder.service;

import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.dto.response.UserResponse;
import ru.admiralpashtet.reminder.entity.User;

public interface UserService {
    User createOrGetByEmail(String email);

    User findByTelegramDataUsername(String telegramUsername);

    UserResponse updateNotificationSettings(UserSettingsRequest settingsRequest, Long userId);

    UserResponse update(User user, Long userId);

    UserResponse findById(Long userId);

    void deleteById(Long userId);
}
