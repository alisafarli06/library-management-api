package com.library.scheduler;

import com.library.service.FileCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.file.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileCleanupScheduler {

	private final FileCleanupService fileCleanupService;

	public FileCleanupScheduler(FileCleanupService fileCleanupService) {
		this.fileCleanupService = fileCleanupService;
	}

	@Scheduled(cron = "${app.file.cleanup.cron}")
	public void runCleanup() {
		fileCleanupService.cleanupOrphanedFiles();
	}
}
