package ru.admiralpashtet.reminder.mapper;

import org.mapstruct.*;
import ru.admiralpashtet.reminder.dto.NotificationSettingsRequest;
import ru.admiralpashtet.reminder.dto.UserResponse;
import ru.admiralpashtet.reminder.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "telegramUsername", target = "telegramData.username")
    void updateEntityFromDto(NotificationSettingsRequest dto, @MappingTarget User entity);

    UserResponse toResponse(User user);
}