package nl.evently.submission;

import nl.evently.error.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

public class SubmissionValidationException extends ErrorResponseException {

    SubmissionValidationException(Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, GlobalExceptionHandler.validationProblem(errors), null);
    }
}
