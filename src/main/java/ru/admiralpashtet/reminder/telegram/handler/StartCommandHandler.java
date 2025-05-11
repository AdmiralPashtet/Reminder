package ru.admiralpashtet.reminder.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.service.UserService;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;

@Component
@AllArgsConstructor
public class StartCommandHandler implements BotCommand {
    private final TelegramMessageSender telegramMessageSender;
    private final UserService userService;

    @Override
    public boolean supports(Update update) {
        return update.getMessage() != null
                && update.getMessage().hasText()
                && update.getMessage().getText().equals("/start");
    }

    @Override
    public void handle(Update update) {
        String userName = update.getMessage().getFrom().getUserName();
        Long chatId = update.getMessage().getChatId();
        User user = null;

        user = getUserByTelegramUsername(user, userName, chatId);

        if (user != null) {
            saveChatIdForUser(user, chatId);
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Registration successful. Now reminders will come to this bot.")
                    .build();
            telegramMessageSender.reply(message);
        }
    }

    private void saveChatIdForUser(User user, Long chatId) {
        if (user.getTelegramData().getChatId() == null) {
            user.getTelegramData().setChatId(chatId);
            userService.update(user, user.getId());
        }
    }

    private User getUserByTelegramUsername(User user, String username, Long chatId) {
        try {
            user = userService.findByTelegramDataUsername(username);
        } catch (UserNotFoundException ex) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(String.format("User with telegram @%s was not found in system.", username))
                    .build();
            telegramMessageSender.reply(message);
        }
        return user;
    }
}
