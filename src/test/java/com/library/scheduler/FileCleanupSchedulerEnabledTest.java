package com.library.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
		"app.file.cleanup.enabled=true"
})
class FileCleanupSchedulerEnabledTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${app.file.cleanup.cron}")
	private String cleanupCron;

	@Test
	void cleanupEnabled_registersSchedulerBean() {
		assertNotNull(applicationContext.getBean(FileCleanupScheduler.class));
	}

	@Test
	void defaultCronIsDailyAtThreeAmWhenNotOverridden() {
		assertEquals("0 0 3 * * *", cleanupCron);
		assertTrue(applicationContext.containsBean("fileCleanupScheduler"));
	}
}
