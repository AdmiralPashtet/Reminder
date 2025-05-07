package ru.admiralpashtet.reminder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "Users")
@Data
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
            @AttributeOverride(name = "username", column = @Column(name = "telegram_username")),
            @AttributeOverride(name = "chatId", column = @Column(name = "telegram_chat_id"))})
    private TelegramData telegramData;
    private String timeZone;
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @ToString.Exclude
    private List<Reminder> reminders;

    @PrePersist
    private void setUpDefaultData() {
        reminderEmail = email;
        timeZone = "UTC";
        telegramData = new TelegramData("null", null);
    }
}