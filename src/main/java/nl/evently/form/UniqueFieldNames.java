package nl.evently.form;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueFieldNames.Validator.class)
public @interface UniqueFieldNames {

    String message() default "field names must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<UniqueFieldNames, List<FieldDefinition>> {

        @Override
        public boolean isValid(List<FieldDefinition> fields, ConstraintValidatorContext context) {
            if (fields == null) return true;
            List<String> names = fields.stream()
                    .filter(Objects::nonNull)
                    .map(FieldDefinition::name)
                    .filter(Objects::nonNull)
                    .toList();
            return names.size() == new HashSet<>(names).size();
        }
    }
}
