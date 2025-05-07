package ru.admiralpashtet.reminder.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.impl.EmailNotificationSenderService;
import ru.admiralpashtet.reminder.service.impl.ScheduleNotifier;
import ru.admiralpashtet.reminder.service.impl.TelegramNotificationSenderService;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class ScheduleNotifierTest {
    @Mock
    private ReminderService reminderService;
    @Mock
    private EmailNotificationSenderService emailNotificationSenderService;
    @Mock
    private TelegramNotificationSenderService telegramNotificationSenderService;
    @InjectMocks
    private ScheduleNotifier scheduleNotifier;

    @Test
    @DisplayName("Test scheduled notification functionality")
    void givenReminder_whenDoNotifyCalled_thenSendNotification() {
        // given
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        OffsetDateTime plusMinute = now.plusMinutes(1);
        List<Reminder> reminders = List.of(DataUtils.getReminderPersisted());
        BDDMockito.given(reminderService.findAllByRemindBetween(now, plusMinute)).willReturn(reminders);
        // when
        Awaitility.await()
                .atMost(62, TimeUnit.SECONDS)
                .untilAsserted(() -> scheduleNotifier.doNotify());
        // then
        Mockito.verify(emailNotificationSenderService, Mockito.times(1))
                .sendNotification(reminders.getFirst());
        Mockito.verify(telegramNotificationSenderService, Mockito.times(1))
                .sendNotification(reminders.getFirst());
    }
}