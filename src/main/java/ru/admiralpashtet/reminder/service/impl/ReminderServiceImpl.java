package ru.admiralpashtet.reminder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.exception.AccessDeniedException;
import ru.admiralpashtet.reminder.exception.ReminderNotFoundException;
import ru.admiralpashtet.reminder.mapper.ReminderMapper;
import ru.admiralpashtet.reminder.repository.ReminderRepository;
import ru.admiralpashtet.reminder.repository.specification.ReminderSpecification;
import ru.admiralpashtet.reminder.service.ReminderService;
import ru.admiralpashtet.reminder.sort.SortCondition;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;

    @Override
    public ReminderResponse create(ReminderRequest reminderRequest, Long userId) {
        Reminder entity = reminderMapper.toEntity(reminderRequest, userId);
        Reminder saved = reminderRepository.save(entity);

        return reminderMapper.toResponseDTO(saved);
    }

    @Override
    public Page<ReminderResponse> findAll(Long userId, String searchQuery, LocalDate date, LocalTime time, String sortBy,
                                          boolean ascending, int page, int size) {
        if (sortBy == null || Arrays.stream(SortCondition.values())
                .noneMatch(sortCondition -> sortBy.equalsIgnoreCase(sortCondition.toString()))) {
            throw new IllegalArgumentException("Illegal argument in \"sortBy\" URI parameter. Expected: {title, description, remind}");
        }

        Specification<Reminder> specification = Specification.where(ReminderSpecification.hasUserId(userId));
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
    public ReminderResponse update(ReminderRequest reminderRequest, Long reminderId, Long userId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFoundException("Reminder with id " + reminderId + " was not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("The current user has no access to this reminder");
        }

        reminderMapper.updateEntityFromDto(reminderRequest, reminder);
        Reminder saved = reminderRepository.save(reminder);

        return reminderMapper.toResponseDTO(saved);

    }

    @Override
    public void deleteById(Long reminderId, Long userId) {
        Optional<Reminder> reminder = reminderRepository.findById(reminderId);
        if (reminder.isPresent() && !reminder.get().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("The current user has no access to this reminder");
        }
        reminderRepository.deleteById(reminderId);
    }

    @Override
    public void remind() {
    }
}