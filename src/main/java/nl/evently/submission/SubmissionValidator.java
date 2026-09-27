package nl.evently.submission;

import nl.evently.form.FieldDefinition;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class SubmissionValidator {

    static final int MAX_TEXT_LENGTH = 1000;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private SubmissionValidator() {
    }

    static Map<String, String> validate(List<FieldDefinition> fields, Map<String, Object> answers) {
        Map<String, String> errors = new LinkedHashMap<>();
        Set<String> known = new HashSet<>();

        for (FieldDefinition field : fields) {
            known.add(field.name());
            Object value = answers.get(field.name());
            if (isEmpty(value)) {
                if (field.required()) errors.put(field.name(), "is required");
                continue;
            }
            String error = checkType(field, value);
            if (error != null) errors.put(field.name(), error);
        }

        for (String name : answers.keySet()) {
            if (!known.contains(name)) errors.put(name, "is not a field of this form");
        }
        return errors;
    }

    private static boolean isEmpty(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private static String checkType(FieldDefinition field, Object value) {
        return switch (field.type()) {
            case TEXT -> value instanceof String s && s.length() <= MAX_TEXT_LENGTH
                    ? null : "must be text of at most " + MAX_TEXT_LENGTH + " characters";
            case EMAIL -> value instanceof String s && s.length() <= MAX_EMAIL_LENGTH && EMAIL.matcher(s).matches()
                    ? null : "must be a valid email address";
            case NUMBER -> value instanceof Number
                    ? null : "must be a number";
            case DATE -> value instanceof String s && isIsoDate(s)
                    ? null : "must be a date (yyyy-MM-dd)";
            case CHOICE -> field.options().contains(value)
                    ? null : "must be one of " + field.options();
        };
    }

    private static boolean isIsoDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
