package ru.admiralpashtet.reminder.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer;
import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.mapper.UserMapper;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.impl.EmailNotificationSenderService;
import ru.admiralpashtet.reminder.service.impl.TelegramNotificationSenderService;
import ru.admiralpashtet.reminder.telegram.listener.TelegramUpdateListener;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;
import ru.admiralpashtet.reminder.util.DataUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // чтобы можно было использовать нестатический метод в @BeforeAll
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT extends BaseIT {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
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


    @BeforeAll
    void userInit() {
        userRepository.save(DataUtils.mockUser(null));
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test user update functionality")
    void givenNotificationSettingRequest_whenUpdate_thenReturnUserResponse() throws Exception {
        // given
        User saved = userRepository.save(DataUtils.mockUser(null));
        CustomUserPrincipal customUserPrincipal = DataUtils.mockCustomUserPrincipal(saved.getId(), saved.getReminderEmail(),
                saved.getTelegramData().getUsername());
        UserSettingsRequest request = new UserSettingsRequest("changedEmail@mail.ru",
                "changedTelegram");
        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/users")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser(customUserPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        perform.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.reminderEmail", CoreMatchers.is(request.reminderEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.telegramUsername", CoreMatchers.is(request.telegramUsername())));
    }

    @Test
    @DisplayName("Test user invalid request update functionality")
    void givenInvalidNotificationSettingRequest_whenUpdate_thenReturnUserResponse() throws Exception {
        // given
        UserSettingsRequest request = new UserSettingsRequest("dummyemail",
                "changedTelegram");
        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/users")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        perform.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.reminderEmail",
                        CoreMatchers.is("The email must be in the correct format.")));
    }

    @Test
    @DisplayName("Test user delete functionality")
    void delete() throws Exception {
        // given when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser()));

        // then
        perform.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}

