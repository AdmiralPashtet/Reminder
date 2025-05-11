package ru.admiralpashtet.reminder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionResponse {
    private String message;
    private LocalDateTime localDateTime;
    private String path;
}
