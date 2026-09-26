package nl.evently.form;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // Field definitions differ per form, so they are stored as one JSONB document.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<FieldDefinition> fields;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Form() {
        // for JPA
    }

    public Form(String name, List<FieldDefinition> fields) {
        this.name = name;
        this.fields = fields;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
