package nl.evently.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Plain id instead of @ManyToOne: we never navigate to the form, the FK guards integrity.
    @Column(nullable = false, updatable = false)
    private UUID formId;

    // Answers have a different shape per form, so they are stored as one JSONB document.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> answers;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Submission() {
        // for JPA
    }

    public Submission(UUID formId, Map<String, Object> answers) {
        this.formId = formId;
        this.answers = answers;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getFormId() {
        return formId;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
