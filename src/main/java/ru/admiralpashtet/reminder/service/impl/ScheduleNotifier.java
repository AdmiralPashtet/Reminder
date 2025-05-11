package ru.admiralpashtet.reminder.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.ReminderService;
import ru.admiralpashtet.reminder.service.ScheduleService;

import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleNotifier implements ScheduleService {

    private ReminderService reminderService;
    private EmailNotificationSenderService emailNotificationSenderService;
    private TelegramNotificationSenderService telegramNotificationSenderService;

    @Override
    @Scheduled(cron = "0 * * * * *")
    public void doNotify() {
        List<Reminder> reminders = reminderService.findAllByLocalDateTimeNow();
        if (!reminders.isEmpty()) {
            reminders.forEach(reminder -> {
                emailNotificationSenderService.sendNotification(reminder);
                telegramNotificationSenderService.sendNotification(reminder);
            });
        }
    }
}