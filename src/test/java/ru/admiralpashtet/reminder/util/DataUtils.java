package ru.admiralpashtet.reminder.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.admiralpashtet.reminder.dto.ReminderRequest;
import ru.admiralpashtet.reminder.dto.ReminderResponse;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.Reminder;
import ru.admiralpashtet.reminder.entity.User;

import java.time.LocalDateTime;
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
                        LocalDateTime.of(2025, 3, 16, 9, 0),
                        mockUser()
                ),
                new Reminder(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        LocalDateTime.of(2025, 3, 16, 14, 0),
                        mockUser()
                ),
                new Reminder(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        LocalDateTime.of(2025, 3, 17, 11, 0),
                        mockUser()
                ),
                new Reminder(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        LocalDateTime.of(2025, 3, 17, 15, 0),
                        mockUser()
                ),
                new Reminder(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        LocalDateTime.of(2025, 3, 18, 10, 0),
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
                        LocalDateTime.of(2025, 3, 16, 9, 0),
                        mockUser()
                ),
                new Reminder(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        LocalDateTime.of(2025, 3, 16, 14, 0),
                        mockUser()
                ),
                new Reminder(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        LocalDateTime.of(2025, 3, 17, 11, 0),
                        mockUser(2)
                ),
                new Reminder(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        LocalDateTime.of(2025, 3, 17, 15, 0),
                        mockUser(2)
                ),
                new Reminder(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        LocalDateTime.of(2025, 3, 18, 10, 0),
                        mockUser(2)
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
                        LocalDateTime.of(2025, 3, 16, 9, 0),
                        1
                ),
                new ReminderResponse(
                        2L,
                        "Reminder4: Project Plan",
                        "Draft plan for next meeting",
                        LocalDateTime.of(2025, 3, 16, 14, 0),
                        1
                ),
                new ReminderResponse(
                        3L,
                        "Reminder2: Team Review",
                        "Review team progress notes",
                        LocalDateTime.of(2025, 3, 17, 11, 0),
                        1
                ),
                new ReminderResponse(
                        4L,
                        "Reminder5: Task List",
                        "Update task list for project",
                        LocalDateTime.of(2025, 3, 17, 15, 0),
                        1
                ),
                new ReminderResponse(
                        5L,
                        "Reminder3: Budget Plan",
                        "Plan budget for next quarter",
                        LocalDateTime.of(2025, 3, 18, 10, 0),
                        1
                )
        ), PageRequest.of(0, 10), 5);


    }

    public static ReminderRequest getReminderRequest() {
        return new ReminderRequest("Title", "Description", LocalDateTime.now().plusDays(1));
    }

    public static ReminderResponse getReminderResponse() {
        return new ReminderResponse(1L, "Title", "Description", LocalDateTime.now().plusDays(1), 1);
    }

    public static Reminder getReminderTransient() {
        return new Reminder(null, "Title", "Description", LocalDateTime.now().plusDays(1), mockUser());
    }

    public static Reminder getReminderPersisted() {
        return new Reminder(1L, "Title", "Description", LocalDateTime.now().plusDays(1), mockUser());
    }

    public static User mockUser() {
        return new User(1L, "email@mail.com", "telegram");
    }

    public static User mockUser(long id) {
        return new User(id, "email@mail.com", "telegram");
    }

    public static CustomUserPrincipal mockCustomUserPrincipal() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("email@mail.com");
        testUser.setTelegram("telegram");

        return new CustomUserPrincipal(testUser);
    }

    public static CustomUserPrincipal mockCustomUserPrincipal(long userId, String email, String telegram) {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        testUser.setTelegram(telegram);

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

}