package ru.admiralpashtet.reminder.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.request.SearchRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.exception.ReminderNotFoundException;
import ru.admiralpashtet.reminder.mapper.ReminderMapper;
import ru.admiralpashtet.reminder.repository.ReminderRepository;
import ru.admiralpashtet.reminder.service.impl.ReminderServiceImpl;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private ReminderMapper reminderMapper;
    @InjectMocks
    private ReminderServiceImpl reminderService;

    @Test
    @DisplayName("Test reminder creation functionality")
    void givenValidReminderRequest_whenCreateCalled_thenReturnReminderResponse() {
        // given
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();
        Reminder reminderTransient = DataUtils.getReminderTransient();
        Reminder reminderPersisted = DataUtils.getReminderPersisted();
        ReminderResponse reminderResponse = DataUtils.getReminderResponse();

        BDDMockito.given(reminderMapper.toEntity(any(ReminderRequest.class), anyLong()))
                .willReturn(reminderTransient);
        BDDMockito.given(reminderRepository.save(any(Reminder.class)))
                .willReturn(reminderPersisted);
        BDDMockito.given(reminderMapper.toResponseDTO(any(Reminder.class)))
                .willReturn(reminderResponse);

        // when
        ReminderResponse result = reminderService.create(reminderRequest, reminderPersisted.getUser().getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(reminderPersisted.getId());
    }

    @Test
    @DisplayName("Test update reminder functionality")
    void givenValidReminderRequestAndExistsUser_whenUpdateCalled_thenReturnReminderResponse() {
        // given
        Reminder reminderPersisted = DataUtils.getReminderPersisted();
        ReminderRequest reminderRequest =
                new ReminderRequest("Updated title", "description", LocalDateTime.now());

        BDDMockito.given(reminderRepository.findById(anyLong()))
                .willReturn(Optional.of(reminderPersisted));
        BDDMockito.doAnswer(invocationOnMock -> {
                            ReminderRequest request = invocationOnMock.getArgument(0);
                            Reminder entity = invocationOnMock.getArgument(1);
                            entity.setTitle(request.title());
                            return null;
                        }
                )
                .when(reminderMapper).updateEntityFromDto(any(ReminderRequest.class), any(Reminder.class));

        // when
        reminderService.update(reminderRequest, 1L, reminderPersisted.getUser().getId());

        // then
        assertThat(reminderPersisted.getTitle()).isEqualTo(reminderRequest.title());
        BDDMockito
                .verify(reminderMapper).updateEntityFromDto(any(ReminderRequest.class), any(Reminder.class));
    }


    @Test
    @DisplayName("Test update not exists reminder functionality")
    void givenNotExistsReminder_whenUpdateCalled_thenThrowsException() {
        // given
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();

        BDDMockito.given(reminderRepository.findById(anyLong()))
                .willThrow(ReminderNotFoundException.class);

        // when then
        assertThrows(ReminderNotFoundException.class, () -> reminderService.update(reminderRequest, 1L, 1L));
    }

    @Test
    @DisplayName("Test delete by id functionality")
    void givenId_whenDeleteByIdCalled_thenReminderSuccessDeleted() {
        // given
        long id = 5;

        // when
        reminderService.deleteById(id, 1L);

        // then
        BDDMockito.verify(reminderRepository, times(1)).deleteById(id);
    }


    @Test
    @DisplayName("Test find all reminders with default params functionality")
    void givenFiveReminders_whenFindAllCalledWithDefaultParams_thenReturnPageWithFiveReminders() {
        // given
        Page<Reminder> entityPage = DataUtils.getPageOfRemindersPersisted();
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(entityPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });

        // when
        Page<ReminderResponse> resultPage = reminderService.findAll(1L,
                new SearchRequest(null, null, null, "title", true, 0, 10));

        // then
        assertThat(resultPage).isNotNull();
        assertThat(entityPage.getTotalElements()).isEqualTo(resultPage.getTotalElements());

        BDDMockito.verify(reminderRepository, times(1))
                .findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Test find all reminders by search query functionality")
    void givenFiveReminders_whenFindAllCalledWithSearchQuery_thenReturnPageWithFoundedReminders() {
        // given
        String searchKeyword = "meeting";
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> filteredReminders = allReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(searchKeyword)
                        || r.getDescription().toLowerCase().contains(searchKeyword))
                .toList();

        Page<Reminder> filteredPage = new PageImpl<>(filteredReminders, PageRequest.of(0, 10), filteredReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(filteredPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });

        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest("meeting", null, null, "remind", true, 0, 10));

        // then
        assertThat(result).isNotNull();
        result.getContent().forEach(response -> {
            String title = response.title().toLowerCase();
            String description = response.description().toLowerCase();
            assertTrue(title.contains("meeting") || description.contains("meeting"));
        });
    }

    @Test
    @DisplayName("Test find all reminders by not exists search query functionality")
    void givenNotExistsSearchQuery_whenFindAllCalledWithNotExistsSearchQuery_thenReturnBlankPage() {
        // given
        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest("nonexistent", null, null, "remind", true, 0, 10));

        // then
        assertThat(result).isNotNull();
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Test find all reminders with date functionality")
    void givenFiveReminders_whenFindAllCalledWithDate_thenReturnPageWithFoundedReminders() {
        // given
        LocalDate date = LocalDateTime.of(2025, 3, 16, 9, 0).toLocalDate();
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().toLocalDate().equals(date))
                .toList();
        Page<Reminder> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), filtered.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(filteredPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });


        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, date, null, "remind", true, 0, 10));

        // then
        assertThat(result).isNotNull();
        result.getContent().forEach(
                response -> assertThat(response.remind().toLocalDate().isEqual(date)).isTrue()
        );
    }

    @Test
    @DisplayName("Test find all reminders with time functionality")
    void givenFiveReminders_whenFindAllCalledWithTime_thenReturnPageWithFoundedReminders() {
        // given
        LocalTime time = LocalDateTime.of(2025, 3, 16, 9, 0).toLocalTime();
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().toLocalTime().equals(time))
                .toList();
        Page<Reminder> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), filtered.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(filteredPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });

        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, null, time, "remind", true, 0, 10));

        // then
        assertThat(result).isNotNull();
        result.getContent().forEach(
                response -> assertThat(response.remind().toLocalTime()).isEqualTo(time));
    }

    @Test
    @DisplayName("Test find all reminders with date and time functionality")
    void givenFiveReminders_whenFindAllCalledWithDateAndTime_thenReturnPageWithFoundedReminders() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 16, 9, 0);
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().equals(dateTime))
                .toList();
        Page<Reminder> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), filtered.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(filteredPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });

        // when
        Page<ReminderResponse> result = reminderService
                .findAll(1L,
                        new SearchRequest(null,
                                dateTime.toLocalDate(),
                                dateTime.toLocalTime(),
                                "remind",
                                true,
                                0,
                                10));

        // then
        assertThat(result).isNotNull();
        result.getContent().forEach(
                response -> assertThat(response.remind()).isEqualTo(dateTime));
    }

    @Test
    @DisplayName("Test find all with all params functionality")
    void givenFiveReminders_whenFindAllCalledWithAllParams_thenReturnPageWithFoundedReminders() {
        // given
        String searchQuery = "meeting";
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 16, 9, 0);
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> filteredReminders = allReminders.stream()
                .filter(r -> (r.getTitle().toLowerCase().contains(searchQuery.toLowerCase())
                        || r.getDescription().toLowerCase().contains(searchQuery.toLowerCase()))
                        && r.getRemind().toLocalDate().equals(dateTime.toLocalDate())
                        && r.getRemind().toLocalTime().equals(dateTime.toLocalTime()))
                .toList();
        Page<Reminder> filteredPage = new PageImpl<>(filteredReminders, PageRequest.of(0, 10),
                filteredReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(filteredPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return responsePage.getContent().stream()
                            .filter(response -> response.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });

        // when
        Page<ReminderResponse> result = reminderService
                .findAll(1L,
                        new SearchRequest(searchQuery,
                                dateTime.toLocalDate(),
                                dateTime.toLocalTime(),
                                "remind",
                                true,
                                0,
                                10));

        // then
        assertThat(result).isNotNull();
        result.getContent().forEach(response -> {
            boolean contains = response.title().toLowerCase().contains("meeting")
                    || response.description().toLowerCase().contains("meeting");
            assertThat(contains).isTrue();
            assertThat(response.remind()).isEqualTo(dateTime);
        });
    }

    @Test
    @DisplayName("Test find all with sorting by title functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByTitle_thenReturnSortedByTitlePageWithReminders() {
        // given
        String sortBy = "title";
        boolean ascending = true;
        Page<ReminderResponse> reminderResponses = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getTitle)).toList();
        Page<Reminder> sortedPage = new PageImpl<>(sorted, PageRequest.of(0, allReminders.size()), allReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(sortedPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return reminderResponses.getContent().stream()
                            .filter(resp -> resp.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });
        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, null, null, sortBy, ascending, 0, allReminders.size()));

        // then
        assertThat(result).isNotNull();
        List<ReminderResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).title().compareToIgnoreCase(content.get(i + 1).title())).isLessThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("Test find all with sorting by description functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByDescription_thenReturnSortedByDescriptionPageWithReminders() {
        // given
        String sortBy = "description";
        boolean ascending = true;
        Page<ReminderResponse> reminderResponses = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getDescription)).toList();
        Page<Reminder> sortedPage = new PageImpl<>(sorted, PageRequest.of(0, allReminders.size()), allReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(sortedPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return reminderResponses.getContent().stream()
                            .filter(resp -> resp.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });
        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, null, null, sortBy, ascending, 0, allReminders.size()));

        // then
        assertThat(result).isNotNull();
        List<ReminderResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).description().compareToIgnoreCase(content.get(i + 1).description())).isLessThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("Test find all with sorting by remind functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByRemind_thenReturnSortedByRemindPageWithReminders() {
        // given
        String sortBy = "remind";
        boolean ascending = true;
        Page<ReminderResponse> reminderResponses = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind)).toList();
        Page<Reminder> sortedPage = new PageImpl<>(sorted, PageRequest.of(0, allReminders.size()), allReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(sortedPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return reminderResponses.getContent().stream()
                            .filter(resp -> resp.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });
        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, null, null, sortBy, ascending, 0, allReminders.size()));

        // then
        assertThat(result).isNotNull();
        List<ReminderResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            LocalDateTime current = content.get(i).remind();
            LocalDateTime next = content.get(i + 1).remind();
            assertThat(current.isBefore(next) || current.isEqual(next)).isTrue();
        }
    }

    @Test
    @DisplayName("Test find all reminders with null sort parameter")
    void givenNullSortParam_whenFindAllCalledWithNullSortParam_thenThrowsException() {
        // when  then
        assertThrows(IllegalArgumentException.class,
                () -> reminderService.findAll(1L,
                        new SearchRequest(null, null, null, null, true, 0, 5)));

    }

    @Test
    @DisplayName("Test find all reminders with invalid sort parameter")
    void givenInvalidSortParam_whenFindAllCalledWithSortByInvalidParam_thenThrowsException() {
        // given
        String invalidParam = "invalid";
        // when  then
        assertThrows(IllegalArgumentException.class,
                () -> reminderService.findAll(1L,
                        new SearchRequest(null, null, null, invalidParam, true, 0, 5)));
    }

    @Test
    @DisplayName("Test find all with default params and desc")
    void givenFiveReminders_whenFindAllCalledWithDefaultParamsAndDesc_thenReturnPageWithFiveRemindersWithDescSort() {
        String sortBy = "remind";
        boolean ascending = false;
        Page<ReminderResponse> reminderResponses = DataUtils.getPageOfReminderResponsesForOneUser();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<Reminder> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind).reversed()).toList();
        Page<Reminder> sortedPage = new PageImpl<>(sorted, PageRequest.of(0, allReminders.size()), allReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(sortedPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return reminderResponses.getContent().stream()
                            .filter(resp -> resp.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });
        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(null, null, null, sortBy, ascending, 0, allReminders.size()));

        // then
        assertThat(result).isNotNull();
        List<ReminderResponse> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            LocalDateTime current = content.get(i).remind();
            LocalDateTime next = content.get(i + 1).remind();
            assertThat(current.isEqual(next) || current.isAfter(next)).isTrue();
        }
    }

    @Test
    @DisplayName("Test find all reminders with invalid page number parameter")
    void givenInvalidPageNumber_whenFindAllCalledWithMinusPage_thenThrowsException() {
        // given
        int invalidPageNumber = -5;
        // when  then
        assertThrows(IllegalArgumentException.class,
                () -> reminderService.findAll(1L,
                        new SearchRequest(
                                null,
                                null,
                                null,
                                "remind",
                                true,
                                invalidPageNumber,
                                5)));

    }

    @Test
    @DisplayName("Test find all with default params and page number 1 and size 2")
    void givenFiveReminders_whenFindAllCalledWithDefaultParamsWithSize2Page1_thenReturnPageWithTwoReminders() {
        // given
        int page = 1;
        int size = 2;
        String sortBy = "title";
        boolean ascending = true;

        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        int startIndex = size * page;
        int endIndex = Math.min(startIndex + size, allReminders.size());
        List<Reminder> subList = allReminders.subList(startIndex, endIndex);
        Page<Reminder> reminderPage = new PageImpl<>(subList,
                PageRequest.of(page, size),
                allReminders.size());

        BDDMockito.when(reminderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(reminderPage);

        BDDMockito.when(reminderMapper.toResponseDTO(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder entity = invocation.getArgument(0);
                    return DataUtils.getPageOfReminderResponsesForOneUser().getContent().stream()
                            .filter(resp -> resp.id().equals(entity.getId()))
                            .findFirst()
                            .orElseThrow();
                });
        // when
        Page<ReminderResponse> result = reminderService.findAll(1L,
                new SearchRequest(sortBy, null, null, sortBy, ascending, page, size));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(size);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(reminderRepository).findAll(any(Specification.class), captor.capture());
        PageRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getPageSize()).isEqualTo(size);
        assertThat(capturedRequest.getPageNumber()).isEqualTo(page);

    }

    @Test
    @DisplayName("Test find all reminders with invalid page size parameter")
    void givenInvalidSize_whenFindAllCalledWithMinusSize_thenThrowsException() {
        // given
        int invalidSize = -10;
        // when  then
        assertThrows(IllegalArgumentException.class,
                () -> reminderService.findAll(1L,
                        new SearchRequest(null, null, null, "remind", true, 0, invalidSize)));
    }
}