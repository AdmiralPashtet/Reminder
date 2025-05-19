package ru.admiralpashtet.reminder.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record SearchRequest(
        @JsonProperty(value = "searchByText")
        @Size(max = 255,
                message = "Search query must be less then 255 characters")
        @Schema(description = "Search query. " +
                "Several words must be connected by a plus. " +
                "The search in performed on all words in sentence.",
                example = "searchByText=sanya+birthday")
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
        @JsonProperty(value = "sort", defaultValue = "remind")
        @Schema(description = "Sort condition parameter.",
                allowableValues = {"title", "description", "remind"},
                defaultValue = "remind")
        String sortBy,
        @JsonProperty(value = "asc", defaultValue = "true")
        @Schema(description = "Sorting direction", defaultValue = "true")
        boolean ascending,
        @JsonProperty(value = "page", defaultValue = "0")
        @Schema(description = "Current page number", defaultValue = "0")
        int page,
        @JsonProperty(value = "size", defaultValue = "10")
        @Schema(description = "Number of elements on one page", defaultValue = "10")
        int size
) {
}