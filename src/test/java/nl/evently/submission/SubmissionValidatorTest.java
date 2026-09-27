package nl.evently.submission;

import nl.evently.form.FieldDefinition;
import nl.evently.form.FieldType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionValidatorTest {

    private static final List<FieldDefinition> FIELDS = List.of(
            new FieldDefinition("naam", FieldType.TEXT, true, null),
            new FieldDefinition("email", FieldType.EMAIL, true, null),
            new FieldDefinition("datum", FieldType.DATE, true, null),
            new FieldDefinition("aantal", FieldType.NUMBER, false, null),
            new FieldDefinition("dieet", FieldType.CHOICE, false, List.of("vega", "vlees")));

    private static Map<String, Object> validAnswers() {
        Map<String, Object> answers = new HashMap<>();
        answers.put("naam", "Emre");
        answers.put("email", "emre@example.com");
        answers.put("datum", "2026-10-15");
        answers.put("aantal", 3);
        answers.put("dieet", "vega");
        return answers;
    }

    @Test
    void validSubmissionHasNoErrors() {
        assertThat(SubmissionValidator.validate(FIELDS, validAnswers())).isEmpty();
    }

    @Test
    void optionalFieldsMayBeLeftOut() {
        Map<String, Object> answers = validAnswers();
        answers.remove("aantal");
        answers.put("dieet", null);

        assertThat(SubmissionValidator.validate(FIELDS, answers)).isEmpty();
    }

    @Test
    void missingOrBlankRequiredFieldIsAnError() {
        Map<String, Object> answers = validAnswers();
        answers.remove("naam");
        answers.put("email", "  ");

        assertThat(SubmissionValidator.validate(FIELDS, answers))
                .containsExactlyInAnyOrderEntriesOf(Map.of("naam", "is required", "email", "is required"));
    }

    @Test
    void wrongTypesAreErrors() {
        Map<String, Object> answers = validAnswers();
        answers.put("naam", 42);
        answers.put("email", "not-an-email");
        answers.put("datum", "2026-13-01");
        answers.put("aantal", "3"); // a string, not a number
        answers.put("dieet", "vis");

        // Exact messages: a type error must never be reported as "is required".
        assertThat(SubmissionValidator.validate(FIELDS, answers)).containsExactlyInAnyOrderEntriesOf(Map.of(
                "naam", "must be text of at most " + SubmissionValidator.MAX_TEXT_LENGTH + " characters",
                "email", "must be a valid email address",
                "datum", "must be a date (yyyy-MM-dd)",
                "aantal", "must be a number",
                "dieet", "must be one of [vega, vlees]"));
    }

    @Test
    void textLongerThanMaxIsAnError() {
        Map<String, Object> answers = validAnswers();
        answers.put("naam", "x".repeat(SubmissionValidator.MAX_TEXT_LENGTH + 1));

        assertThat(SubmissionValidator.validate(FIELDS, answers)).containsExactlyEntriesOf(Map.of(
                "naam", "must be text of at most " + SubmissionValidator.MAX_TEXT_LENGTH + " characters"));
    }

    @Test
    void unknownFieldIsAnError() {
        Map<String, Object> answers = validAnswers();
        answers.put("isAdmin", true);

        assertThat(SubmissionValidator.validate(FIELDS, answers))
                .containsExactlyEntriesOf(Map.of("isAdmin", "is not a field of this form"));
    }
}
