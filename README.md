# Ramkrishna Sales Service — Production Backend

Spring Boot 4.1.1 / Java 25 REST API with PostgreSQL, Flyway, JWT security, document storage and role-based access control.

ADMIN manages users and customer/site master data. SALES_USER manages operational requests they create or are assigned.

Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `DOCUMENT_STORAGE_PATH`, and `CORS_ALLOWED_ORIGINS`.
Run `./mvnw spring-boot:run` or package with `./mvnw clean package`.

## Document storage

New document records store a relative storage key (for example, `2026/10/06/REQ-123/<document-id>_invoice.pdf`) in `documents.storage_key`; `DOCUMENT_STORAGE_PATH` supplies the local storage root. The application uses the `DocumentStorage` interface for file operations, so a cloud-backed implementation can later use the same keys as object keys. Existing records whose keys are absolute paths are mapped using their final year/month/day/request/document path segments, so their files can be copied to a new root while those rows are migrated.

## Request list and search

`GET /api/requests` returns a paginated response rather than the full request list. Use zero-based `page` and a `size` from 1 to 100 (default 50). Optional filters are `search` (matches request number, description, customer name, or company name), `status`, `priority`, `source`, and `assignedTo` (assignee user ID or `unassigned`). Filters are applied in the database before pagination. For example: `/api/requests?page=0&size=50&search=RK-2026&status=NEW&priority=HIGH&source=WEBSITE&assignedTo=u2`.

The response contains `content` and page metadata (`page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `hasNext`, and `hasPrevious`). The frontend should render only `content`, then request the next page when the user navigates or loads more. Search is performed by the backend, so send the search term with each page request instead of downloading all requests and filtering them in the browser.

## Customer and site lists

`GET /api/customers` and `GET /api/sites` return searchable, paginated lists. Both endpoints accept `search`, `page` (one-based, default `1`), and `limit` (default `50`); `limit` must be at least `1` and is capped at `50`. Search is a case-insensitive partial match on the customer or site name, with surrounding whitespace trimmed. Sites also accept the existing optional `customerId` filter, which is applied together with the search before counting and pagination.

For example: `/api/customers?search=acme&page=1&limit=25` and `/api/sites?customerId=c1&search=warehouse&page=1&limit=50`. The response has a `data` array and `pagination` metadata containing `page`, `limit`, `total`, and `totalPages`. A page beyond the last page consistently returns an empty `data` array while retaining the requested page number and reporting the actual `total` and `totalPages`; an empty result has `totalPages: 0`. Results are ordered by name ascending, then ID ascending. A page below `1` or a limit below `1` is rejected.

## Logging

The application writes logs to the console and to `logs/application.log` by default. Set `LOG_DIR` to change the file-log directory. Log files roll over at 100 MB, keeping at most 10 files total (the active file and 9 numbered archives). Docker Compose persists these files in its `log_data` volume; set `LOG_DIR` and mount a persistent volume in other deployments as needed.

Each API response includes an `X-Request-Id` header. Search for that ID in the log file to correlate the HTTP request with its application and security events. Logs intentionally exclude credentials, JWT contents, and request/response bodies.
