package ru.admiralpashtet.reminder.mapper;

import org.mapstruct.*;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;

import java.time.ZoneOffset;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = MapperHelper.class,
        imports = {ZoneOffset.class}
)
public interface ReminderMapper extends BaseMapper<Reminder, ReminderRequest, ReminderResponse> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "remind", expression = "java(dto.remind().toInstant())")
    void updateEntityFromDto(ReminderRequest dto, @MappingTarget Reminder entity);

    @Override
    @Mapping(source = "userId", target = "user", qualifiedByName = "idToUser")
    @Mapping(target = "remind", expression = "java(dto.remind().toInstant())")
    Reminder toEntity(ReminderRequest dto, Long userId);

    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(
            target = "remind",
            expression = "java(mapperHelper.instantToUserOffset(reminder.getRemind(), " +
                    "reminder.getUser().getTimeZone()))"
    )
    ReminderResponse toResponseDTO(Reminder reminder);
}