package nl.evently.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record CreateFormRequest(
        @NotBlank @Size(max = 200)
        String name,

        @NotEmpty @Size(max = 50)
        List<@NotNull @Valid FieldDefinition> fields
) {

    @JsonIgnore
    @AssertTrue(message = "field names must be unique")
    public boolean isFieldNamesUnique() {
        if (fields == null) return true;
        List<String> names = fields.stream()
                .filter(Objects::nonNull)
                .map(FieldDefinition::name)
                .filter(Objects::nonNull)
                .toList();
        return names.size() == new HashSet<>(names).size();
    }
}
