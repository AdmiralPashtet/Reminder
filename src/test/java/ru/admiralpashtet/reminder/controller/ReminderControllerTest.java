package ru.admiralpashtet.reminder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.exception.AccessDeniedException;
import ru.admiralpashtet.reminder.exception.ReminderNotFoundException;
import ru.admiralpashtet.reminder.mapper.ReminderMapper;
import ru.admiralpashtet.reminder.mapper.ReminderMapperImpl;
import ru.admiralpashtet.reminder.mapper.UserMapperHelper;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.ReminderService;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReminderController.class)
@Import({ReminderMapperImpl.class, UserMapperHelper.class})
class ReminderControllerTest {
    @MockitoBean
    private ReminderService reminderService;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;      // mock for UserMapperHelper
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReminderMapper reminderMapper;

    @Test
    @DisplayName("Test reminder creation functionality")
    void givenValidReminderRequest_whenCreateCalled_thenReturnReminderResponse() throws Exception {
        // given
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();
        ReminderResponse reminderResponse = DataUtils.getReminderResponse();

        BDDMockito.given(reminderService.create(any(ReminderRequest.class), anyLong()))
                .willReturn(reminderResponse);

        // when
        ResultActions perform = mockMvc.perform(post("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(reminderResponse.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(reminderResponse.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.remind", CoreMatchers.startsWith(remindToDateTimeWithoutMs(reminderResponse.remind()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", CoreMatchers.is(reminderResponse.userId())));
    }

    @Test
    @DisplayName("Test update reminder functionality")
    void givenValidReminderRequestAndExistsUser_whenUpdateCalled_thenReturnReminderResponse() throws Exception {
        // given
        long id = 1;
        ReminderRequest reminderRequest =
                new ReminderRequest("Updated title", "description", LocalDateTime.now());
        ReminderResponse reminderResponse = new ReminderResponse(1L, reminderRequest.title(),
                reminderRequest.description(), reminderRequest.remind(), 1);

        BDDMockito.given(reminderService.update(
                        any(ReminderRequest.class), anyLong(), anyLong()))
                .willReturn(reminderResponse);

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/reminders/" + id)
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(reminderRequest.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(reminderRequest.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", CoreMatchers.notNullValue()));
    }


    @Test
    @DisplayName("Test update not exists reminder functionality")
    void givenNotExistsReminder_whenUpdateCalled_thenThrowsException() throws Exception {
        // given
        long id = 1L;
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();

        BDDMockito.given(reminderService.update(
                        any(ReminderRequest.class), eq(id), anyLong()))
                .willThrow(new ReminderNotFoundException("Reminder with id " + id + " was not found"));

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/reminders/" + id)
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Reminder with id 1 was not found")));
    }

    @Test
    @DisplayName("Test delete by id functionality")
    void givenId_whenDeleteByIdCalled_thenReminderSuccessDeleted() throws Exception {
        // given
        long id = 5L;
        CustomUserPrincipal customUserPrincipal = DataUtils.mockCustomUserPrincipal();

        BDDMockito.doNothing().when(reminderService).deleteById(id, customUserPrincipal.getId());

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/reminders/" + id)
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(customUserPrincipal))
                .with(csrf()));

        // then
        perform.andDo(print())
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("Test find all reminders with default params functionality")
    void givenFiveReminders_whenFindAllCalledWithDefaultParams_thenReturnPageWithFiveReminders() throws Exception {
        // given
        Page<Reminder> entityPage = DataUtils.getPageOfRemindersPersisted();
        Page<ReminderResponse> responsePage = DataUtils.getPageOfReminderResponsesForOneUser();

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(responsePage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(entityPage.getContent().size())));
    }

    @Test
    @DisplayName("Test find all reminders by search query functionality")
    void givenFiveReminders_whenFindAllCalledWithSearchQuery_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        String searchKeyword = "meeting";
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> filteredReminders = allReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(searchKeyword)
                        || r.getDescription().toLowerCase().contains(searchKeyword))
                .map(reminderMapper::toResponseDTO)
                .toList();

        Page<ReminderResponse> filteredPage = new PageImpl<>(filteredReminders, PageRequest.of(0, 10), allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        eq(searchKeyword),
                        isNull(),
                        isNull(),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(filteredPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByText", searchKeyword));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(filteredReminders.size())));
    }

    @Test
    @DisplayName("Test find all reminders by not exists search query functionality")
    void givenNotExistsSearchQuery_whenFindAllCalledWithNotExistsSearchQuery_thenReturnBlankPage() throws Exception {
        // given
        String searchKeyword = "nonexistent";

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        eq(searchKeyword),
                        isNull(),
                        isNull(),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(Page.empty());

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByText", searchKeyword));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(0)));
    }

    @Test
    @DisplayName("Test find all reminders with date functionality")
    void givenFiveReminders_whenFindAllCalledWithDate_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        LocalDate date = LocalDateTime.of(2025, 3, 16, 9, 0).toLocalDate();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().toLocalDate().equals(date))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        eq(date),
                        isNull(),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(filteredPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByDate", date.toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind", CoreMatchers.startsWith(date.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(filtered.size())));
    }

    @Test
    @DisplayName("Test find all reminders with time functionality")
    void givenFiveReminders_whenFindAllCalledWithTime_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        LocalTime time = LocalDateTime.of(2025, 3, 16, 9, 0).toLocalTime();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().toLocalTime().equals(time))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        eq(time),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(filteredPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByTime", time.toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind", CoreMatchers.endsWith(time.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(filtered.size())));
    }

    @Test
    @DisplayName("Test find all reminders with date and time functionality")
    void givenFiveReminders_whenFindAllCalledWithDateAndTime_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 16, 9, 0);
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> filtered = allReminders.stream()
                .filter(reminder -> reminder.getRemind().equals(dateTime))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> filteredPage = new PageImpl<>(filtered, PageRequest.of(0, 10), allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        eq(dateTime.toLocalDate()),
                        eq(dateTime.toLocalTime()),
                        eq("remind"),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(filteredPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByDate", dateTime.toLocalDate().toString())
                .param("searchByTime", dateTime.toLocalTime().toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind", CoreMatchers.containsString(dateTime.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(filtered.size())));

    }

    @Test
    @DisplayName("Test find all with all params functionality")
    void givenFiveReminders_whenFindAllCalledWithAllParams_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        Reminder first = allReminders.getFirst();
        String searchKeyword = "meeting";
        String sortBy = "title";
        boolean ascending = true;
        int page = 0;
        int size = 5;


        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getTitle))
                .filter(reminder -> reminder.getRemind().toLocalDate().isEqual(first.getRemind().toLocalDate()))
                .filter(reminder -> reminder.getRemind().toLocalTime().equals(first.getRemind().toLocalTime()))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> sortedPage = new PageImpl<>(sorted,
                PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.ASC, sortBy)),
                allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        eq(searchKeyword),
                        eq(first.getRemind().toLocalDate()),
                        eq(first.getRemind().toLocalTime()),
                        eq(sortBy),
                        eq(ascending),
                        eq(page),
                        eq(size)))
                .thenReturn(sortedPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("searchByText", searchKeyword)
                .param("searchByDate", first.getRemind().toLocalDate().toString())
                .param("searchByTime", first.getRemind().toLocalTime().toString())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending))
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(sorted.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted", CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by title functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByTitle_thenReturnSortedByTitlePageWithReminders() throws Exception {
        // given
        String sortBy = "title";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getTitle))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> sortedPage = new PageImpl<>(sorted,
                PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.ASC, sortBy)),
                allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(sortBy),
                        eq(ascending),
                        eq(0),
                        eq(10)))
                .thenReturn(sortedPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted", CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by description functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByDescription_thenReturnSortedByDescriptionPageWithReminders() throws Exception {
        // given
        String sortBy = "description";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getDescription))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> sortedPage = new PageImpl<>(sorted,
                PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.ASC, sortBy)),
                allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(sortBy),
                        eq(ascending),
                        eq(0),
                        eq(10)))
                .thenReturn(sortedPage);
        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted", CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by remind functionality")
    void givenFiveReminders_whenFindAllCalledWithSortByRemind_thenReturnSortedByRemindPageWithReminders() throws Exception {
        // given
        String sortBy = "remind";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind))
                .map(reminderMapper::toResponseDTO)
                .toList();
        Page<ReminderResponse> sortedPage = new PageImpl<>(sorted,
                PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.ASC, sortBy)),
                allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(sortBy),
                        eq(ascending),
                        eq(0),
                        eq(10)))
                .thenReturn(sortedPage);
        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted", CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all reminders with invalid sort parameter")
    void givenInvalidSortParam_whenFindAllCalledWithSortByInvalidParam_thenThrowsException() throws Exception {
        // given
        String invalidParam = "invalid";
        BDDMockito
                .given(reminderService.findAll(1L, null, null, null, invalidParam, true, 0, 10))
                .willThrow(new IllegalArgumentException("Illegal argument in \"sortBy\" URI parameter. Expected: {title, description, remind}"));
        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("sort", invalidParam));

        // when  then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("Illegal argument in \"sortBy\" URI parameter. Expected: {title, description, remind}")));
    }

    @Test
    @DisplayName("Test find all with default params and desc")
    void givenFiveReminders_whenFindAllCalledWithDefaultParamsAndDesc_thenReturnPageWithFiveRemindersWithDescSort() throws Exception {
        String sortBy = "remind";
        boolean ascending = false;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        List<ReminderResponse> sortedReminderResponse = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind).reversed())
                .map(reminderMapper::toResponseDTO).toList();
        Page<ReminderResponse> sortedPage =
                new PageImpl<>(sortedReminderResponse,
                        PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.DESC, sortBy)),
                        allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(sortBy),
                        eq(ascending),
                        eq(0),
                        eq(10)))
                .thenReturn(sortedPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted", CoreMatchers.is(true)));
    }


    @Test
    @DisplayName("Test find all reminders with invalid page number parameter")
    void givenInvalidPageNumber_whenFindAllCalledWithMinusPage_thenThrowsException() throws Exception {
        // given
        int invalidPageNumber = -5;
        BDDMockito
                .given(reminderService.findAll(
                        1L, null, null, null, "remind", true, invalidPageNumber, 10))
                .willThrow(new IllegalArgumentException("Page index must not be less than zero"));
        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("page", String.valueOf(invalidPageNumber)));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Page index must not be less than zero")));
    }

    @Test
    @DisplayName("Test find all with default params and page number 2 and size 2")
    void givenFiveReminders_whenFindAllCalledWithDefaultParamsWithSize2Page2_thenReturnPageWithTwoReminders() throws Exception {
        // given
        int page = 1;
        int size = 2;

        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent();
        int startIndex = size * page;
        int endIndex = Math.min(startIndex + size, allReminders.size());
        List<ReminderResponse> subList = allReminders.subList(startIndex, endIndex).stream()
                .map(reminderMapper::toResponseDTO)
                .toList();

        Page<ReminderResponse> reminderPage = new PageImpl<>(subList,
                PageRequest.of(page, size),
                allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq("remind"),
                        eq(true),
                        eq(page),
                        eq(size)))
                .thenReturn(reminderPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements", CoreMatchers.is(size)));
    }

    @Test
    @DisplayName("Test find all reminders with invalid page size parameter")
    void givenInvalidSize_whenFindAllCalledWithMinusSize_thenThrowsException() throws Exception {
        // given
        int invalidSize = -10;

        BDDMockito
                .given(reminderService.findAll(1L, null, null, null, "remind", true, 0, invalidSize))
                .willThrow(new IllegalArgumentException("Page size must not be less than one"));
        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .param("size", String.valueOf(invalidSize)));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Page size must not be less than one")));
    }

    @Test
    @DisplayName("Test update to other users reminders")
    void givenTwoUsers_whenFirstTryToUpdateReminderOfFirstUser_thenThrowsException() throws Exception {
        // given
        long id = 1;
        ReminderRequest reminderRequest =
                new ReminderRequest("Updated title", "description", LocalDateTime.now());

        BDDMockito.given(reminderService.update(
                        any(ReminderRequest.class), anyLong(), anyLong()))
                .willThrow(new AccessDeniedException("The current user has no access to this reminder"));

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/reminders/" + id)
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("The current user has no access to this reminder")));
    }

    @Test
    @DisplayName("Test find all with few users")
    void givenFiveRemindersTwoForFirstUserThreeForSecond_whenFindAllWithDefaultParamsCalled_thenReturnCorrectReminders() throws Exception {
        // given
        String sortBy = "remind";
        List<Reminder> allReminders = DataUtils.getPageOfRemindersForTwoUsersPersisted().getContent();

        List<ReminderResponse> filtered = allReminders.stream()
                .filter(reminder -> reminder.getUser().getId().equals(2L))
                .map(reminderMapper::toResponseDTO)
                .toList();

        Page<ReminderResponse> filteredPage =
                new PageImpl<>(filtered,
                        PageRequest.of(0, allReminders.size(), Sort.by(Sort.Direction.DESC, sortBy)),
                        allReminders.size());

        BDDMockito.when(reminderService.findAll(
                        isNotNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(sortBy),
                        eq(true),
                        eq(0),
                        eq(10)))
                .thenReturn(filteredPage);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .oauth2User(DataUtils.mockCustomUserPrincipal()))
                .with(csrf()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", CoreMatchers.is(filtered.size())));
    }

    private String remindToDateTimeWithoutMs(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
