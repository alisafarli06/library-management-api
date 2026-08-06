# Library Management API

REST API for managing authors, books, and library members. Built with Spring Boot using a layered architecture.

## Features

- Full CRUD REST endpoints for Author, Book, and Member
- DTO-based API responses (entities are never exposed)
- Jakarta Bean Validation on request payloads
- Centralized exception handling with consistent JSON error responses
- Pagination and sorting on list endpoints
- OpenAPI / Swagger UI documentation
- Unit tests for the service layer and exception handler

## Technologies Used

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- PostgreSQL
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

1. Clone the repository.
2. Copy the sample configuration (optional if `application.properties` already exists):

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Set environment variables for your local database and JWT settings (see below). Defaults work for local development if they match your setup.
4. Create the database (see below).
5. Build and run the application.

## Environment Variables

Configuration uses environment variables with safe defaults. You can run without setting any variables; Spring will use the defaults in parentheses.

| Variable | Purpose | Default |
|----------|---------|---------|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/library_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `CHANGE_ME` |
| `JWT_SECRET` | HMAC signing secret (min. 32 characters) | `CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS` |
| `JWT_ACCESS_EXPIRATION_MS` | Access token lifetime (ms) | `900000` (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token lifetime (ms) | `604800000` (7 days) |

### Example (Windows PowerShell)

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/library_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS"
$env:JWT_ACCESS_EXPIRATION_MS="900000"
$env:JWT_REFRESH_EXPIRATION_MS="604800000"
```

### Example (Linux / macOS)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/library_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS
export JWT_ACCESS_EXPIRATION_MS=900000
export JWT_REFRESH_EXPIRATION_MS=604800000
```

Never commit real secrets. Prefer environment variables (or a local untracked override) over putting production passwords in the repo.

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

Tables are created/updated automatically via `spring.jpa.hibernate.ddl-auto=update`.

## Application Configuration (`application.properties`)

```properties
spring.application.name=library-management-api

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/library_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:CHANGE_ME}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

app.jwt.secret=${JWT_SECRET:CHANGE_ME_TO_A_SECURE_SECRET_KEY_AT_LEAST_32_CHARS}
app.jwt.access-expiration-ms=${JWT_ACCESS_EXPIRATION_MS:900000}
app.jwt.refresh-expiration-ms=${JWT_REFRESH_EXPIRATION_MS:604800000}
```

Use `application.properties.example` as a template.
## How to Run the Project

```bash
./gradlew bootRun
```

On Windows:

```bash
.\gradlew.bat bootRun
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

## Swagger / OpenAPI

After starting the application:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

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


