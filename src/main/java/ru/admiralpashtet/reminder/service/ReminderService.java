package ru.admiralpashtet.reminder.service;

import org.springframework.data.domain.Page;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReminderService {
    ReminderResponse create(ReminderRequest reminderRequest, Long userId);

    Page<ReminderResponse> findAll(Long userId, String searchQuery, LocalDate date, LocalTime time, String sortBy,
                                   boolean ascending, int page, int size);

    ReminderResponse update(ReminderRequest reminderRequest, Long reminderId, Long userId);

    void deleteById(Long reminderId, Long userId);

    List<Reminder> findAllByLocalDateTimeNow();
}
