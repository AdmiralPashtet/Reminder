package ru.admiralpashtet.reminder.repository.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.admiralpashtet.reminder.entity.CustomUserPrincipal;
import ru.admiralpashtet.reminder.entity.Reminder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ReminderSpecification {

    private ReminderSpecification() {
    }

    public static Specification<Reminder> hasUserId(Long userId) {
        if (userId == null || userId == 0) {
            throw new IllegalArgumentException("User id must not be null and must be greater then zero");
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Reminder> hasDateAndTime(LocalDate date, LocalTime time) {
        return (root, query, criteriaBuilder) -> {
            Path<OffsetDateTime> path = root.get("remind");
            ZoneId zoneId = ZoneId.of(((CustomUserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal()).getTimeZone());

            if (date != null && time != null) {
                OffsetDateTime offsetDateTime = date.atTime(time).atZone(zoneId).toOffsetDateTime();
                return criteriaBuilder.equal(path, offsetDateTime);
            } else if (date != null) {
                OffsetDateTime start = date.atStartOfDay(zoneId).toOffsetDateTime();
                OffsetDateTime end = date.atTime(LocalTime.MAX).atZone(zoneId).toOffsetDateTime();
                return criteriaBuilder.between(path, start, end);
            } else if (time != null) {
                LocalDate today = LocalDate.now();
                OffsetDateTime atTime = today.atTime(time).atZone(zoneId).toOffsetDateTime();
                return criteriaBuilder.equal(path, atTime);
            }

            throw new IllegalArgumentException("Expected date or/and time parameter");
        };
    }

    public static Specification<Reminder> hasKeywords(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String[] strings = splitByPlus(searchQuery);

                for (String word : strings) {
                    Predicate titlePredicate = criteriaBuilder
                            .like(criteriaBuilder.lower(root.get("title")), "%" + word.toLowerCase() + "%");

                    Predicate descriptionPredicate = criteriaBuilder
                            .like(criteriaBuilder.lower(root.get("description")), "%" + word.toLowerCase() + "%");

                    predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String[] splitByPlus(String string) {
        return string.split("\\+");
    }
}