package nl.evently.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldDefinition(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,49}$",
                message = "must start with a letter and contain only letters, digits or _ (max 50)")
        String name,

        @NotNull
        FieldType type,

        Boolean required,

        @Size(max = 50)
        List<@NotBlank @Size(max = 100) String> options
) {

    public FieldDefinition {
        required = Boolean.TRUE.equals(required);
    }

    @JsonIgnore
    @AssertTrue(message = "CHOICE fields need options; other field types must not have options")
    public boolean isOptionsValid() {
        boolean hasOptions = options != null && !options.isEmpty();
        return (type == FieldType.CHOICE) == hasOptions;
    }
}
