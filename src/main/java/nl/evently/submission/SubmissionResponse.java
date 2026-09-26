package nl.evently.submission;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SubmissionResponse(UUID id, UUID formId, Map<String, Object> answers, Instant createdAt) {

    static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getFormId(),
                submission.getAnswers(), submission.getCreatedAt());
    }
}
