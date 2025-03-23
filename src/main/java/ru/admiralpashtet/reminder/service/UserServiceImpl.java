package ru.admiralpashtet.reminder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.exception.UserNotFoundException;
import ru.admiralpashtet.reminder.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " was not found"));
    }
}