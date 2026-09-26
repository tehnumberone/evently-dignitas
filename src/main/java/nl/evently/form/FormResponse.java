package nl.evently.form;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormResponse(UUID id, String name, List<FieldDefinition> fields, Instant createdAt) {

    static FormResponse from(Form form) {
        return new FormResponse(form.getId(), form.getName(), form.getFields(), form.getCreatedAt());
    }
}
