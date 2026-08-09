package com.library.controller;

import com.library.dto.FileMetadataDto;
import com.library.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Files", description = "Secure multipart file upload and download")
public class FileController {

	private final FileService fileService;

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Upload file", description = "Uploads a file with type and size validation")
	public FileMetadataDto upload(@RequestPart("file") MultipartFile file) {
		return fileService.upload(file);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Download file", description = "Downloads a previously uploaded file by id")
	public ResponseEntity<Resource> download(@PathVariable Long id) {
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
