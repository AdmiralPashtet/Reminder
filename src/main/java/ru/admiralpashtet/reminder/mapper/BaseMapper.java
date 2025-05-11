package ru.admiralpashtet.reminder.mapper;

public interface BaseMapper<Entity, RequestDTO, ResponseDTO> {
    Entity toEntity(RequestDTO requestDTO, Long userId);

    ResponseDTO toResponseDTO(Entity entity);
}