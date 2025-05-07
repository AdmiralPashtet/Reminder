package ru.admiralpashtet.reminder.it;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.User;
import ru.admiralpashtet.reminder.mapper.ReminderMapper;
import ru.admiralpashtet.reminder.repository.ReminderRepository;
import ru.admiralpashtet.reminder.repository.UserRepository;
import ru.admiralpashtet.reminder.service.impl.EmailNotificationSenderService;
import ru.admiralpashtet.reminder.service.impl.TelegramNotificationSenderService;
import ru.admiralpashtet.reminder.telegram.listener.TelegramUpdateListener;
import ru.admiralpashtet.reminder.telegram.sender.TelegramMessageSender;
import ru.admiralpashtet.reminder.util.DataUtils;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // чтобы можно было использовать нестатический метод в @BeforeAll
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReminderControllerIT extends BaseIT {
    @Autowired
    private ReminderRepository reminderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReminderMapper reminderMapper;
    @MockitoBean
    private EmailNotificationSenderService emailNotificationSenderService;
    @MockitoBean
    private TelegramNotificationSenderService telegramNotificationSenderService;
    @MockitoBean
    private TelegramBotInitializer telegramBotInitializer;
    @MockitoBean
    private TelegramMessageSender telegramMessageSender;
    @MockitoBean
    private TelegramUpdateListener telegramUpdateListener;


    @BeforeAll
    void userInit() {
        userRepository.save(DataUtils.mockUser(null));
    }

    @BeforeEach
    void setUp() {
        reminderRepository.deleteAll();
    }

    @Test
    @DisplayName("Test reminder creation functionality")
    void givenValidReminderRequest_whenCreateCalled_thenReturnReminderResponse() throws Exception {
        // given
        ReminderRequest reminderRequest =
                new ReminderRequest("Title", "Description", OffsetDateTime.now(ZoneOffset.UTC));

        // when
        ResultActions perform = mockMvc.perform(post("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title",
                        CoreMatchers.is(reminderRequest.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description",
                        CoreMatchers.is(reminderRequest.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.remind",
                        CoreMatchers.startsWith(reminderRequest.remind().toString().substring(0, 17))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId",
                        CoreMatchers.notNullValue()));
    }

    @Test
    @DisplayName("Test update reminder functionality")
    void givenValidReminderRequestAndExistsUser_whenUpdateCalled_thenReturnReminderResponse() throws Exception {
        // given
        Reminder reminderTransient = DataUtils.getReminderTransient();
        ReminderRequest reminderRequest =
                new ReminderRequest("Updated title", "description", OffsetDateTime.now());

        Reminder saved = reminderRepository.save(reminderTransient);

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/reminders/" + saved.getId())
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        CoreMatchers.is(saved.getId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title",
                        CoreMatchers.is(reminderRequest.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description",
                        CoreMatchers.is(reminderRequest.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId",
                        CoreMatchers.notNullValue()));
    }


    @Test
    @DisplayName("Test update not exists reminder functionality")
    void givenNotExistsReminder_whenUpdateCalled_thenReturnExceptionResponse() throws Exception {
        // given
        long id = 1155521311231L;
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/reminders/" + id)
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("Reminder with id " + id + " was not found")));
    }

    @Test
    @DisplayName("Test delete by id functionality")
    void givenId_whenDeleteByIdCalled_thenReminderSuccessDeleted() throws Exception {
        // given
        Reminder reminderTransient = DataUtils.getReminderTransient();
        Reminder saved = reminderRepository.save(reminderTransient);
        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/reminders/" + saved.getId())
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser()));

        // then
        perform.andDo(print())
                .andExpect(status().isNoContent());
        assertThat(reminderRepository.findById(saved.getId())).isEmpty();
    }


    @Test
    @DisplayName("Test find all reminders with default params functionality")
    void givenFiveReminders_whenGetAllCalledWithDefaultParams_thenReturnPageWithFiveReminders() throws Exception {
        // given
        List<Reminder> list = DataUtils.getPageOfRemindersPersisted()
                .getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();

        reminderRepository.saveAll(list);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
        );

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(list.size())));
    }

    @Test
    @DisplayName("Test find all reminders by search query functionality")
    void givenFiveReminders_whenGetAllCalledWithSearchQuery_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        String searchKeyword = "meeting";
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted()
                .getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        long expectedSize = allReminders.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(searchKeyword)
                        || r.getDescription().toLowerCase().contains(searchKeyword))
                .count();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByText", searchKeyword));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is((int) expectedSize)));
    }

    @Test
    @DisplayName("Test find all reminders by not exists search query functionality")
    void givenNotExistsSearchQuery_whenGetAllCalledWithNotExistsSearchQuery_thenReturnBlankPage() throws Exception {
        // given
        String searchKeyword = "nonexistent";

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByText", searchKeyword));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(0)));
    }

    @Test
    @DisplayName("Test find all reminders with date functionality")
    void givenFiveReminders_whenGetAllCalledWithDate_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        LocalDate date = LocalDateTime.of(2025, 3, 16, 9, 0).toLocalDate();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        long expectedSize = allReminders.stream()
                .filter(reminder -> reminder.getRemind().atOffset(ZoneOffset.UTC).toLocalDate().equals(date))
                .count();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByDate", date.toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind",
                        CoreMatchers.startsWith(date.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is((int) expectedSize)));
    }

    @Test
    @DisplayName("Test find all reminders with time functionality")
    void givenFiveReminders_whenGetAllCalledWithTime_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        LocalTime time = OffsetDateTime.of(2025, 3, 16, 9, 0, 0,
                0, ZoneOffset.UTC).toLocalTime();
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .collect(Collectors.toList());
        allReminders.add(new Reminder(null,
                "Some title",
                "Some description",
                OffsetDateTime.of(LocalDate.now(), time, ZoneOffset.UTC).toInstant(),
                DataUtils.mockUser(1L)));

        long expectedSize = allReminders.stream()
                .filter(reminder -> reminder.getRemind().atOffset(ZoneOffset.UTC).toLocalDate().isEqual(LocalDate.now()))
                .filter(reminder -> reminder.getRemind().atOffset(ZoneOffset.UTC).toLocalTime().equals(time))
                .count();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByTime", time.toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind",
                        CoreMatchers.containsString(time.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is((int) expectedSize)));
    }

    @Test
    @DisplayName("Test find all reminders with date and time functionality")
    void givenFiveReminders_whenGetAllCalledWithDateAndTime_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        OffsetDateTime dateTime = OffsetDateTime.of(2025, 3, 16, 9, 0, 0, 0, ZoneOffset.UTC);
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        long expectedSize = allReminders.stream()
                .filter(reminder -> reminder.getRemind().equals(dateTime.toInstant()))
                .count();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByDate", dateTime.toLocalDate().toString())
                .param("searchByTime", dateTime.toLocalTime().toString()));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind",
                        CoreMatchers.containsString(dateTime.toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is((int) expectedSize)));
    }

    @Test
    @DisplayName("Test find all with all params functionality")
    void givenFiveReminders_whenGetAllCalledWithAllParams_thenReturnPageWithFoundedReminders() throws Exception {
        // given
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        Reminder first = allReminders.getFirst();
        String searchKeyword = "meeting";
        String sortBy = "title";
        boolean ascending = true;
        int page = 0;
        int size = 5;

        long expectedSize = allReminders.stream()
                .filter(reminder -> reminder.getRemind().atOffset(ZoneOffset.UTC).toLocalDate().isEqual(first.getRemind().atOffset(ZoneOffset.UTC).toLocalDate()))
                .filter(reminder -> reminder.getRemind().atOffset(ZoneOffset.UTC).toLocalTime().equals(first.getRemind().atOffset(ZoneOffset.UTC).toLocalTime()))
                .count();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByText", searchKeyword)
                .param("searchByDate", first.getRemind().atOffset(ZoneOffset.UTC).toLocalDate().toString())
                .param("searchByTime", first.getRemind().atOffset(ZoneOffset.UTC).toLocalTime().toString())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending))
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is((int) expectedSize)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted",
                        CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by title functionality")
    void givenFiveReminders_whenGetAllCalledWithSortByTitle_thenReturnSortedByTitlePageWithReminders() throws Exception {
        // given
        String sortBy = "title";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getTitle))
                .map(reminderMapper::toResponseDTO)
                .toList();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].title",
                        CoreMatchers.containsString(sorted.get(0).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].title",
                        CoreMatchers.containsString(sorted.get(1).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].title",
                        CoreMatchers.containsString(sorted.get(2).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].title",
                        CoreMatchers.containsString(sorted.get(3).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted",
                        CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by description functionality")
    void givenFiveReminders_whenGetAllCalledWithSortByDescription_thenReturnSortedByDescriptionPageWithReminders() throws Exception {
        // given
        String sortBy = "description";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getDescription))
                .map(reminderMapper::toResponseDTO)
                .toList();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].description",
                        CoreMatchers.containsString(sorted.get(0).description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].description",
                        CoreMatchers.containsString(sorted.get(1).description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].description",
                        CoreMatchers.containsString(sorted.get(2).description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].description",
                        CoreMatchers.containsString(sorted.get(3).description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted",
                        CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all with sorting by remind functionality")
    void givenFiveReminders_whenGetAllCalledWithSortByRemind_thenReturnSortedByRemindPageWithReminders() throws Exception {
        // given
        String sortBy = "remind";
        boolean ascending = true;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind))
                .map(reminderMapper::toResponseDTO)
                .toList();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind",
                        CoreMatchers.containsString(sorted.get(0).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].remind",
                        CoreMatchers.containsString(sorted.get(1).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].remind",
                        CoreMatchers.containsString(sorted.get(2).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].remind",
                        CoreMatchers.containsString(sorted.get(3).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted",
                        CoreMatchers.is(true)));
    }

    @Test
    @DisplayName("Test find all reminders with invalid sort parameter")
    void givenInvalidSortParam_whenGetAllCalledWithSortByInvalidParam_thenReturnExceptionResponse() throws Exception {
        // given
        String invalidParam = "invalid";

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("sort", invalidParam));

        // when  then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("Illegal argument in \"sortBy\" URI parameter. Expected: {title, description, remind}")));
    }

    @Test
    @DisplayName("Test find all with default params and desc")
    void givenFiveReminders_whenGetAllCalledWithDefaultParamsAndDesc_thenReturnPageWithFiveRemindersWithDescSort() throws Exception {
        String sortBy = "remind";
        boolean ascending = false;
        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        List<ReminderResponse> sorted = allReminders.stream()
                .sorted(Comparator.comparing(Reminder::getRemind).reversed())
                .map(reminder -> reminderMapper.toResponseDTO(reminder)).toList();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("sort", sortBy)
                .param("asc", String.valueOf(ascending)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(allReminders.size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].remind",
                        CoreMatchers.containsString(sorted.get(0).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].remind",
                        CoreMatchers.containsString(sorted.get(1).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].remind",
                        CoreMatchers.containsString(sorted.get(2).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].remind",
                        CoreMatchers.containsString(sorted.get(3).remind().toLocalDateTime().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort.sorted",
                        CoreMatchers.is(true)));
    }


    @Test
    @DisplayName("Test find all reminders with invalid page number parameter")
    void givenInvalidPageNumber_whenGetAllCalledWithMinusPage_thenReturnExceptionResponse() throws Exception {
        // given
        int invalidPageNumber = -5;

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("page", String.valueOf(invalidPageNumber)));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("Page index must not be less than zero")));
    }

    @Test
    @DisplayName("Test find all with default params and page number 2 and size 2")
    void givenFiveReminders_whenGetAllCalledWithDefaultParamsWithSize2Page2_thenReturnPageWithTwoReminders() throws Exception {
        // given
        int page = 1;
        int size = 2;

        List<Reminder> allReminders = DataUtils.getPageOfRemindersPersisted().getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        int startIndex = size * page;
        int endIndex = Math.min(startIndex + size, allReminders.size());
        List<ReminderResponse> subList = allReminders.subList(startIndex, endIndex).stream()
                .map(reminder -> reminderMapper.toResponseDTO(reminder))
                .toList();

        reminderRepository.saveAll(allReminders);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].title",
                        CoreMatchers.is(subList.get(0).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].title",
                        CoreMatchers.is(subList.get(1).title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number",
                        CoreMatchers.is(page)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements",
                        CoreMatchers.is(size)));
    }

    @Test
    @DisplayName("Test find all reminders with invalid page size parameter")
    void givenInvalidSize_whenGetAllCalledWithMinusSize_thenReturnExceptionResponse() throws Exception {
        // given
        int invalidSize = -10;

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("size", String.valueOf(invalidSize)));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("Page size must not be less than one")));
    }

    @Test
    @DisplayName("Test find all with longer then 255 characters search query functionality")
    void givenSearchQueryLongerThen255Characters_whenGetAllCalled_thenReturnExceptionResponse() throws Exception {
        // given
        String query = DataUtils.generateString(300);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByText", query));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.searchByText",
                        CoreMatchers.is("Search query must be less then 255 characters")));
    }

    @Test
    @DisplayName("Test find all with invalid date format functionality")
    void givenInvalidDateFormat_whenGetAllCalled_thenReturnExceptionResponse() throws Exception {
        // given
        String invalidDate = "15/12/2021";

        // when

        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByDate", invalidDate));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.startsWith("Method parameter")));
    }

    @Test
    @DisplayName("Test find all with invalid time functionality")
    void givenInvalidTimeFormat_whenGetAllCalled_thenReturnExceptionResponse() throws Exception {
        // given
        String invalidTime = "21-12";

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
                .param("searchByTime", invalidTime));

        // then
        perform.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.startsWith("Method parameter")));
    }

    @Test
    @DisplayName("Test update to other users reminders")
    void givenTwoUsers_whenFirstTryToUpdateReminderOfFirstUser_thenThrowsException() throws Exception {
        // given
        Reminder reminderTransientFirstUser = DataUtils.getReminderTransient();
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();

        Reminder saved = reminderRepository.save(reminderTransientFirstUser);

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/reminders/" + saved.getId())
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser(
                        DataUtils.mockCustomUserPrincipal(1999L, "mailmail@mail.com", "tg")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reminderRequest)));

        // then
        perform.andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        CoreMatchers.is("The current user has no access to this reminder")));
    }

    @Test
    @DisplayName("Test update to other users reminders")
    void givenTwoUsers_whenFirstTryToDeleteReminderOfFirstUser_thenThrowsException() throws Exception {
        // given
        Reminder reminderTransientFirstUser = DataUtils.getReminderTransient();
        ReminderRequest reminderRequest = DataUtils.getReminderRequest();

        Reminder saved = reminderRepository.save(reminderTransientFirstUser);

        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/reminders/" + saved.getId())
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser(
                        DataUtils.mockCustomUserPrincipal(1999L, "mailmail@mail.com", "tg")))
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
        User user = DataUtils.mockUser(null);
        user.setEmail("email2@mail.com");
        userRepository.save(user);
        List<Reminder> list = DataUtils.getPageOfRemindersForTwoUsersPersisted()
                .getContent()
                .stream()
                .peek(reminder -> reminder.setId(null))
                .toList();
        long userId = list.getFirst().getUser().getId();
        int expectedSize = (int) list.stream()
                .filter(reminder -> reminder.getUser().getId().equals(userId))
                .count();

        reminderRepository.saveAll(list);

        // when
        ResultActions perform = mockMvc.perform(get("/api/v1/reminders")
                .with(DataUtils.securityMockMvcRequestPostProcessorsWithMockUser())
        );

        // then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()",
                        CoreMatchers.is(expectedSize)));
    }

}