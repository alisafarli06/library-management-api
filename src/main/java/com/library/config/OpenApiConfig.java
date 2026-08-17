package com.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_AUTH_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI libraryOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Library Management API")
						.description("""
								REST API for managing library authors, books, members, and file uploads.

								**Authentication:** Most endpoints require a JWT access token from \
								`POST /api/auth/login` or `POST /api/auth/register`. \
								Include `Authorization: Bearer <accessToken>` on protected requests.

								**Public endpoints:** `POST /api/auth/register`, `POST /api/auth/login`, \
								and `POST /api/auth/refresh` (no JWT required). \
								`POST /api/auth/change-password` requires authentication.

								**Role-restricted endpoints:**
								- `/api/admin/**` — ADMIN role only (includes `/api/admin/analytics/**`)
								- `/api/user/**` — USER or ADMIN role

								**Pagination & sorting:** List and search endpoints accept Spring Data \
								query parameters `page`, `size`, and `sort` (e.g. `sort=title,asc`).
								""")
						.version("1.0"))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
								.name(BEARER_AUTH_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT access token returned by login or register")))
				.tags(List.of(
						new Tag().name("Authentication")
								.description("Registration, login, and token refresh (public)"),
						new Tag().name("Authors").description("Author CRUD operations (JWT required)"),
						new Tag().name("Books").description("Book CRUD and search (JWT required)"),
						new Tag().name("Members").description("Member CRUD and book borrowing (JWT required)"),
						new Tag().name("Loans").description("Loan history (ADMIN)"),
						new Tag().name("Files").description("Multipart file upload and download (JWT required)"),
						new Tag().name("Admin").description("Admin-only endpoints (ADMIN role)"),
						new Tag().name("Analytics").description("Borrowing analytics from Loan history (ADMIN role)"),
						new Tag().name("User").description("User profile endpoints (USER or ADMIN role)")
				));
	}
}
