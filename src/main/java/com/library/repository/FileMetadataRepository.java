package com.library.repository;

import com.library.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

	@Query("select f.storedFilename from FileMetadata f")
	List<String> findAllStoredFilenames();
}
