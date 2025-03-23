package ru.admiralpashtet.reminder.exception;

public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException(String message) {
        super(message);
    }
}
