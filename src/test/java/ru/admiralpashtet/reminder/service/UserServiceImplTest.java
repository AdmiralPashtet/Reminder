package ru.admiralpashtet.reminder.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.dto.response.UserResponse;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.mapper.UserMapper;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.impl.UserServiceImpl;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Test create or get user functionality")
    void givenEmail_whenCreateOrGetByEmailCalled_thenReturnNewUser() {
        // given
        long id = 133123L;
        BDDMockito.given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(DataUtils.mockUser(id)));
        // when
        User user = userService.createOrGetByEmail("email@mail.com");
        // then
        assertThat(user.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Test find user by telegram username functionality")
    void givenExistsTelegramUsername_whenFindByTelegramDataUsernameCalled_thenReturnFoundUser() {
        // given
        User user = DataUtils.mockUser();
        BDDMockito.given(userRepository.findByTelegramDataUsername(anyString()))
                .willReturn(Optional.of(user));
        // when
        User foundUser = userService.findByTelegramDataUsername(user.getTelegramData().getUsername());
        // then
        assertThat(foundUser.getTelegramData().getUsername()).isEqualTo(user.getTelegramData().getUsername());
        assertThat(foundUser.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Test find user by non exists telegram username functionality")
    void givenNonExistsTelegramUsername_whenFindByTelegramDataUsernameCalled_thenThrowsUserNotFoundException() {
        // given
        BDDMockito.given(userRepository.findByTelegramDataUsername(anyString()))
                .willThrow(UserNotFoundException.class);
        // when then
        assertThrows(UserNotFoundException.class,
                () -> userService.findByTelegramDataUsername("nonexists"));
    }

    @Test
    @DisplayName("Test update user functionality")
    void givenExistsUser_whenUpdateCalled_thenUserUpdated() {
        // given
        User user = DataUtils.mockUser();
        User updatedUser = DataUtils.mockUser(15L);
        BDDMockito.given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        BDDMockito.given(userRepository.save(any(User.class)))
                .willReturn(updatedUser);
        BDDMockito.given(userMapper.toResponse(any(User.class)))
                .willReturn(new UserResponse(updatedUser.getReminderEmail(),
                        updatedUser.getTelegramData().getUsername()));
        // when
        UserResponse update = userService.update(user, 15L);
        // then
        assertThat(update).isNotNull();
        assertThat(update.reminderEmail()).isEqualTo(updatedUser.getReminderEmail());
        assertThat(update.telegramUsername()).isEqualTo(updatedUser.getTelegramData().getUsername());
    }

    @Test
    @DisplayName("Test update user with non exists user functionality")
    void givenNonExistsUser_whenUpdateCalled_thenThrowsUserNotFoundException() {
        // given
        BDDMockito.given(userRepository.findById(anyLong()))
                .willThrow(UserNotFoundException.class);
        // when then
        assertThrows(UserNotFoundException.class, () -> userService.update(DataUtils.mockUser(), 151L));
    }

    @Test
    @DisplayName("Test update notification settings functionality")
    void givenNotificationSettingsRequest_whenCalledUpdate_thenReturnUserResponse() {
        // given
        User user = DataUtils.mockUser();
        User updatedUser = DataUtils.mockUser(15L);
        BDDMockito.given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        BDDMockito.given(userRepository.save(any(User.class)))
                .willReturn(updatedUser);
        BDDMockito.given(userMapper.toResponse(any(User.class)))
                .willReturn(new UserResponse(updatedUser.getReminderEmail(),
                        updatedUser.getTelegramData().getUsername()));

        BDDMockito.doAnswer(invocationOnMock -> {
                            UserSettingsRequest request = invocationOnMock.getArgument(0);
                            User entity = invocationOnMock.getArgument(1);
                            entity.setReminderEmail(request.reminderEmail());
                            return null;
                        }
                )
                .when(userMapper).updateEntityFromDto(any(UserSettingsRequest.class), any(User.class));

        // when
        UserResponse userResponse = userService.updateNotificationSettings(new UserSettingsRequest(
                "mock", "mock", "UTC"), 15L);
        // then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.reminderEmail()).isEqualTo(updatedUser.getReminderEmail());
        assertThat(userResponse.telegramUsername()).isEqualTo(updatedUser.getTelegramData().getUsername());
    }

    @Test
    @DisplayName("Test delete user functionality")
    void givenUserId_whenDeleteByIdCalled_thenUserDeleted() {
        // given
        long id = 5;
        // when
        userService.deleteById(id);
        // then
        BDDMockito.verify(userRepository, times(1)).deleteById(id);
    }

}