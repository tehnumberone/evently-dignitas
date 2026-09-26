CREATE TABLE submission (
    id         UUID PRIMARY KEY,
    form_id    UUID        NOT NULL REFERENCES form (id),
    answers    JSONB       NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- Submissions are always fetched per form.
CREATE INDEX idx_submission_form_id ON submission (form_id);
