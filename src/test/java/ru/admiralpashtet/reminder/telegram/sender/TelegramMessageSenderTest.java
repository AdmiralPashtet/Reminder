package ru.admiralpashtet.reminder.telegram.sender;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.TelegramData;
import ru.admiralpashtet.reminder.util.DataUtils;

@ExtendWith(MockitoExtension.class)
class TelegramMessageSenderTest {
    @Mock
    private TelegramClient telegramClient;
    @InjectMocks
    private TelegramMessageSender telegramMessageSender;

    @Test
    @DisplayName("Test reply message")
    void givenMessage_whenReply_thenMessageSend() throws TelegramApiException {
        // given
        SendMessage message = SendMessage.builder()
                .chatId(123L)
                .parseMode(ParseMode.MARKDOWNV2)
                .text("text")
                .build();
        // when
        telegramMessageSender.reply(message);
        // then
        BDDMockito.verify(telegramClient, Mockito.times(1)).execute(message);
    }

    @Test
    @DisplayName("Test send message functionality")
    void givenReminder_whenSendMessageCalled_thenMessageSend() throws TelegramApiException {
        // given
        Reminder reminder = DataUtils.getReminderPersisted();
        SendMessage message = SendMessage.builder()
                .chatId(reminder.getUser().getTelegramData().getChatId())
                .parseMode(ParseMode.MARKDOWNV2)
                .text(String.format("*%s*\n%s", reminder.getTitle(), reminder.getDescription()))
                .build();
        // when
        telegramMessageSender.sendMessage(reminder);
        // then
        BDDMockito.verify(telegramClient, Mockito.times(1)).execute(message);
    }

    @Test
    @DisplayName("Test send message without chat id functionality")
    void givenReminderWithoutChatId_whenSendMessageCalled_thenThrowsException() throws TelegramApiException {
        // given
        Reminder reminder = DataUtils.getReminderPersisted();
        reminder.getUser().setTelegramData(new TelegramData("mock", null));
        // when then
        BDDMockito.verify(telegramClient, Mockito.never()).execute(ArgumentMatchers.any(SendMessage.class));
    }
}