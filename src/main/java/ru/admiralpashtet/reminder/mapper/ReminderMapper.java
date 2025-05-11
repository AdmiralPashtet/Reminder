package ru.admiralpashtet.reminder.mapper;

import org.mapstruct.*;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = MapperHelper.class)
public interface ReminderMapper extends BaseMapper<Reminder, ReminderRequest, ReminderResponse> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ReminderRequest dto, @MappingTarget Reminder entity);

    @Override
    @Mapping(source = "userId", target = "user", qualifiedByName = "idToUser")
    Reminder toEntity(ReminderRequest dto, Long userId);

    @Override
    @Mapping(source = "user.id", target = "userId")
    ReminderResponse toResponseDTO(Reminder reminder);
}