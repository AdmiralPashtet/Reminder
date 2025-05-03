package ru.admiralpashtet.reminder.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.impl.EmailNotificationSenderService;
import ru.admiralpashtet.reminder.service.impl.TelegramNotificationSenderService;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;


@SpringBootTest
@ActiveProfiles("test")
class ReminderMapperTest {
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
                new ReminderRequest("new title", "new description", LocalDateTime.now());
        Reminder reminder = DataUtils.getReminderPersisted();

        // when
        reminderMapper.updateEntityFromDto(request, reminder);

        // then
        assertEquals(reminder.getTitle(), request.title());
        assertEquals(reminder.getDescription(), request.description());
        assertEquals(reminder.getRemind(), request.remind());
    }
}