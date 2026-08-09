package com.library.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String BOOKS_CACHE = "books";

	@Bean
	public CacheManager cacheManager(
			@Value("${app.cache.books.maximum-size:500}") long maximumSize,
			@Value("${app.cache.books.expire-after-write-minutes:10}") long expireAfterWriteMinutes) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.maximumSize(maximumSize)
				.expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES));
		cacheManager.setCacheNames(List.of(BOOKS_CACHE));
		return cacheManager;
	}
}
