package com.library.service;

import com.library.config.FileStorageProperties;
import com.library.dto.FileMetadataDto;
import com.library.entity.FileMetadata;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.FileMetadataRepository;
import com.library.service.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

	private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
			"jpg", "image/jpeg",
			"jpeg", "image/jpeg",
			"png", "image/png",
			"pdf", "application/pdf"
	);

	private static final Set<String> ALLOWED_EXTENSIONS = EXTENSION_TO_CONTENT_TYPE.keySet();

	private final FileMetadataRepository fileMetadataRepository;
	private final FileStorageService fileStorageService;
	private final FileStorageProperties fileStorageProperties;

	public FileService(
			FileMetadataRepository fileMetadataRepository,
			FileStorageService fileStorageService,
			FileStorageProperties fileStorageProperties) {
		this.fileMetadataRepository = fileMetadataRepository;
		this.fileStorageService = fileStorageService;
		this.fileStorageProperties = fileStorageProperties;
	}

	@Transactional
	public FileMetadataDto upload(MultipartFile file) {
		validateFile(file);

		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
		if (!StringUtils.hasText(originalFilename) || originalFilename.contains("..")) {
			throw new BadRequestException("Invalid original filename");
		}

		String extension = extractExtension(originalFilename);
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new BadRequestException("Unsupported file extension: ." + extension);
		}

		String detectedContentType = detectContentType(file);
		String extensionContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
		if (!extensionContentType.equals(detectedContentType)) {
			throw new BadRequestException("File extension does not match file content type");
		}
		if (!fileStorageProperties.getAllowedContentTypes().contains(detectedContentType)) {
			throw new BadRequestException("Unsupported file type: " + detectedContentType);
		}

		String storedFilename = UUID.randomUUID() + "." + extension;
		fileStorageService.store(file, storedFilename);

		try {
			FileMetadata metadata = new FileMetadata();
			metadata.setOriginalFilename(originalFilename);
			metadata.setStoredFilename(storedFilename);
			metadata.setContentType(detectedContentType);
			metadata.setSize(file.getSize());
			metadata.setCreatedAt(Instant.now());
			FileMetadata saved = fileMetadataRepository.save(metadata);
			return toDto(saved);
		} catch (RuntimeException ex) {
			fileStorageService.delete(storedFilename);
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public FileDownload loadForDownload(Long id) {
		FileMetadata metadata = fileMetadataRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
		Resource resource = fileStorageService.loadAsResource(metadata.getStoredFilename());
		return new FileDownload(resource, metadata.getOriginalFilename(), metadata.getContentType(), metadata.getSize());
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("File must not be empty");
		}
		if (file.getSize() > fileStorageProperties.getMaxSize()) {
			throw new BadRequestException("File exceeds maximum allowed size of "
					+ fileStorageProperties.getMaxSize() + " bytes");
		}
	}

	private String extractExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot < 0 || lastDot == filename.length() - 1) {
			throw new BadRequestException("File must have an extension");
		}
		return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
	}

	private String detectContentType(MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			byte[] header = inputStream.readNBytes(8);
			if (header.length >= 3
					&& (header[0] & 0xFF) == 0xFF
					&& (header[1] & 0xFF) == 0xD8
					&& (header[2] & 0xFF) == 0xFF) {
				return "image/jpeg";
			}
			if (header.length >= 8
					&& header[0] == (byte) 0x89
					&& header[1] == 0x50
					&& header[2] == 0x4E
					&& header[3] == 0x47
					&& header[4] == 0x0D
					&& header[5] == 0x0A
					&& header[6] == 0x1A
					&& header[7] == 0x0A) {
				return "image/png";
			}
			if (header.length >= 5
					&& header[0] == 0x25
					&& header[1] == 0x50
					&& header[2] == 0x44
					&& header[3] == 0x46
					&& header[4] == 0x2D) {
				return "application/pdf";
			}
			throw new BadRequestException("Unsupported or unrecognized file content type");
		} catch (IOException ex) {
			throw new BadRequestException("Failed to read uploaded file content");
		}
	}

	private FileMetadataDto toDto(FileMetadata metadata) {
		FileMetadataDto dto = new FileMetadataDto();
		dto.setId(metadata.getId());
		dto.setOriginalFilename(metadata.getOriginalFilename());
		dto.setContentType(metadata.getContentType());
		dto.setSize(metadata.getSize());
		dto.setCreatedAt(metadata.getCreatedAt());
		return dto;
	}

	public record FileDownload(Resource resource, String originalFilename, String contentType, long size) {
	}
}
