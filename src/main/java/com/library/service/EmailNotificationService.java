package com.library.service;

import com.library.config.AsyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Simulates outbound email notifications without blocking HTTP request threads.
 */
@Service
public class EmailNotificationService {

	private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

	private final long simulationDelayMs;
	private final boolean simulateFailure;
	private final AsyncNotificationTracker notificationTracker;

	public EmailNotificationService(
			@Value("${app.async.notification.delay-ms:500}") long simulationDelayMs,
			@Value("${app.async.notification.simulate-failure:false}") boolean simulateFailure,
			AsyncNotificationTracker notificationTracker) {
		this.simulationDelayMs = simulationDelayMs;
		this.simulateFailure = simulateFailure;
		this.notificationTracker = notificationTracker;
	}

	/**
	 * Simulates sending a welcome email. Runs on the notification executor;
	 * callers must invoke this through the Spring proxy (not via {@code this}).
	 */
	@Async(AsyncConfig.NOTIFICATION_EXECUTOR)
	public void sendWelcomeEmail(String toEmail, String fullName) {
		try {
			log.info("Simulating welcome email to {} ({})", toEmail, fullName);
			if (simulationDelayMs > 0) {
				Thread.sleep(simulationDelayMs);
			}
			if (simulateFailure) {
				throw new IllegalStateException("Simulated email provider failure");
			}
			notificationTracker.markWelcomeEmailSent(toEmail);
			log.info("Welcome email simulation completed for {}", toEmail);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			log.warn("Welcome email simulation interrupted for {}", toEmail);
		} catch (RuntimeException ex) {
			log.warn("Welcome email simulation failed for {}: {}", toEmail, ex.getMessage());
		}
	}
}
