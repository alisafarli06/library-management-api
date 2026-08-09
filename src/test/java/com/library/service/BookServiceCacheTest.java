package com.library.service;

import com.library.config.CacheConfig;
import com.library.dto.BookDto;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class BookServiceCacheTest {

	@Autowired
	private BookService bookService;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private CacheManager cacheManager;

	@MockitoSpyBean
	private BookRepository bookRepository;

	private Author author;
	private Book book;

	@BeforeEach
	void setUp() {
		clearBooksCache();

		author = new Author();
		author.setName("Cache Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		book = new Book();
		book.setTitle("Cached Book");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book = bookRepository.save(book);

		clearInvocations(bookRepository);
	}

	@AfterEach
	void tearDown() {
		clearBooksCache();

		if (book != null && book.getId() != null && bookRepository.existsById(book.getId())) {
			bookRepository.deleteById(book.getId());
		}
		if (author != null && author.getId() != null && authorRepository.existsById(author.getId())) {
			authorRepository.deleteById(author.getId());
		}
	}

	@Test
	void findById_secondCallDoesNotHitRepository() {
		BookDto first = bookService.findById(book.getId());
		assertNotNull(cacheManager.getCache(CacheConfig.BOOKS_CACHE).get(book.getId()));

		BookDto second = bookService.findById(book.getId());

		assertEquals(first.getId(), second.getId());
		assertEquals(first.getTitle(), second.getTitle());
		verify(bookRepository, times(1)).findById(book.getId());
	}

	@Test
	void findById_afterUpdate_reloadsFromRepository() {
		bookService.findById(book.getId());

		BookDto updateRequest = new BookDto();
		updateRequest.setTitle("Updated Cached Book");
		updateRequest.setIsbn(book.getIsbn());
		updateRequest.setPublishedYear(2025);
		updateRequest.setAuthorId(author.getId());
		bookService.update(book.getId(), updateRequest);
		clearInvocations(bookRepository);

		BookDto afterUpdate = bookService.findById(book.getId());

		assertEquals("Updated Cached Book", afterUpdate.getTitle());
		assertEquals(2025, afterUpdate.getPublishedYear());
		verify(bookRepository, times(1)).findById(book.getId());
	}

	@Test
	void findById_afterDelete_throwsAndDoesNotServeStaleCache() {
		Long bookId = book.getId();
		bookService.findById(bookId);
		assertNotNull(cacheManager.getCache(CacheConfig.BOOKS_CACHE).get(bookId));

		bookService.delete(bookId);
		assertNull(cacheManager.getCache(CacheConfig.BOOKS_CACHE).get(bookId));
		clearInvocations(bookRepository);

		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(bookId));
		verify(bookRepository, times(1)).findById(bookId);
		book = null;
	}

	@Test
	void findById_whenBookDoesNotExist_throwsAndDoesNotCacheMiss() {
		Long missingId = 9_999_999L;

		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(missingId));
		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(missingId));

		verify(bookRepository, times(2)).findById(missingId);
	}

	private void clearBooksCache() {
		Cache cache = cacheManager.getCache(CacheConfig.BOOKS_CACHE);
		if (cache != null) {
			cache.clear();
		}
	}
}
