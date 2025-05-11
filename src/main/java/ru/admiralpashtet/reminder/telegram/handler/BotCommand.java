package ru.admiralpashtet.reminder.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotCommand {
    boolean supports(Update update);
    void handle(Update update);
}
