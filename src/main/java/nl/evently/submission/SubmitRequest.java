package nl.evently.submission;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitRequest(@NotNull Map<String, Object> answers) {
}
