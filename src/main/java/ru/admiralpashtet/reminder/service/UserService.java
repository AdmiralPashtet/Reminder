package ru.admiralpashtet.reminder.service;

import ru.admiralpashtet.reminder.entity.User;

public interface UserService {
    User createOrGetByEmail(String email);

    User findById(long id);

    User findByTelegramDataUserName(String telegramUserName);

    User update(User user, Long id);
}
