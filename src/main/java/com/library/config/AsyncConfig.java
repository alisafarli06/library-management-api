package com.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	public static final String NOTIFICATION_EXECUTOR = "notificationTaskExecutor";

	private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

	@Bean(name = NOTIFICATION_EXECUTOR)
	public ThreadPoolTaskExecutor notificationTaskExecutor(
			@Value("${app.async.executor.core-pool-size:2}") int corePoolSize,
			@Value("${app.async.executor.max-pool-size:4}") int maxPoolSize,
			@Value("${app.async.executor.queue-capacity:100}") int queueCapacity) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("notification-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new LoggingAsyncExceptionHandler();
	}

	static final class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

		@Override
		public void handleUncaughtException(Throwable ex, Method method, Object... params) {
			log.error("Unhandled exception in async method '{}': {}", method.getName(), ex.getMessage(), ex);
		}
	}
}
