package ru.admiralpashtet.reminder.telegram.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.telegram.event.TelegramUpdateEvent;

@Component
@RequiredArgsConstructor
public class TelegramUpdateListener
        implements LongPollingSingleThreadUpdateConsumer, SpringLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;
    private final ApplicationEventPublisher publisher;

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            publisher.publishEvent(new TelegramUpdateEvent(update));
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}