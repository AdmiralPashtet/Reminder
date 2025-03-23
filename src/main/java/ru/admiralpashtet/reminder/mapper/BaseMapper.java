package ru.admiralpashtet.reminder.mapper;

public interface BaseMapper<Entity, RequestDTO, ResponseDTO> {
    Entity toEntity(RequestDTO requestDTO);

    ResponseDTO toResponseDTO(Entity entity);
}