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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
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
		reset(bookRepository);

		if (book != null && book.getId() != null && bookRepository.existsById(book.getId())) {
			bookRepository.deleteById(book.getId());
		}
		if (author != null && author.getId() != null && authorRepository.existsById(author.getId())) {
			authorRepository.deleteById(author.getId());
		}
	}

	@Test
	void bookService_isSpringAopProxy() {
		assertTrue(AopUtils.isAopProxy(bookService));
	}

	@Test
	void findById_secondCallDoesNotHitRepository() {
		BookDto first = bookService.findById(book.getId());
		assertNotNull(booksCache().get(book.getId()));

		BookDto second = bookService.findById(book.getId());

		assertEquals(first.getId(), second.getId());
		assertEquals(first.getTitle(), second.getTitle());
		verify(bookRepository, times(1)).findById(book.getId());
	}

	@Test
	void findById_afterUpdate_reloadsFromRepository() {
		BookDto cached = bookService.findById(book.getId());
		assertEquals("Cached Book", cached.getTitle());
		assertNotNull(booksCache().get(book.getId()));

		BookDto updateRequest = new BookDto();
		updateRequest.setTitle("Updated Cached Book");
		updateRequest.setIsbn(book.getIsbn());
		updateRequest.setPublishedYear(2025);
		updateRequest.setAuthorId(author.getId());
		bookService.update(book.getId(), updateRequest);

		assertNull(booksCache().get(book.getId()), "Successful update must evict the books cache entry");
		clearInvocations(bookRepository);

		BookDto afterUpdate = bookService.findById(book.getId());

		assertEquals("Updated Cached Book", afterUpdate.getTitle());
		assertEquals(2025, afterUpdate.getPublishedYear());
		assertNotEquals(cached.getTitle(), afterUpdate.getTitle());
		verify(bookRepository, times(1)).findById(book.getId());
		assertNotNull(booksCache().get(book.getId()));
	}

	@Test
	void findById_afterDelete_throwsAndDoesNotServeStaleCache() {
		Long bookId = book.getId();
		bookService.findById(bookId);
		assertNotNull(booksCache().get(bookId));

		bookService.delete(bookId);
		assertNull(booksCache().get(bookId), "Successful delete must evict the books cache entry");
		clearInvocations(bookRepository);

		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(bookId));
		verify(bookRepository, times(1)).findById(bookId);
		book = null;
	}

	@Test
	void failedUpdate_doesNotEvictExistingCacheEntry() {
		BookDto cached = bookService.findById(book.getId());
		assertNotNull(booksCache().get(book.getId()));

		BookDto updateRequest = new BookDto();
		updateRequest.setTitle("Should Not Persist");
		updateRequest.setIsbn(book.getIsbn());
		updateRequest.setPublishedYear(2099);
		updateRequest.setAuthorId(9_999_999L);

		assertThrows(ResourceNotFoundException.class, () -> bookService.update(book.getId(), updateRequest));
		assertNotNull(booksCache().get(book.getId()), "Failed update must not evict the cache entry");
		clearInvocations(bookRepository);

		BookDto fromCache = bookService.findById(book.getId());
		assertEquals(cached.getTitle(), fromCache.getTitle());
		assertEquals(cached.getPublishedYear(), fromCache.getPublishedYear());
		verify(bookRepository, times(0)).findById(book.getId());
	}

	@Test
	void failedDelete_doesNotEvictExistingCacheEntry() {
		BookDto cached = bookService.findById(book.getId());
		assertNotNull(booksCache().get(book.getId()));

		doThrow(new RuntimeException("Forced delete failure"))
				.when(bookRepository).delete(org.mockito.ArgumentMatchers.any(Book.class));

		assertThrows(RuntimeException.class, () -> bookService.delete(book.getId()));
		assertNotNull(booksCache().get(book.getId()), "Failed delete must not evict the cache entry");
		clearInvocations(bookRepository);

		BookDto fromCache = bookService.findById(book.getId());
		assertEquals(cached.getTitle(), fromCache.getTitle());
		verify(bookRepository, times(0)).findById(book.getId());
	}

	@Test
	void findById_whenBookDoesNotExist_throwsAndDoesNotCacheMiss() {
		Long missingId = 9_999_999L;

		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(missingId));
		assertThrows(ResourceNotFoundException.class, () -> bookService.findById(missingId));

		verify(bookRepository, times(2)).findById(missingId);
	}

	private Cache booksCache() {
		Cache cache = cacheManager.getCache(CacheConfig.BOOKS_CACHE);
		assertNotNull(cache);
		return cache;
	}

	private void clearBooksCache() {
		Cache cache = cacheManager.getCache(CacheConfig.BOOKS_CACHE);
		if (cache != null) {
			cache.clear();
		}
	}
}
