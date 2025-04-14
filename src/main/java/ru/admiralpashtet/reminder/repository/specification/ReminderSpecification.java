package ru.admiralpashtet.reminder.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.admiralpashtet.reminder.entity.Reminder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            if (date != null && time != null) {
                LocalDateTime localDateTime = LocalDateTime.of(date, time);
                return criteriaBuilder.equal(root.get("remind"), localDateTime);
            } else if (date != null) {
                return criteriaBuilder.between(root.get("remind"),
                        LocalDateTime.of(date, LocalTime.of(0, 0, 0)),
                        LocalDateTime.of(date, LocalTime.of(23, 59, 59)));
            } else if (time != null) {
                return criteriaBuilder.equal(root.get("remind"), LocalDateTime.of(LocalDate.now(), time));
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