package ru.admiralpashtet.reminder.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.repository.UserRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class MapperHelper {
    private final UserRepository userRepository;

    @Named("instantToUserOffset")
    public static OffsetDateTime instantToUserOffset(Instant instant, String userTimeZone) {
        return instant
                .atZone(ZoneId.of(userTimeZone))
                .toOffsetDateTime();
    }
    @Named("idToUser")
    public User idToUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId + " was not found"));
    }
}