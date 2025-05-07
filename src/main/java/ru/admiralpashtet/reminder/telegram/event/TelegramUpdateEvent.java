package ru.admiralpashtet.reminder.telegram.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
@AllArgsConstructor
public class TelegramUpdateEvent {
    private final Update update;
}
