package ru.admiralpashtet.reminder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    @Email(message = "The email must be in the correct format.")
    private String reminderEmail;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "userName", column = @Column(name = "telegram_username")),
            @AttributeOverride(name = "chatId", column = @Column(name = "telegram_chat_id"))})
    private TelegramData telegramData;
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Reminder> reminders;

    @PrePersist
    private void setUpDefaultNotificationEmail() {
        reminderEmail = email;
    }
}