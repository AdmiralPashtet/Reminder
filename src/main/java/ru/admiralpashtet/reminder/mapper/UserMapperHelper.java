package ru.admiralpashtet.reminder.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserMapperHelper {
    private final UserRepository userRepository;

    @Named("idToUser")
    public User idToUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId + " was not found"));
    }
}