package nl.evently.error;

import nl.evently.form.FieldDefinition;
import nl.evently.form.FieldType;
import nl.evently.form.Form;
import nl.evently.form.FormController;
import nl.evently.form.FormRepository;
import nl.evently.form.FormService;
import nl.evently.submission.SubmissionController;
import nl.evently.submission.SubmissionRepository;
import nl.evently.submission.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest({FormController.class, SubmissionController.class})
@Import(SubmissionService.class)
class ValidationErrorResponseTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    FormService formService;
    @MockitoBean
    FormRepository forms;
    @MockitoBean
    SubmissionRepository submissions;

    @Test
    void invalidFormReturns400WithFieldErrors() {
        var response = mvc.post().uri("/api/forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "", "fields": [{"name": "1x", "type": "TEXT"}]}""");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(response).bodyJson().extractingPath("$.title").isEqualTo("Validation failed");
        assertThat(response).bodyJson().extractingPath("$.errors").asMap()
                .containsOnlyKeys("name", "fields[0].name");
    }

    @Test
    void duplicateFieldNamesAreReportedOnFields() {
        var response = mvc.post().uri("/api/forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Workshop", "fields": [{"name": "naam", "type": "TEXT"}, {"name": "naam", "type": "EMAIL"}]}""");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(response).bodyJson().extractingPath("$.errors").asMap()
                .containsOnly(Map.entry("fields", "field names must be unique"));
    }

    @Test
    void unknownFieldTypeNamesTheFieldAndTheAllowedTypes() {
        var response = mvc.post().uri("/api/forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Workshop", "fields": [{"name": "naam", "type": "text"}]}""");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(response).bodyJson().extractingPath("$.errors").asMap()
                .containsEntry("fields[0].type", "must be one of [TEXT, EMAIL, NUMBER, DATE, CHOICE]");
    }

    @Test
    void invalidSubmissionReturns400WithFieldErrorsAndIsNotStored() {
        UUID formId = UUID.randomUUID();
        when(forms.findById(formId)).thenReturn(Optional.of(new Form("Workshop", List.of(
                new FieldDefinition("naam", FieldType.TEXT, true, null)))));

        var response = mvc.post().uri("/api/forms/{id}/submissions", formId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"answers": {"hack": 1}}""");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(response).bodyJson().extractingPath("$.title").isEqualTo("Validation failed");
        assertThat(response).bodyJson().extractingPath("$.errors").asMap()
                .containsEntry("naam", "is required")
                .containsEntry("hack", "is not a field of this form");
        verify(submissions, never()).save(any());
    }
}
