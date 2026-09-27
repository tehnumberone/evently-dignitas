package nl.evently;

import nl.evently.form.FormController;
import nl.evently.form.FormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(FormController.class)
class RequestSizeLimitTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    FormService formService;

    @Test
    void bodyOverLimitIsRejectedBeforeReachingTheController() {
        String body = "{\"name\": \"" + "x".repeat((int) RequestSizeLimit.MAX_BODY_BYTES) + "\"}";

        var response = mvc.post().uri("/api/forms").contentType(MediaType.APPLICATION_JSON).content(body);

        assertThat(response).hasStatus(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(response).bodyJson().extractingPath("$.status").isEqualTo(413);
        verify(formService, never()).create(any());
    }
}
