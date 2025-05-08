package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.NotificationSenderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationSenderService implements NotificationSenderService {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender mailSender;

    @Override
    public void sendNotification(Reminder reminder) {
        SimpleMailMessage message = createMessage(reminder);
        if (message != null) {
            mailSender.send(message);
            log.info("Reminder with id {} was sent successfully by email", reminder.getId());
        }
    }

    private SimpleMailMessage createMessage(Reminder reminder) {
        String email = reminder.getUser().getReminderEmail();
        if (email == null) {
            log.warn("Reminder email is not set for user with id {}. The letter will not be sent.",
                    reminder.getUser().getId());
            return null;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reminder: " + reminder.getTitle());
        message.setText(reminder.getDescription());
        return message;
    }
}