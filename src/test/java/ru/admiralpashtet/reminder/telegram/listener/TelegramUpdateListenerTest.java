package ru.admiralpashtet.reminder.telegram.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.telegram.event.TelegramUpdateEvent;
import ru.admiralpashtet.reminder.util.DataUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramUpdateListenerTest {

    @Test
    @DisplayName("Test publish event after update functionality")
    void givenUpdate_whenIncome_thenPublishEvent() {
        ApplicationEventPublisher applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TelegramUpdateListener telegramUpdateListener = new TelegramUpdateListener(applicationEventPublisher);

        Update update = DataUtils.createUpdate("/start", "username", 123L);
        telegramUpdateListener.consume(update);

        ArgumentCaptor<TelegramUpdateEvent> cap = ArgumentCaptor.forClass(TelegramUpdateEvent.class);
        Mockito.verify(applicationEventPublisher).publishEvent(cap.capture());
        assertThat(cap.getValue().getUpdate()).isSameAs(update);
    }
}