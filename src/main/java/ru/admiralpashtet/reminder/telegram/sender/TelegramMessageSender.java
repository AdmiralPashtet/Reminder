package ru.admiralpashtet.reminder.telegram.sender;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.util.Message;

@Slf4j
@Component
public class TelegramMessageSender {

    @Value("${telegram.bot.token}")
    private String botToken;
    private TelegramClient telegramClient;

    @PostConstruct
    private void init() {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    public void reply(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Reminder reminder) {
        Long chatId = reminder.getUser().getTelegramData().getChatId();
        if (chatId == null) {
            log.warn("Telegram chat for user with id {} was not found. Reminder will not be sent in telegram",
                    reminder.getUser().getId());
            return;
        }
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .parseMode(ParseMode.MARKDOWNV2)
                .text(Message.REMINDER_DEFAULT_TEMPLATE.format(reminder.getTitle(), reminder.getDescription()))
                .build();
        try {
            telegramClient.execute(message);
            log.info("Reminder with id {} was sent successfully by telegram", reminder.getId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}