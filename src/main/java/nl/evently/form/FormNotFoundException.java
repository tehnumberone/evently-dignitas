package nl.evently.form;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class FormNotFoundException extends ResponseStatusException {

    public FormNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Form " + id + " not found");
    }
}
