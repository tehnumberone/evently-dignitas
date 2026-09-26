CREATE TABLE form (
    id         UUID PRIMARY KEY,
    name       TEXT        NOT NULL,
    fields     JSONB       NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
