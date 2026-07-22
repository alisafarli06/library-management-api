package com.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI libraryOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Library Management API")
						.description("REST API for managing authors, books, and members")
						.version("1.0"));
	}
}
