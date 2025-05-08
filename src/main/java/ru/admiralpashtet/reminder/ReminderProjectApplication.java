package ru.admiralpashtet.reminder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;

@Slf4j
@SpringBootApplication
public class ReminderProjectApplication {
    public static void main(String[] args) {
        log.info("Java time zone: {}", ZoneId.systemDefault());
        SpringApplication.run(ReminderProjectApplication.class, args);
    }
}