package ru.admiralpashtet.reminder.telegram.router;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.telegram.event.TelegramUpdateEvent;
import ru.admiralpashtet.reminder.telegram.handler.BotCommand;

import java.util.List;

@Component
@AllArgsConstructor
public class CommandRouter {
    private List<BotCommand> botCommandsHandlers;

    @EventListener
    public void onTelegramUpdate(TelegramUpdateEvent event) {
        Update update = event.getUpdate();
        for (BotCommand handler : botCommandsHandlers) {
            if (handler.supports(update)) {
                handler.handle(update);
                return;
            }
        }
    }
}