package nl.evently.submission;

import nl.evently.form.Form;
import nl.evently.form.FormNotFoundException;
import nl.evently.form.FormRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final FormRepository forms;
    private final SubmissionRepository submissions;

    public SubmissionService(FormRepository forms, SubmissionRepository submissions) {
        this.forms = forms;
        this.submissions = submissions;
    }

    @Transactional
    public SubmissionResponse submit(UUID formId, SubmitRequest request) {
        Form form = forms.findById(formId).orElseThrow(() -> new FormNotFoundException(formId));
        Map<String, String> errors = SubmissionValidator.validate(form.getFields(), request.answers());
        if (!errors.isEmpty()) {
            log.warn("Rejected submission for form {}: {} invalid field(s)", formId, errors.size());
            throw new SubmissionValidationException(errors);
        }
        Submission submission = submissions.save(new Submission(formId, request.answers()));
        return SubmissionResponse.from(submission);
    }

    public List<SubmissionResponse> findByForm(UUID formId) {
        if (!forms.existsById(formId)) {
            throw new FormNotFoundException(formId);
        }
        return submissions.findByFormIdOrderByCreatedAtAsc(formId).stream()
                .map(SubmissionResponse::from)
                .toList();
    }
}
