package ru.admiralpashtet.reminder.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.admiralpashtet.reminder.dto.request.UserSettingsRequest;
import ru.admiralpashtet.reminder.dto.response.UserResponse;
import ru.admiralpashtet.reminder.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source = "telegramUsername", target = "telegramData.username")
    void updateEntityFromDto(UserSettingsRequest dto, @MappingTarget User entity);

    @Mapping(source = "telegramData.username", target = "telegramUsername")
    UserResponse toResponse(User user);
}