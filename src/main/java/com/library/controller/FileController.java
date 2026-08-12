package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.StandardAuthenticatedResponses;
import com.library.dto.FileMetadataDto;
import com.library.exception.ErrorResponse;
import com.library.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Secure multipart file upload and download (JWT required)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@StandardAuthenticatedResponses
public class FileController {

	private final FileService fileService;

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Upload file",
			description = "Uploads a file with extension, MIME type, and size validation. "
					+ "Send the file as multipart form field `file`."
	)
	@ApiResponse(
			responseCode = "201",
			description = "File uploaded; metadata returned (not the file bytes)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = FileMetadataDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Empty file, unsupported type, size exceeded, or invalid multipart request",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "500",
			description = "File storage failure",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public FileMetadataDto upload(
			@Parameter(
					description = "File to upload (allowed types and max size are configured server-side)",
					required = true,
					content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
			@RequestPart("file") MultipartFile file) {
		return fileService.upload(file);
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Download file",
			description = "Downloads a previously uploaded file by its metadata identifier."
	)
	@ApiResponse(
			responseCode = "200",
			description = "File stream with appropriate Content-Type and Content-Disposition headers",
			content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
	@ApiResponse(
			responseCode = "404",
			description = "File metadata or stored file not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "500",
			description = "File storage read failure",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Resource> download(
			@Parameter(description = "File metadata identifier", required = true, example = "1") @PathVariable Long id) {
		FileService.FileDownload download = fileService.loadForDownload(id);

		ContentDisposition contentDisposition = ContentDisposition.attachment()
				.filename(download.originalFilename(), StandardCharsets.UTF_8)
				.build();

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(download.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
				.contentLength(download.size())
				.body(download.resource());
	}
}
