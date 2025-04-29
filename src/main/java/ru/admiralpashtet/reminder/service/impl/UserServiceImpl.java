package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createOrGetByEmail(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            return userRepository.save(user);
        });
    }

    @Override
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " was not found"));
    }

    @Override
    public User findByTelegramDataUserName(String telegramUserName) {
        return userRepository.findByTelegramDataUserName(telegramUserName)
                .orElseThrow(() -> new UserNotFoundException("User with telegram username " + telegramUserName +
                        " was not found"));
    }

    @Override
    public User update(User user, Long id) {
        return null;
    }
}