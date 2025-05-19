package ru.admiralpashtet.reminder.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record SearchRequest(
        @JsonProperty(value = "searchQuery")
        @Size(max = 255,
                message = "Search query must be less then 255 characters")
        @Schema(description = "Search query. " +
                "Several words must be connected by a plus. " +
                "The search in performed on all words in sentence.",
                example = "searchQuery=sanya+birthday")
        String searchQuery,
        @JsonProperty(value = "searchByDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "ISO format is used.",
                example = "searchByDate=2025-05-06")
        LocalDate date,
        @JsonProperty(value = "searchByTime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        @Schema(description = "ISO format is used.",
                example = "searchByTime=15:50")
        LocalTime time,
        @JsonProperty(value = "sortBy")
        @Schema(description = "Sort condition parameter.",
                allowableValues = {"title", "description", "remind"},
                defaultValue = "remind")
        String sortBy,
        @JsonProperty(value = "asc")
        @Schema(description = "Sorting direction", defaultValue = "true")
        boolean ascending,
        @JsonProperty(value = "page")
        @Schema(description = "Current page number", defaultValue = "0")
        @PositiveOrZero(message = "Page number must be positive or zero")
        int page,
        @JsonProperty(value = "size")
        @Schema(description = "Number of elements on one page", defaultValue = "10")
        @Positive(message = "Page size must be positive")
        int size
) {
}