package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.dto.NotificationSettingsRequest;
import ru.admiralpashtet.reminder.dto.UserResponse;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.mapper.UserMapper;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User createOrGetByEmail(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            return userRepository.save(user);
        });
    }

    @Override
    public User findByTelegramDataUserName(String telegramUserName) {
        return userRepository.findByTelegramDataUsername(telegramUserName)
                .orElseThrow(() -> new UserNotFoundException("User with telegram username " + telegramUserName +
                        " was not found"));
    }

    @Override
    public UserResponse updateNotificationSettings(NotificationSettingsRequest settingsRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " was not found"));
        userMapper.updateEntityFromDto(settingsRequest, user);
        if (settingsRequest.telegramUsername() == null) {
            user.getTelegramData().setChatId(null);
        }
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse update(User user, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + userId + " was not found");
        }
        user.setId(userId);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }
}