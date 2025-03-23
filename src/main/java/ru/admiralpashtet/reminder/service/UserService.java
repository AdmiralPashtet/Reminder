package ru.admiralpashtet.reminder.service;

import ru.admiralpashtet.reminder.entity.User;

public interface UserService {
    User findById(long id);
}
