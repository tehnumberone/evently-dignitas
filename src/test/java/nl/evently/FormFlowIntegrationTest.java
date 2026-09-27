package nl.evently;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FormFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    MockMvcTester mvc;

    @Test
    void createFormSubmitAndListSubmissions() {
        var created = mvc.post().uri("/api/forms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Pizza workshop", "fields": [
                          {"name": "naam",  "type": "TEXT",   "required": true},
                          {"name": "aantal", "type": "NUMBER"},
                          {"name": "dieet", "type": "CHOICE", "options": ["vega", "vlees"]}
                        ]}""")
                .exchange();
        assertThat(created).hasStatus(HttpStatus.CREATED);
        assertThat(created).bodyJson().extractingPath("$.fields[0]").asMap().doesNotContainKey("options");
        String formId = created.getResponse().getHeader("Location").replace("/api/forms/", "");

        var submitted = mvc.post().uri("/api/forms/{id}/submissions", formId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"answers": {"naam": "Emre", "aantal": 2, "dieet": "vega"}}""")
                .exchange();
        assertThat(submitted).hasStatus(HttpStatus.CREATED);

        var rejected = mvc.post().uri("/api/forms/{id}/submissions", formId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"answers": {"dieet": "vis"}}""")
                .exchange();
        assertThat(rejected).hasStatus(HttpStatus.BAD_REQUEST);

        var listed = mvc.get().uri("/api/forms/{id}/submissions", formId).exchange();
        assertThat(listed).hasStatusOk();
        assertThat(listed).bodyJson().extractingPath("$").asArray().hasSize(1);
        assertThat(listed).bodyJson().extractingPath("$[0].answers").asMap()
                .isEqualTo(Map.of("naam", "Emre", "aantal", 2, "dieet", "vega"));
    }
}
