package ru.admiralpashtet.reminder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "chatId", column = @Column(name = "telegram_chat_id")),
            @AttributeOverride(name = "userName", column = @Column(name = "telegram_username"))})
    private TelegramData telegramData;

}