package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import com.library.dto.LoanDto;
import com.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Loan history (ADMIN)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class LoanController {

	private final LoanService loanService;

	public LoanController(LoanService loanService) {
		this.loanService = loanService;
	}

	@GetMapping
	@Operation(
			summary = "List all loans",
			description = "Returns paginated active and historical loans. "
					+ "Supports `page`, `size`, and `sort` (e.g. `sort=borrowedAt,desc`)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of loans",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = LoanDto.class)))
	public Page<LoanDto> getAll(
			@ParameterObject
			@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return loanService.findAll(pageable);
	}
}
