package ru.admiralpashtet.reminder.telegram.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.admiralpashtet.reminder.service.UserService;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;
import ru.admiralpashtet.reminder.util.DataUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DefaultCommandHandlerTest {
    @Mock
    private TelegramMessageSender telegramMessageSender;
    @Mock
    private UserService userService;
    @InjectMocks
    private DefaultCommandHandler defaultCommandHandler;

    @Test
    @DisplayName("Test correct update support functionality")
    void givenCorrectUpdate_whenSupportCalled_thenReturnTrue() {
        Update update = DataUtils.createUpdate("text", "user", 123L);
        boolean supports = defaultCommandHandler.supports(update);
        assertThat(supports).isTrue();
    }

    @Test
    @DisplayName("Test incorrect update support functionality")
    void givenIncorrectUpdate_whenSupportCalled_thenReturnFalse() {
        Update update = DataUtils.createUpdate("text", "user", 123L);
        update.setMessage(null);
        boolean supports = defaultCommandHandler.supports(update);
        assertThat(supports).isFalse();
    }

    @Test
    @DisplayName("Test handle update functionality")
    void givenUpdate_whenHandleCalled_thenMessageSend() {
        // given
        Update update = DataUtils.createUpdate("text", "user", 123L);
        BDDMockito.given(userService.findByTelegramDataUsername(any())).willReturn(DataUtils.mockUser());
        // when
        defaultCommandHandler.handle(update);
        // then
        BDDMockito.verify(telegramMessageSender, Mockito.times(1)).reply(any(SendMessage.class));
    }

    @Test
    @DisplayName("Test handle update with unknown telegram user functionality")
    void givenUpdateWithUnknownTelegramUser_whenHandleCalled_thenMessageSend() {
        // given
        Update update = DataUtils.createUpdate("text", "unknownUser", 123L);

        // when then
        BDDMockito.verify(telegramMessageSender, Mockito.never()).reply(any(SendMessage.class));
    }
}