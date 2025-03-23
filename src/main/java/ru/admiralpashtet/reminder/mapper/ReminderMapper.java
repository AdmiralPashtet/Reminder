package ru.admiralpashtet.reminder.mapper;

import org.mapstruct.*;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapperHelper.class)
public interface ReminderMapper extends BaseMapper<Reminder, ReminderRequest, ReminderResponse> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ReminderRequest dto, @MappingTarget Reminder entity);

    @Override
    @Mapping(source = "userId", target = "user", qualifiedByName = "idToUser")
    Reminder toEntity(ReminderRequest reminderRequest);

    @Override
    @Mapping(source = "user.id", target = "userId")
    ReminderResponse toResponseDTO(Reminder reminder);
}