package ru.admiralpashtet.reminder.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.admiralpashtet.reminder.dto.ExceptionResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    private ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ReminderNotFoundException.class, UserNotFoundException.class})
    private ResponseEntity<ExceptionResponse> handleNotFoundException(Exception exception, WebRequest request) {
        ExceptionResponse response =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now(), getPath(request));
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<Map<String, String>> handleMethodValidationException(HandlerMethodValidationException exception) {
        Map<String, String> errors = exception.getParameterValidationResults().stream()
                .flatMap(parameterValidationResult -> parameterValidationResult.getResolvableErrors().stream())
                .collect(Collectors.toMap(
                        arg -> ((DefaultMessageSourceResolvable) arg.getArguments()[0]).getDefaultMessage(),
                        MessageSourceResolvable::getDefaultMessage
                ));
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    private ResponseEntity<ExceptionResponse> handleIllegalArgumentException(RuntimeException exception, WebRequest request) {
        ExceptionResponse response =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now(), getPath(request));
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        ExceptionResponse response =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now(), getPath(request));
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(Exception exception, WebRequest request) {
        ExceptionResponse response =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now(), getPath(request));
        log.error("An exception was caught: " + exception);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
