package com.library.service.storage;

import com.library.config.FileStorageProperties;
import com.library.exception.BadRequestException;
import com.library.exception.FileStorageException;
import com.library.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LocalFileStorageService implements FileStorageService {

	private final Path rootLocation;

	public LocalFileStorageService(FileStorageProperties properties) {
		this.rootLocation = Paths.get(properties.getStorageDirectory()).toAbsolutePath().normalize();
	}

	@PostConstruct
	void init() {
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException ex) {
			throw new FileStorageException("Could not initialize file storage directory", ex);
		}
	}

	@Override
	public void store(MultipartFile file, String storedFilename) {
		Path destination = resolveSafePath(storedFilename);
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new FileStorageException("Failed to store file", ex);
		}
	}

	@Override
	public Resource loadAsResource(String storedFilename) {
		try {
			Path file = resolveSafePath(storedFilename);
			Resource resource = new UrlResource(file.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ResourceNotFoundException("Stored file not found: " + storedFilename);
			}
			return resource;
		} catch (MalformedURLException ex) {
			throw new FileStorageException("Failed to read stored file", ex);
		}
	}

	@Override
	public void delete(String storedFilename) {
		try {
			Path file = resolveSafePath(storedFilename);
			Files.deleteIfExists(file);
		} catch (IOException ex) {
			throw new FileStorageException("Failed to delete stored file", ex);
		}
	}

	@Override
	public List<String> listRegularFilenames() {
		if (!Files.isDirectory(rootLocation)) {
			return List.of();
		}
		try (Stream<Path> entries = Files.list(rootLocation)) {
			return entries
					.filter(Files::isRegularFile)
					.map(path -> path.getFileName().toString())
					.toList();
		} catch (IOException ex) {
			throw new FileStorageException("Failed to list stored files", ex);
		}
	}

	Path resolveSafePath(String storedFilename) {
		if (!StringUtils.hasText(storedFilename)) {
			throw new BadRequestException("Stored filename must not be blank");
		}
		if (storedFilename.contains("..")
				|| storedFilename.contains("/")
				|| storedFilename.contains("\\")
				|| Paths.get(storedFilename).isAbsolute()) {
			throw new BadRequestException("Invalid stored filename");
		}

		Path resolved = rootLocation.resolve(storedFilename).normalize();
		if (!resolved.startsWith(rootLocation)) {
			throw new BadRequestException("Invalid stored filename");
		}
		return resolved;
	}

	Path getRootLocation() {
		return rootLocation;
	}
}
