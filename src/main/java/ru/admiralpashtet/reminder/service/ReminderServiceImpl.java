package ru.admiralpashtet.reminder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.exception.ReminderNotFoundException;
import ru.admiralpashtet.reminder.mapper.ReminderMapper;
import ru.admiralpashtet.reminder.repository.ReminderRepository;
import ru.admiralpashtet.reminder.repository.specification.ReminderSpecification;
import ru.admiralpashtet.reminder.sort.SortCondition;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;

    @Override
    public ReminderResponse create(ReminderRequest reminderRequest) {
        Reminder entity = reminderMapper.toEntity(reminderRequest);
        Reminder saved = reminderRepository.save(entity);

        return reminderMapper.toResponseDTO(saved);
    }

    @Override
    public Page<ReminderResponse> findAll(String searchQuery, LocalDate date, LocalTime time, String sortBy,
                                          boolean ascending, int page, int size) {
        if (sortBy == null || Arrays.stream(SortCondition.values())
                .noneMatch(sortCondition -> sortBy.equalsIgnoreCase(sortCondition.toString()))) {
            throw new IllegalArgumentException("Illegal argument in \"sortBy\" URI parameter. Expected: {title, description, remind}");
        }

        Specification<Reminder> specification = Specification.where(null);
        if (searchQuery != null) {
            specification = specification.and(ReminderSpecification.hasKeywords(searchQuery));
        }
        if (date != null || time != null) {
            specification = specification.and(ReminderSpecification.hasDateAndTime(date, time));
        }

        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return reminderRepository.findAll(specification, pageRequest).map(reminderMapper::toResponseDTO);
    }

    @Override
    public ReminderResponse update(ReminderRequest reminderRequest, long id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ReminderNotFoundException("Reminder with id " + id + " was not found"));

        reminderMapper.updateEntityFromDto(reminderRequest, reminder);
        Reminder saved = reminderRepository.save(reminder);

        return reminderMapper.toResponseDTO(saved);

    }

    @Override
    public void deleteById(long reminderId) {
        reminderRepository.deleteById(reminderId);
    }
}