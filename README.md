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
2. Copy the sample configuration:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Edit `application.properties` and set your local PostgreSQL password (`random123`).
4. Create the database (see below).
5. Build and run the application.

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

spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=postgres
spring.datasource.password=random123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Use `application.properties.example` as a template. Never commit a real database password.

You can also override the password at runtime:

```bash
# Windows PowerShell
$env:SPRING_DATASOURCE_PASSWORD="your_password"

# Linux / macOS
export SPRING_DATASOURCE_PASSWORD=your_password
```

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


