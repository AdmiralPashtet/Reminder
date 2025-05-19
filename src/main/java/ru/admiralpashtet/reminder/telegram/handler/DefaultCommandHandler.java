package ru.admiralpashtet.reminder.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.service.UserService;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;
import ru.admiralpashtet.reminder.util.Message;

@Component
@AllArgsConstructor
public class DefaultCommandHandler implements BotCommand {
    private final TelegramMessageSender telegramMessageSender;
    private final UserService userService;

    @Override
    public boolean supports(Update update) {
        return (update.getMessage() != null
                && !update.getMessage().hasText())
                || (update.getMessage() != null
                && update.getMessage().hasText()
                && !update.getMessage().getText().equals("/start"));
    }

    @Override
    public void handle(Update update) {
        String username = update.getMessage().getFrom().getUserName();
        Long chatId = update.getMessage().getChatId();
        User user = null;

        user = getUserByTelegramUsername(user, username, chatId);

        if (user != null) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(Message.TG_BOT_SUPPORTS_ONLY_START_COMMAND.get())
                    .build();
            telegramMessageSender.reply(message);
        }
    }

    private User getUserByTelegramUsername(User user, String username, Long chatId) {
        try {
            user = userService.findByTelegramDataUsername(username);
        } catch (UserNotFoundException ex) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(Message.USER_WITH_CURRENT_TG_NOT_FOUND.format(username))
                    .build();
            telegramMessageSender.reply(message);
        }
        return user;
    }
}
