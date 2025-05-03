package ru.admiralpashtet.reminder.telegram;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.service.UserService;

@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${telegram.bot.token}")
    private String botToken;
    private TelegramClient telegramClient;
    private final UserService userService;

    @PostConstruct
    private void init() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    public TelegramBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String userName = update.getMessage().getFrom().getUserName();
            Long chatId = update.getMessage().getChatId();

            if (update.getMessage().hasText() && update.getMessage().getText().equals("/start")) {
                User user = null;
                try {
                    user = userService.findByTelegramDataUserName(userName);
                } catch (UserNotFoundException ex) {
                    try {
                        SendMessage message = SendMessage.builder()
                                .chatId(chatId)
                                .text(String.format("User with telegram @%s was not found in system.", userName))
                                .build();
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (user != null && user.getTelegramData().getChatId() == null) {
                    user.getTelegramData().setChatId(chatId);
                    userService.update(user, user.getId());
                    try {
                        SendMessage message = SendMessage.builder()
                                .chatId(chatId)
                                .text("Registration successful. Now reminders will come to this bot.")
                                .build();
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                try {
                    SendMessage message = SendMessage.builder()
                            .chatId(chatId)
                            .text("The bot only supports the /start command.")
                            .build();
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendMessage(Reminder reminder) {
        Long chatId = reminder.getUser().getTelegramData().getChatId();
        if (chatId == null) {
            throw new RuntimeException("Telegram chat for user "
                    + reminder.getUser().getTelegramData().getUsername() + " was not found");
        }
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .parseMode(ParseMode.MARKDOWNV2)
                .text(String.format("*%s*\n%s", reminder.getTitle(), reminder.getDescription()))
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}