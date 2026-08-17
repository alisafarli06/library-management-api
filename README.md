# Library Management API

REST API for a small library: catalogue, members, loans, files, and JWT-secured admin account management. Built with Spring Boot 4, Java 21, PostgreSQL, and Flyway.

Companion UI: [library-management-web](https://github.com/alisafarli06/library-management-web).

## Live Demo

| Layer | URL |
|-------|-----|
| Frontend | [library-management-web-4tu2-woad.vercel.app](https://library-management-web-4tu2-woad.vercel.app) |
| API | [library-management-api-8wiv.onrender.com](https://library-management-api-8wiv.onrender.com) |
| Swagger UI | [library-management-api-8wiv.onrender.com/swagger-ui/index.html](https://library-management-api-8wiv.onrender.com/swagger-ui/index.html) |
| OpenAPI JSON | [library-management-api-8wiv.onrender.com/v3/api-docs](https://library-management-api-8wiv.onrender.com/v3/api-docs) |

The Render service may sleep on the free tier; the first request after idle can take a while.

## Key Features

- JWT access + refresh tokens (register, login, refresh, change password)
- Role-based access: `USER` and `ADMIN`
- Admin account management: list users, promote/demote, block/unblock, delete when safe
- Author, book, and member CRUD with search, pagination, and sorting
- Borrow/return (self-service for the signed-in user; admin can borrow on behalf of a member)
- Loan history and borrowing analytics (admin)
- Book cover (JPEG/PNG) and preface (PDF) attachments
- Jakarta Bean Validation and a single JSON error shape via `GlobalExceptionHandler`
- Flyway migrations; Hibernate `ddl-auto=validate`
- Caffeine cache for book-by-id; scheduled cleanup of orphaned upload files
- Simulated welcome email on registration (`@Async`)

## Technology Stack

| Area | Choice |
|------|--------|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Security, Data JPA, Validation, Cache) |
| Database | PostgreSQL |
| Migrations | Flyway |
| Auth | JWT (`jjwt` 0.12.6), BCrypt |
| API docs | springdoc-openapi 3.0.3 (Swagger UI) |
| Cache | Caffeine |
| Build | Gradle Wrapper |
| Deploy | Docker on Render |

## Architecture

Layered Spring Boot API. Controllers never return JPA entities.

```
Browser (Vercel)  →  REST / JWT  →  Spring Boot (Render)
                                        ↓
                                   PostgreSQL
                                   Disk uploads (`FILE_STORAGE_DIRECTORY`)
```

| Layer | Package | Role |
|-------|---------|------|
| HTTP | `controller` | Routes, `@Valid`, OpenAPI annotations |
| Security | `security` | Filter chain, JWT parse/issue, 401/403 JSON |
| Domain | `service` | Business rules, transactions, cache eviction |
| Mapping | `mapper` | Entity ↔ DTO |
| Persistence | `repository`, `entity` | Spring Data JPA + Specifications |
| Config | `config` | CORS, OpenAPI, cache, async, bootstrap admin, file storage |

The frontend stores tokens in `localStorage` and sends `Authorization: Bearer <accessToken>`. CORS allows configured browser origins only (`allowCredentials` is false).

## Authentication and Authorization

1. `POST /api/auth/register` or `POST /api/auth/login` returns `accessToken` and `refreshToken`.
2. Protected routes require `Authorization: Bearer <accessToken>`.
3. `POST /api/auth/refresh` issues a new pair. Blocked accounts cannot log in, refresh, or use an existing JWT (403).
4. `POST /api/auth/change-password` uses the JWT subject (email), not a user id in the body.

Registration always creates `USER`. An `ADMIN` is bootstrapped on startup from `ADMIN_EMAIL` / `ADMIN_INITIAL_PASSWORD` if that email is missing (existing users with that email are promoted to ADMIN; the stored password is not overwritten).

| Path | Who |
|------|-----|
| `/api/auth/register`, `/login`, `/refresh` | Public |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public |
| `GET /api/books/**`, `GET /api/authors/**`, `GET /api/files/**` | Authenticated |
| `/api/user/**` | `USER` or `ADMIN` |
| Member mutations, catalogue mutations, `POST /api/files` | `ADMIN` |
| `/api/admin/**`, `/api/loans/**` | `ADMIN` |

Admin user management (`/api/admin/users`) operates on `User`, not `Member`. Role and `ACTIVE` / `BLOCKED` live on `User`. Guards:

- Cannot change, block, or delete your own account (400)
- Cannot demote, block, or delete the last **ACTIVE** admin (409)
- Delete is refused (409) if the linked member still has loan history — block instead

The web app exposes these actions on the **Members** page for members that have a linked login.

## Swagger / OpenAPI

Interactive API docs are provided by springdoc-openapi (Swagger UI). There is no Postman collection in this repository. Swagger UI and the OpenAPI document are **public** (no JWT to open them). Protected endpoints still require a JWT to execute.

| | Local | Production |
|--|-------|------------|
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | [https://library-management-api-8wiv.onrender.com/swagger-ui/index.html](https://library-management-api-8wiv.onrender.com/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | [https://library-management-api-8wiv.onrender.com/v3/api-docs](https://library-management-api-8wiv.onrender.com/v3/api-docs) |

**Local:** start the API (`./gradlew bootRun`), then open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

**Production:** open the Render Swagger UI URL above. The service may take a moment to wake on the free tier.

To call protected operations from Swagger:

1. `POST /api/auth/login` or `POST /api/auth/register`.
2. Copy `accessToken` from the response.
3. Click **Authorize**, enter `Bearer <accessToken>`, and confirm.

### Endpoint map

List/search endpoints accept Spring Data `page`, `size`, and `sort` (example: `sort=title,asc`).

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/auth/register` | Public; role `USER` |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/refresh` | Public |
| POST | `/api/auth/change-password` | JWT |
| GET/PATCH | `/api/user/profile` | Own profile |
| POST | `/api/user/books/{bookId}/borrow` | Self borrow |
| POST | `/api/user/books/{bookId}/return` | Self return |
| GET | `/api/user/loans` | Own loans |
| GET | `/api/authors`, `/api/authors/search` | `q`, `hasBooks` |
| GET/POST/PUT/DELETE | `/api/authors`, `/api/authors/{id}` | Write: ADMIN |
| GET | `/api/books`, `/api/books/search` | title, author, authorId, year, available |
| POST/PUT/DELETE | `/api/books`, `/api/books/{id}` | ADMIN |
| POST/DELETE | `/api/books/{id}/cover`, `/preface` | ADMIN; cover image, preface PDF |
| GET | `/api/members`, `/api/members/search` | ADMIN; `q` on name/email |
| POST/PUT/DELETE | `/api/members`, `/api/members/{id}` | ADMIN |
| POST | `/api/members/{memberId}/books/{bookId}/borrow` | ADMIN |
| POST | `/api/members/{memberId}/books/{bookId}/return` | ADMIN |
| GET | `/api/loans`, `/api/loans/search` | ADMIN; `q`, `status` |
| GET | `/api/admin/users` | ADMIN; `q`, `role`, `status` |
| PATCH | `/api/admin/users/{id}/role` | `{ "role": "USER" \| "ADMIN" }` |
| PATCH | `/api/admin/users/{id}/status` | `{ "blocked": true \| false }` |
| DELETE | `/api/admin/users/{id}` | ADMIN |
| GET | `/api/admin/analytics/summary` | ADMIN |
| GET | `/api/admin/analytics/books\|authors\|members` | Ranked borrow counts |
| POST | `/api/files` | ADMIN; JPEG, PNG, PDF; default max 10 MB |
| GET | `/api/files/{id}` | Authenticated download |

## Database

Flyway owns the schema (`src/main/resources/db/migration`, currently V1–V12). Do not create application tables by hand.

```
User 1:1? Member  *──* Book *──1 Author
              │         │
              └─ Loan ──┘
Book ──? FileMetadata (cover, preface)
```

| Entity | Table | Notes |
|--------|-------|--------|
| `User` | `users` | Email, BCrypt password, `Role`, `AccountStatus` |
| `Member` | `members` | Library patron; optional `user_id` |
| `Author` / `Book` | `authors` / `books` | Book has `available`, ISBN, optional files |
| `Loan` | `loans` | Borrow history (`returned_at` null = active) |
| `FileMetadata` | `file_metadata` | Bytes on disk; metadata in PostgreSQL |

`role` and blocked status are **not** columns on `Member`. Member list DTOs copy them from the linked `User` when present.

## Installation / Setup

**Needs:** JDK 21, PostgreSQL, this repo’s Gradle wrapper.

1. Clone the repository:

```bash
git clone https://github.com/alisafarli06/library-management-api.git
cd library-management-api
```

2. Create the database (Flyway creates tables on startup; do not create application tables by hand):

```sql
CREATE DATABASE library_db;
```

3. Set environment variables (see [Configuration](#configuration)). Production secrets must not be committed.

4. Run the API:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The API listens on http://localhost:8080. Start the [web app](https://github.com/alisafarli06/library-management-web) separately (`npm run dev` on port 5173). Dev CORS already allows that origin.

## Configuration

Live files: `application.yml` (shared), `application-dev.yml` (local), `application-prod.yml` (Render). A full placeholder copy is in [`application.yml.example`](src/main/resources/application.yml.example). Prefer environment variables over committing secrets. Production has **no** password or JWT defaults.

### `application.yml` example (placeholders only)

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/library_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:YOUR_SECRET}
  jpa:
    hibernate:
      ddl-auto: validate

app:
  jwt:
    secret: ${JWT_SECRET:YOUR_SECRET_AT_LEAST_32_CHARACTERS}
    access-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
    refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173}
  admin:
    email: ${ADMIN_EMAIL:alisafarli@gmail.com}
    full-name: ${ADMIN_FULL_NAME:Ali Safarli}
    password: ${ADMIN_INITIAL_PASSWORD:${ADMIN_PASSWORD:YOUR_SECRET}}
  file:
    storage-directory: ${FILE_STORAGE_DIRECTORY:uploads}
```

### Environment variables

**Local (`dev`):**

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/library_db
export DB_USERNAME=postgres
export DB_PASSWORD=YOUR_SECRET
export JWT_SECRET=YOUR_SECRET_AT_LEAST_32_CHARACTERS
export ADMIN_INITIAL_PASSWORD=YOUR_SECRET
```

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:postgresql://localhost:5432/library_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_SECRET"
$env:JWT_SECRET="YOUR_SECRET_AT_LEAST_32_CHARACTERS"
$env:ADMIN_INITIAL_PASSWORD="YOUR_SECRET"
```

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `dev` locally; `prod` on Render |
| `DB_URL` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL credentials |
| `JWT_SECRET` | HMAC signing secret (≥ 32 characters) |
| `ADMIN_INITIAL_PASSWORD` | Bootstrap ADMIN password (used only if that email does not exist) |
| `CORS_ALLOWED_ORIGINS` | Dev browser origins (comma-separated) |

Optional: `ADMIN_EMAIL`, `ADMIN_FULL_NAME`, `ADMIN_PASSWORD` (fallback if `ADMIN_INITIAL_PASSWORD` is unset), `JWT_ACCESS_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`, `FILE_*`, `CACHE_BOOKS_*`, `ASYNC_*`.

**Production (Render):**

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://YOUR_HOST:5432/library_db
DB_USERNAME=YOUR_SECRET
DB_PASSWORD=YOUR_SECRET
JWT_SECRET=YOUR_SECRET_AT_LEAST_32_CHARACTERS
FILE_STORAGE_DIRECTORY=/var/lib/library/uploads
ADMIN_INITIAL_PASSWORD=YOUR_SECRET
FRONTEND_ORIGIN=https://library-management-web-4tu2-woad.vercel.app
```

`FRONTEND_ORIGIN` is the Vercel origin with **no trailing slash**. `CORS_ALLOWED_ORIGINS` is only a fallback if `FRONTEND_ORIGIN` is unset. The Dockerfile sets `SPRING_PROFILES_ACTIVE=prod`.

## Testing

```bash
./gradlew test    # Windows: .\gradlew.bat test
./gradlew build
```

The suite includes service unit tests (Mockito) and Spring `MockMvc` / `@SpringBootTest` tests against local PostgreSQL (`library_db`). Set `DB_PASSWORD` (and matching `DB_URL` / `DB_USERNAME` if you are not on the YAML defaults) before running tests.

## Deployment

**Backend (Render):** multi-stage `Dockerfile` builds the Boot jar with Java 21 and runs it on a JRE as a non-root user, port 8080, profile `prod`. Attach a PostgreSQL database and set the production env vars above. Uploads live on the instance filesystem; they are **ephemeral** unless you add a persistent disk for `FILE_STORAGE_DIRECTORY`.

**Frontend (Vercel):** set `VITE_API_ORIGIN` to this API origin (`https://library-management-api-8wiv.onrender.com`, no trailing slash).

**CORS:** Render `FRONTEND_ORIGIN` must be the Vercel origin with no trailing slash. A mismatch shows as `Invalid CORS request` in the browser. Local Vite must talk to **localhost:8080**, not Render, or production CORS will reject `http://localhost:5173`.

## Project Structure

```
src/main/java/com/library/
├── config/          # CORS, OpenAPI, cache, async, admin bootstrap
├── controller/      # REST
├── dto/
├── entity/
├── exception/       # GlobalExceptionHandler + ErrorResponse
├── mapper/
├── repository/
├── scheduler/       # Orphan file cleanup
├── security/        # JWT filter, SecurityConfig
├── service/         # Business logic + storage/
└── LibraryManagementApiApplication.java
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/    # Flyway V1–V12
Dockerfile
```

## Demo Account

A bootstrap **ADMIN** is created on first startup for `ADMIN_EMAIL` (config default: `alisafarli@gmail.com`) using `ADMIN_INITIAL_PASSWORD`. The password is not published here. Register your own `USER` from the live app, or set a local admin password in the environment.
