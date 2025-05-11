package ru.admiralpashtet.reminder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.admiralpashtet.reminder.config.TestConfig;
import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.dto.response.UserResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.service.UserService;
import ru.admiralpashtet.reminder.util.DataUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(UserController.class)
@Import(TestConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test user update functionality")
    void givenNotificationSettingRequest_whenUpdate_thenReturnUserResponse() throws Exception {
        // given
        UserSettingsRequest request = new UserSettingsRequest("changedEmail@mail.ru",
                "changedTelegram");
        BDDMockito.given(userService.updateNotificationSettings(any(UserSettingsRequest.class), anyLong()))
                .willReturn(new UserResponse(request.reminderEmail(), request.telegramUsername()));
        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/users")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
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
        BDDMockito.given(userService.updateNotificationSettings(any(UserSettingsRequest.class), anyLong()))
                .willReturn(new UserResponse("mock@mock.mock", "mock"));
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
        // given
        CustomUserPrincipal customUserPrincipal = DataUtils.mockCustomUserPrincipal();
        BDDMockito.doNothing().when(userService).deleteById(customUserPrincipal.getId());

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser()));

        // then
        perform.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}