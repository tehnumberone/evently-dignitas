package nl.evently.submission;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

/**
 * 400 with an {@code errors} map (field name -> message) in the ProblemDetail body.
 */
public class SubmissionValidationException extends ErrorResponseException {

    SubmissionValidationException(Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, problem(errors), null);
    }

    private static ProblemDetail problem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }
}
