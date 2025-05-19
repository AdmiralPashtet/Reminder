package ru.admiralpashtet.reminder.service;

import org.springframework.data.domain.Page;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.request.SearchRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;

import java.util.List;

public interface ReminderService {
    ReminderResponse create(ReminderRequest reminderRequest, Long userId);

    Page<ReminderResponse> findAll(Long userId, SearchRequest searchRequest);

    ReminderResponse update(ReminderRequest reminderRequest, Long reminderId, Long userId);

    void deleteById(Long reminderId, Long userId);

    List<Reminder> findAllByLocalDateTimeNow();
}
