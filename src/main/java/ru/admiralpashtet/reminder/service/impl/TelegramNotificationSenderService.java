package ru.admiralpashtet.reminder.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.NotificationSenderService;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;

@Service
@AllArgsConstructor
public class TelegramNotificationSenderService implements NotificationSenderService {

    private final TelegramMessageSender telegramMessageSender;

    @Override
    public void sendNotification(Reminder reminder) {
        telegramMessageSender.sendMessage(reminder);
    }
}