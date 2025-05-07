package ru.admiralpashtet.reminder.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.admiralpashtet.reminder.dto.request.ReminderRequest;
import ru.admiralpashtet.reminder.dto.response.ReminderResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.TelegramData;
import ru.admiralpashtet.reminder.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DataUtils {

    public static Page<Reminder> getPageOfRemindersPersisted() {
        return new PageImpl<>(List.of(
                new Reminder(
                        1L,
                        "Reminder1: Meeting Notes",
                        "Prepare notes for team meeting",
                        OffsetDateTime.of(2025, 3, 16, 9, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        OffsetDateTime.of(2025, 3, 16, 14, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        OffsetDateTime.of(2025, 3, 17, 11, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        OffsetDateTime.of(2025, 3, 17, 15, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        OffsetDateTime.of(2025, 3, 18, 10, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                )
        ), PageRequest.of(0, 10), 5);
    }

    public static Page<Reminder> getPageOfRemindersForTwoUsersPersisted() {
        return new PageImpl<>(List.of(
                new Reminder(
                        1L,
                        "Reminder1: Meeting Notes",
                        "Prepare notes for team meeting",
                        OffsetDateTime.of(2025, 3, 16, 9, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        OffsetDateTime.of(2025, 3, 16, 14, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser()
                ),
                new Reminder(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        OffsetDateTime.of(2025, 3, 17, 11, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser(2L)
                ),
                new Reminder(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        OffsetDateTime.of(2025, 3, 17, 15, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser(2L)
                ),
                new Reminder(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        OffsetDateTime.of(2025, 3, 18, 10, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        mockUser(2L)
                )
        ), PageRequest.of(0, 10), 5);
    }

    /**
     * Повторяющиеся слова:
     * "meeting" встречается в title (ID: 1) и description (ID: 1, 2).
     * "plan" встречается в title (ID: 2, 5) и description (ID: 2, 5).
     * "team" встречается в title (ID: 3) и description (ID: 1, 3).
     * "notes" встречается в title (ID: 1) и description (ID: 1, 3).
     * "task" встречается в title (ID: 4) и description (ID: 4).
     * Это позволяет тестировать поиск с несколькими совпадениями:
     * "meeting" найдет ID: 1, 2.
     * "plan" найдет ID: 2, 5.
     * "team+notes" найдет ID: 1, 3.
     */
    public static Page<ReminderResponse> getPageOfReminderResponsesForOneUser() {
        return new PageImpl<>(List.of(
                new ReminderResponse(
                        1L,
                        "Reminder1: Meeting Notes",
                        "Prepare notes for team meeting",
                        OffsetDateTime.of(2025, 3, 16, 9, 0, 0, 0, ZoneOffset.UTC),
                        1
                ),
                new ReminderResponse(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        OffsetDateTime.of(2025, 3, 16, 14, 0, 0, 0, ZoneOffset.UTC),
                        1
                ),
                new ReminderResponse(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        OffsetDateTime.of(2025, 3, 17, 11, 0, 0, 0, ZoneOffset.UTC),
                        1
                ),
                new ReminderResponse(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        OffsetDateTime.of(2025, 3, 17, 15, 0, 0, 0, ZoneOffset.UTC),
                        1
                ),
                new ReminderResponse(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        OffsetDateTime.of(2025, 3, 18, 10, 0, 0, 0, ZoneOffset.UTC),
                        1
                )
        ), PageRequest.of(0, 10), 5);


    }

    public static ReminderRequest getReminderRequest() {
        return new ReminderRequest("Title", "Description", OffsetDateTime.now().plusDays(1));
    }

    public static ReminderResponse getReminderResponse() {
        return new ReminderResponse(1L, "Title", "Description", OffsetDateTime.now().plusDays(1), 1);
    }

    public static Reminder getReminderTransient() {
        return new Reminder(null, "Title", "Description", OffsetDateTime.now().plusDays(1).toInstant(), mockUser());
    }

    public static Reminder getReminderPersisted() {
        return new Reminder(1L, "Title", "Description", OffsetDateTime.now().plusDays(1).toInstant(), mockUser());
    }

    public static User mockUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email@mail.com");
        user.setReminderEmail("email@mail.com");
        user.setTelegramData(new TelegramData("telegram", 1233L));
        user.setTimeZone("UTC");
        return user;
    }

    public static User mockUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("email@mail.com");
        user.setReminderEmail("email@mail.com");
        user.setTelegramData(new TelegramData("telegram", 1233L));
        user.setTimeZone("UTC");
        return user;
    }

    public static CustomUserPrincipal mockCustomUserPrincipal() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("email@mail.com");
        testUser.setTelegramData(new TelegramData("telegram", 1233L));
        testUser.setTimeZone("UTC");
        return new CustomUserPrincipal(testUser);
    }

    public static CustomUserPrincipal mockCustomUserPrincipal(long userId, String email, String telegram) {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        testUser.setTelegramData(new TelegramData("telegram", 1233L));
        return new CustomUserPrincipal(testUser);
    }

    public static String generateString(int length) {
        if (length > 0) {
            char[] chars = new char[length];
            Arrays.fill(chars, 'a');
            return new String(chars);
        }
        return "";
    }

    public static RequestPostProcessor securityMockMvcRequestPostProcessorsWithMockUser() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(DataUtils.mockUser());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserPrincipal, null, Collections.emptyList());
        return SecurityMockMvcRequestPostProcessors.authentication(authenticationToken);
    }

    public static RequestPostProcessor securityMockMvcRequestPostProcessorsWithMockUser(CustomUserPrincipal mockCustomUserPrincipal) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                mockCustomUserPrincipal, null, Collections.emptyList());
        return SecurityMockMvcRequestPostProcessors.authentication(authenticationToken);
    }

    public static Update createUpdate(String text, String username, long chatId) {
        Update update = new Update();
        Message message = new Message();
        message.setChat(new Chat(123L, "private"));
        message.setText(text);
        message.setFrom(new org.telegram.telegrambots.meta.api.objects.User(1L, username, false));
        update.setMessage(message);
        return update;
    }
}