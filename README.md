# Library Management API

REST API for managing authors, books, and library members. Built with Spring Boot using a layered architecture.

## Features

- Full CRUD REST endpoints for Author, Book, and Member
- DTO-based API responses (entities are never exposed)
- Jakarta Bean Validation on request payloads
- Centralized exception handling with consistent JSON error responses
- Pagination and sorting on list endpoints
- OpenAPI / Swagger UI documentation
- Asynchronous welcome-email simulation on user registration (`@Async`)
- Caffeine-backed book-by-id caching with eviction on successful update/delete
- Externalized YAML configuration with `dev` / `prod` profiles
- Flyway database migrations
- Unit tests for the service layer and exception handler

## Technologies Used

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Gradle
- springdoc-openapi (Swagger UI)
- JUnit 5 & Mockito

## Project Structure

```
src/main/java/com/library/
├── config/          # OpenAPI configuration
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions and global handler
├── mapper/          # Entity ↔ DTO mappers
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
└── LibraryManagementApiApplication.java
```

## Prerequisites

- JDK 21+
- PostgreSQL
- Gradle Wrapper (included — no global Gradle install required)

## Installation Steps

1. Clone the repository: `git clone https://github.com/alisafarli06/library-management-api.git`
2. Review YAML configuration under `src/main/resources/` (`application.yml`, `application-dev.yml`, `application-prod.yml`).
3. Optionally open `src/main/resources/application.yml.example` for a full environment-variable / placeholder reference.
4. Set environment variables for your local database and JWT settings (see below). The default profile is `dev`.
5. Create the PostgreSQL database `library_db` (tables are created by Flyway on startup; do not create application tables by hand).
6. Build and run the application.

## Profiles

| Profile | Purpose | Activation |
|---------|---------|------------|
| `dev` | Local development (safe defaults) | Default, or `SPRING_PROFILES_ACTIVE=dev` |
| `prod` | Production deployment (secrets required via env) | `SPRING_PROFILES_ACTIVE=prod` |

### Development

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_PASSWORD="your_password"
$env:ADMIN_PASSWORD="CHANGE_ME_ADMIN_PASSWORD"
./gradlew bootRun
```

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_PASSWORD=your_password
export ADMIN_PASSWORD=CHANGE_ME_ADMIN_PASSWORD
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production

Provide all required secrets via environment variables (no passwords in YAML):

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://db-host:5432/library_db
export DB_USERNAME=library_app
export DB_PASSWORD=...
export JWT_SECRET=...
export ADMIN_PASSWORD=...
export FILE_STORAGE_DIRECTORY=/var/lib/library/uploads
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Environment Variables

Configuration uses environment variables. Values in the active profile / `application.yml` supply safe defaults for development. Environment variables always override YAML.

| Variable | Purpose | Dev default | Prod |
|----------|---------|-------------|------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` | set to `prod` |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/library_db` | **required** |
| `DB_USERNAME` | Database username | `postgres` | **required** |
| `DB_PASSWORD` | Database password | `CHANGE_ME` | **required** |
| `JWT_SECRET` | HMAC signing secret (min. 32 characters) | placeholder | **required** |
| `JWT_ACCESS_EXPIRATION_MS` | Access token lifetime (ms) | `900000` | `900000` |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token lifetime (ms) | `604800000` | `604800000` |
| `FILE_STORAGE_DIRECTORY` | Upload storage directory | `uploads` | **required** |
| `FILE_MAX_SIZE` | Max upload size in bytes | `10485760` | `10485760` |
| `FILE_ALLOWED_CONTENT_TYPES` | MIME allowlist | `image/jpeg,image/png,application/pdf` | same |
| `FILE_CLEANUP_ENABLED` | Orphaned-file cleanup | `true` | `true` |
| `FILE_CLEANUP_CRON` | Cleanup cron | `0 0 3 * * *` | same |
| `CACHE_BOOKS_MAXIMUM_SIZE` | Book cache max entries | `500` | `500` |
| `CACHE_BOOKS_EXPIRE_AFTER_WRITE_MINUTES` | Book cache TTL (minutes) | `10` | `10` |
| `ASYNC_EXECUTOR_CORE_POOL_SIZE` | Async pool core size | `2` | `2` |
| `ASYNC_EXECUTOR_MAX_POOL_SIZE` | Async pool max size | `4` | `4` |
| `ASYNC_EXECUTOR_QUEUE_CAPACITY` | Async queue capacity | `100` | `100` |
| `ASYNC_NOTIFICATION_DELAY_MS` | Simulated email delay (ms) | `500` | `200` |
| `ADMIN_EMAIL` | Bootstrap ADMIN user email | `admin@library.com` | `admin@library.com` |
| `ADMIN_FULL_NAME` | Bootstrap ADMIN display name | `Library Admin` | `Library Admin` |
| `ADMIN_PASSWORD` | Bootstrap ADMIN password (created only if that email does not exist) | `CHANGE_ME_ADMIN_PASSWORD` | **required** |

### Example (Windows PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:postgresql://localhost:5432/library_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS"
$env:JWT_ACCESS_EXPIRATION_MS="900000"
$env:JWT_REFRESH_EXPIRATION_MS="604800000"
```

### Example (Linux / macOS)

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/library_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS
export JWT_ACCESS_EXPIRATION_MS=900000
export JWT_REFRESH_EXPIRATION_MS=604800000
```

Never commit real secrets. Prefer environment variables (or a local untracked override) over putting production passwords in the repo.

## Bootstrap ADMIN user

On first startup the application creates an ADMIN user if that email is not already in the database. Credentials come from configuration, not from hardcoded values in Java.

- **Local / `dev`:** email `admin@library.com`, password `CHANGE_ME_ADMIN_PASSWORD` unless you set `ADMIN_PASSWORD`.
- **Production:** set `ADMIN_PASSWORD` (required). Optionally override `ADMIN_EMAIL` and `ADMIN_FULL_NAME`.

Mentors can log in via `POST /api/auth/login` with that email and password, then use the access token in Swagger **Authorize**. Change the password in any shared or production environment.

## Database Setup

Create a PostgreSQL database named `library_db`:

```sql
CREATE DATABASE library_db;
```

Default connection settings assume:

- Host: `localhost`
- Port: `5432`
- Database: `library_db`
- Username: `postgres`

Do **not** create application tables (`authors`, `books`, `members`, and so on) by hand. Flyway creates and updates them when the application starts.

## Database / Flyway

Flyway is the schema source of truth. Hibernate uses `spring.jpa.hibernate.ddl-auto=validate` in all profiles (it does not create or alter tables).

- **Fresh empty `library_db`:** on startup Flyway runs `V1__init_library_schema.sql` and `V2__add_books_author_id_index.sql` and creates the current tables, keys, foreign keys (including `books.available`), and the `books.author_id` index.
- **Existing non-empty databases:** `spring.flyway.baseline-on-migrate=true` with `baseline-version=0` records a baseline, then still runs V1. V1 is idempotent (`CREATE TABLE IF NOT EXISTS` and `ADD COLUMN IF NOT EXISTS`), so existing data is kept.
- **`books.available`:** included in the V1 `CREATE TABLE`. If Hibernate previously created `books` without that column, V1 adds it with `ALTER TABLE books ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT TRUE`.

Do **not** run the migration SQL manually when starting the app (`./gradlew bootRun` applies Flyway). Do not drop or recreate tables to “fix” schema.

## Application Configuration (YAML)

Shared settings live in `application.yml`. Profile-specific overrides:

- `application-dev.yml` — local PostgreSQL defaults, DEBUG logging for `com.library`; `ddl-auto=validate`
- `application-prod.yml` — requires `DB_*`, `JWT_SECRET`, `FILE_STORAGE_DIRECTORY`, and `ADMIN_PASSWORD` from the environment; `ddl-auto=validate`

### Configuration example

See [`src/main/resources/application.yml.example`](src/main/resources/application.yml.example) for a full YAML-shaped example with environment-variable placeholders (`CHANGE_ME`, `${DB_PASSWORD:CHANGE_ME}`, etc.).

That file is documentation only — it is safe to commit because it contains **no real credentials**. Prefer setting values via environment variables (or an untracked `application-local.yml`, which is gitignored).

## How to Run the Project

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

Or with an explicit profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The API starts at `http://localhost:8080`.

## How to Run Tests

```bash
./gradlew test
```

## Build Command

```bash
./gradlew build
```

## Swagger / OpenAPI (required for review)

Swagger UI and the OpenAPI document are **public** (no JWT required to open them). Protected API endpoints still require JWT.

After starting the application, open:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### Mentor quick start (authenticate in Swagger)

1. Start the app (`./gradlew bootRun`) with a valid `DB_PASSWORD` for your local PostgreSQL.
2. Open **Swagger UI** (link above). You can browse all endpoints without a token.
3. Under **Authentication**, call **`POST /api/auth/register`** (or **`POST /api/auth/login`** if you already have a user).
4. Copy the `accessToken` from the response.
5. Click **Authorize** (lock icon), enter: `Bearer <accessToken>` (or only the token if the UI already prefixes `Bearer`), then confirm.
6. Call any protected endpoint (Authors, Books, Members, Files, etc.). Requests will send the JWT automatically.

Public auth endpoints (no JWT):

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register and receive access + refresh tokens |
| POST | `/api/auth/login` | Login and receive access + refresh tokens |
| POST | `/api/auth/refresh` | Exchange refresh token for a new token pair |

Example register body:

```json
{
  "fullName": "Jane Doe",
  "email": "jane.doe@example.com",
  "password": "SecurePass123"
}
```

Example login body:

```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePass123"
}
```

### Security summary

| Path | Access |
|------|--------|
| `/api/auth/**` | Public |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public (documentation only) |
| `/api/admin/**` | JWT + **ADMIN** role |
| `/api/user/**` | JWT + **USER** or **ADMIN** |
| All other `/api/**` | JWT required |

A Postman collection is **not** required for this submission because Swagger/OpenAPI covers interactive API exploration.

## Example API Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/authors` | List authors (paginated) | 200 |
| GET | `/api/authors/{id}` | Get author by ID | 200 |
| POST | `/api/authors` | Create author | 201 |
| PUT | `/api/authors/{id}` | Update author | 200 |
| DELETE | `/api/authors/{id}` | Delete author | 204 |
| GET | `/api/books` | List books (paginated) | 200 |
| GET | `/api/books/{id}` | Get book by ID | 200 |
| POST | `/api/books` | Create book | 201 |
| PUT | `/api/books/{id}` | Update book | 200 |
| DELETE | `/api/books/{id}` | Delete book | 204 |
| GET | `/api/members` | List members (paginated) | 200 |
| GET | `/api/members/{id}` | Get member by ID | 200 |
| POST | `/api/members` | Create member | 201 |
| PUT | `/api/members/{id}` | Update member | 200 |
| DELETE | `/api/members/{id}` | Delete member | 204 |
| POST | `/api/files` | Upload file (multipart `file`) | 201 |
| GET | `/api/files/{id}` | Download file by ID | 200 |

### File upload / download

- Allowed types: `image/jpeg`, `image/png`, `application/pdf` (configurable)
- Max size: 10 MB by default (`FILE_MAX_SIZE`)
- Storage: files on disk under `FILE_STORAGE_DIRECTORY`; metadata in the database
- Auth: requires a valid JWT (same as other `/api/**` endpoints)
- Cleanup: a scheduled job deletes orphaned files (on disk but not in `file_metadata`); enabled by default daily at 03:00 (`FILE_CLEANUP_ENABLED`, `FILE_CLEANUP_CRON`)

Example upload (PowerShell):

```powershell
curl.exe -X POST "http://localhost:8080/api/files" `
  -H "Authorization: Bearer $token" `
  -F "file=@./document.pdf"
```

Example upload (bash):

```bash
curl -X POST "http://localhost:8080/api/files" \
  -H "Authorization: Bearer $token" \
  -F "file=@./document.pdf"
```

Download:

```bash
curl -L "http://localhost:8080/api/files/1" \
  -H "Authorization: Bearer $token" \
  -o downloaded.pdf
```

Pagination / sorting example:

```
GET /api/authors?page=0&size=10&sort=name,asc
```

Example create author request body:

```json
{
  "name": "Ali Safarli"
}
```

Example create book request body:

```json
{
  "title": "Harry Potter and the Philosopher's Stone",
  "isbn": "9780747532699",
  "publishedYear": 1997,
  "authorId": 1
}
```

Example create member request body:

```json
{
  "name": "Omar Ismayilov",
  "email": "omar.ismayilov@gmail.com"
}
```

## Layered Architecture Overview

```
Controller → Service → Repository → Database
```

- **Controller** — HTTP mapping, validation trigger (`@Valid`), returns DTOs
- **Service** — Business logic, entity ↔ DTO mapping, not-found handling
- **Repository** — Database access via Spring Data JPA
- **Entity** — Persistence model (not exposed by the API)
- **DTO** — Request/response payload model


