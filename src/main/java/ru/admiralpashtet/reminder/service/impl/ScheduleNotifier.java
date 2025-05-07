package ru.admiralpashtet.reminder.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.ReminderService;
import ru.admiralpashtet.reminder.service.ScheduleService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleNotifier implements ScheduleService {

    private ReminderService reminderService;
    private EmailNotificationSenderService emailNotificationSenderService;
    private TelegramNotificationSenderService telegramNotificationSenderService;

    @Override
    @Scheduled(cron = "0 * * * * *", zone = "UTC")
    public void doNotify() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        OffsetDateTime plusMinute = now.plusMinutes(1);

        List<Reminder> reminders = reminderService.findAllByRemindBetween(now, plusMinute);
        if (!reminders.isEmpty()) {
            reminders.forEach(reminder -> {
                emailNotificationSenderService.sendNotification(reminder);
                telegramNotificationSenderService.sendNotification(reminder);
            });
        }
    }
}
