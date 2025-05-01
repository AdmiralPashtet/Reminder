package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.service.NotificationSenderService;

@Service
@RequiredArgsConstructor
public class EmailNotificationSenderService implements NotificationSenderService {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender mailSender;

    @Override
    public void sendNotification(Reminder reminder) {
        SimpleMailMessage message = createMessage(reminder);
        mailSender.send(message);
    }

    private SimpleMailMessage createMessage(Reminder reminder) {
        String email = reminder.getUser().getNotificationEmail();
        if (email == null) {
            throw new RuntimeException("Email for notification not set");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reminder Project: " + reminder.getTitle());
        message.setText(reminder.getDescription());
        return message;
    }
}
