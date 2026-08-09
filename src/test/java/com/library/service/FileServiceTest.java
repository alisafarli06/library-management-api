package com.library.service;

import com.library.config.FileStorageProperties;
import com.library.dto.FileMetadataDto;
import com.library.entity.FileMetadata;
import com.library.exception.BadRequestException;
import com.library.repository.FileMetadataRepository;
import com.library.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

	@Mock
	private FileMetadataRepository fileMetadataRepository;

	@Mock
	private FileStorageService fileStorageService;

	private FileService fileService;

	@BeforeEach
	void setUp() {
		FileStorageProperties properties = new FileStorageProperties();
		properties.setMaxSize(1024);
		properties.setAllowedContentTypes(List.of("image/jpeg", "image/png", "application/pdf"));
		fileService = new FileService(fileMetadataRepository, fileStorageService, properties);
	}

	@Test
	void upload_persistsMetadataWithGeneratedStorageName() {
		byte[] pdfBytes = "%PDF-1.4 content".getBytes();
		MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", pdfBytes);

		when(fileMetadataRepository.save(any(FileMetadata.class))).thenAnswer(invocation -> {
			FileMetadata metadata = invocation.getArgument(0);
			metadata.setId(42L);
			return metadata;
		});

		FileMetadataDto dto = fileService.upload(file);

		assertEquals(42L, dto.getId());
		assertEquals("report.pdf", dto.getOriginalFilename());
		assertEquals("application/pdf", dto.getContentType());
		assertEquals(pdfBytes.length, dto.getSize());

		ArgumentCaptor<FileMetadata> metadataCaptor = ArgumentCaptor.forClass(FileMetadata.class);
		verify(fileMetadataRepository).save(metadataCaptor.capture());
		FileMetadata saved = metadataCaptor.getValue();
		assertNotEquals("report.pdf", saved.getStoredFilename());
		assertTrue(saved.getStoredFilename().endsWith(".pdf"));
		verify(fileStorageService).store(eq(file), eq(saved.getStoredFilename()));
	}

	@Test
	void upload_rejectsEmptyFile() {
		MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

		assertThrows(BadRequestException.class, () -> fileService.upload(file));
		verify(fileStorageService, never()).store(any(), any());
		verify(fileMetadataRepository, never()).save(any());
	}

	@Test
	void upload_rejectsUnsupportedMimeType() {
		byte[] exeLike = new byte[] {0x4D, 0x5A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		MockMultipartFile file = new MockMultipartFile("file", "tool.exe", "application/octet-stream", exeLike);

		assertThrows(BadRequestException.class, () -> fileService.upload(file));
		verify(fileStorageService, never()).store(any(), any());
	}

	@Test
	void upload_rejectsOversizedFile() {
		byte[] large = new byte[2048];
		large[0] = 0x25;
		large[1] = 0x50;
		large[2] = 0x44;
		large[3] = 0x46;
		large[4] = 0x2D;
		MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", large);

		BadRequestException ex = assertThrows(BadRequestException.class, () -> fileService.upload(file));
		assertTrue(ex.getMessage().contains("maximum allowed size"));
		verify(fileStorageService, never()).store(any(), any());
	}

	@Test
	void upload_rejectsMismatchedExtensionAndContent() {
		byte[] jpegHeader = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00};
		MockMultipartFile file = new MockMultipartFile("file", "photo.pdf", "application/pdf", jpegHeader);

		assertThrows(BadRequestException.class, () -> fileService.upload(file));
		verify(fileStorageService, never()).store(any(), any());
	}

	@Test
	void upload_rejectsPathTraversalInOriginalFilename() {
		byte[] pdfBytes = "%PDF-1.4 content".getBytes();
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"../evil.pdf",
				"application/pdf",
				pdfBytes
		);

		assertThrows(BadRequestException.class, () -> fileService.upload(file));
		verify(fileStorageService, never()).store(any(), any());
	}
}
