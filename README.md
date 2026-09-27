# Evently — form registration API

A REST API to create sign-up forms with custom fields and accept submissions on them.
Every submission is validated against the fields of its own form.

## Run

```bash
docker compose up --build
```

The API runs on `http://localhost:8080`. Only Docker is needed; the app is built inside the container.
`docker compose down -v` stops everything and wipes the database.

Tests (needs Java 21): `./mvnw test`

## API

| Method | Endpoint                      | Result                              |
|--------|-------------------------------|-------------------------------------|
| POST   | `/api/forms`                  | 201 + `Location` header             |
| GET    | `/api/forms`                  | all forms                           |
| GET    | `/api/forms/{id}`             | 200, or 404                         |
| POST   | `/api/forms/{id}/submissions` | 201, or 400 with an error per field |
| GET    | `/api/forms/{id}/submissions` | all submissions of the form, or 404 |

Create a form:

```bash
curl -i localhost:8080/api/forms -H 'Content-Type: application/json' -d '{
  "name": "Pizza workshop",
  "fields": [
    { "name": "naam",  "type": "TEXT",   "required": true },
    { "name": "email", "type": "EMAIL",  "required": true },
    { "name": "datum", "type": "DATE",   "required": true },
    { "name": "dieet", "type": "CHOICE", "options": ["vega", "vlees"] }
  ]}'
```

Submit to it, using the `id` from the response:

```bash
curl -i localhost:8080/api/forms/{id}/submissions -H 'Content-Type: application/json' -d '{
  "answers": { "naam": "Emre", "email": "emre@example.com", "datum": "2026-10-15", "dieet": "vega" }}'
```

An invalid submission returns every problem at once:

```json
{ "title": "Validation failed", "status": 400,
  "errors": { "email": "must be a valid email address", "datum": "is required", "isAdmin": "is not a field of this form" } }
```

**Field types**

| Type     | Accepts                                    |
|----------|--------------------------------------------|
| `TEXT`   | a string of at most 1000 characters        |
| `EMAIL`  | a string shaped like `x@y.z`               |
| `NUMBER` | a JSON number (`3`, not `"3"`)             |
| `DATE`   | an ISO date, `yyyy-MM-dd`                  |
| `CHOICE` | one of the field's `options` (required for this type only) |

`required` defaults to `false`. An empty or blank answer counts as missing.

All errors, including 404, 405 and broken JSON, use the standard ProblemDetail format (RFC 9457).

## Design choices

A form's structure is data, not code: field definitions are stored with the form, and submissions are checked against them at runtime.

### Validation

- **Two layers.** The form definition has a fixed shape, so it uses Bean Validation: unique field names, a name pattern, `CHOICE` needs options. A submission's shape depends on its form, which annotations can't express, so `SubmissionValidator` reads the form's fields and checks the answers against them.
- **Whitelist, not blacklist.** Answer fields the form doesn't define are rejected, so nothing is stored that the form didn't ask for.
- **Strict types.** No silent conversion: the client sends what the form says. The email check is deliberately loose; the only real proof of an address is mailing it.
- **Everything is bounded.** At most 50 fields and 50 options per form, text at most 1000 characters, and request bodies at most 1 MB (413). Tomcat doesn't limit JSON bodies by itself, so without this a single request could fill the heap before validation even runs. Bodies without a `Content-Length` (chunked) are refused (411), because their size can't be checked up front.
- **Records in, records out.** Requests bind to records that only have the fields we accept, and entities are never exposed. A client can't set fields it shouldn't, such as an id.

### Data

- **Two tables, JSONB.** `form.fields` and `submission.answers` are JSONB, because every form has different fields. A table per field (EAV) would be more complex for no gain at this scope. Downside: harder to query per field, though Postgres can still query JSONB.
- **UUID ids**, so forms can't be enumerated by counting `/forms/1`, `/forms/2`, ...
- **Flyway owns the schema**; Hibernate only validates that the entities match it. Constraints (foreign key, NOT NULL) live in the database. Submissions are always fetched per form, so `submission(form_id)` is indexed.
- **`Submission.formId` is a plain UUID**, not a `@ManyToOne`: the code never navigates to the form, and the foreign key guards integrity.
- **No hand-written SQL.** Repositories are Spring Data, so every query is parameterised.

### Errors and logging

- **One exception handler** extends Spring's own and adds the `errors` map. Anything unexpected becomes a generic 500: the stack trace goes to the server log, never to the client.
- **Logs contain no submission content.** Validation failures are logged with field paths for forms and only a count for submissions. Answers are personal data, and unknown field names are chosen by the client, so logging them would allow fake log lines.

### Structure and deployment

- **Per-feature packages** (`form`, `submission`, `error`). Controllers only do HTTP; services hold the logic and transactions.
- **Multi-stage Docker build.** Maven runs inside the build stage, so a reviewer needs only Docker, and the final image has just a JRE and the jar, running as a non-root user.
- **The database isn't reachable from outside.** Its port isn't published, only the app container can reach it, and the password comes from `DB_PASSWORD` (with a dev default).
- **Minimal dependencies**: web, JPA, validation, Flyway, the Postgres driver, with versions from the Spring Boot BOM.

## Assumptions

- **No authentication.** It needs decisions the assignment doesn't give: who the users are, which identity provider, how users are managed. A secret committed to a public repo isn't authentication. In production, creating forms and reading submissions (which contain personal data) would be admin-only via Spring Security with OAuth2/JWT from an identity provider over HTTPS, and submitting would stay public.
- **Forms can't be edited or deleted.** Not asked for. Editing a form that already has submissions needs form versioning.
- **No pagination** on lists.
- **One organisation**, so no multi-tenancy.

## Next steps

- Authentication and authorisation, as described above.
- Pagination on submissions.
- Form versioning, so forms can be edited.
- Integration tests against a real Postgres (Testcontainers).
- Rate limiting on the public submit endpoint.
