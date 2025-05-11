package ru.admiralpashtet.reminder.service;

import ru.admiralpashtet.reminder.entity.Reminder;

public interface NotificationSenderService {
    void sendNotification(Reminder reminder);
}
