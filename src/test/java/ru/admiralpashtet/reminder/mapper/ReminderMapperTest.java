package ru.admiralpashtet.reminder.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.it.BaseIT;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.impl.EmailNotificationSenderService;
import ru.admiralpashtet.reminder.service.impl.TelegramNotificationSenderService;
import ru.admiralpashtet.reminder.telegram.listener.TelegramUpdateListener;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
class ReminderMapperTest extends BaseIT {
    @Autowired
    private ReminderMapper reminderMapper;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private EmailNotificationSenderService emailNotificationSenderService;
    @MockitoBean
    private TelegramNotificationSenderService telegramNotificationSenderService;
    @MockitoBean
    private TelegramBotInitializer telegramBotInitializer;
    @MockitoBean
    private TelegramMessageSender telegramMessageSender;
    @MockitoBean
    private TelegramUpdateListener telegramUpdateListener;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Используем DataUtils для создания тестового пользователя
        testUser = DataUtils.mockUser();
        // При запросе любого userId возвращаем testUser
        BDDMockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
    }

    @Test
    void givenReminderRequest_whenToEntityCalled_thenReturnReminder() {
        //given
        ReminderRequest request = DataUtils.getReminderRequest();

        //when
        Reminder reminder = reminderMapper.toEntity(request, testUser.getId());

        //then
        assertNotNull(reminder);
        assertNotNull(reminder.getUser());
        assertEquals(testUser.getId(), reminder.getUser().getId());
        assertEquals(request.title(), reminder.getTitle());
        assertEquals(request.description(), reminder.getDescription());
    }

    @Test
    void givenReminder_whenToResponseDTOCalled_thenReturnReminderResponse() {
        // given
        Reminder persistedReminder = DataUtils.getReminderPersisted();

        // when
        ReminderResponse response = reminderMapper.toResponseDTO(persistedReminder);

        // then
        assertNotNull(response);
        assertEquals(persistedReminder.getId(), response.id());
        assertEquals(persistedReminder.getTitle(), response.title());
        assertEquals(persistedReminder.getDescription(), response.description());
        // Проверяем, что userId в ответе совпадает с id пользователя из сущности
        assertEquals(persistedReminder.getUser().getId().intValue(), response.userId());
    }

    @Test
    void givenExistsEntityAndRequestDto_whenUpdateEntityFromDtoCalled_thenEntityUpdated() {
        // given
        ReminderRequest request =
                new ReminderRequest("new title", "new description", OffsetDateTime.now());
        Reminder reminder = DataUtils.getReminderPersisted();

        // when
        reminderMapper.updateEntityFromDto(request, reminder);

        // then
        assertEquals(reminder.getTitle(), request.title());
        assertEquals(reminder.getDescription(), request.description());
        assertEquals(reminder.getRemind(), request.remind().toInstant());
    }
}