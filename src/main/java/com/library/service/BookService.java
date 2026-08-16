package com.library.service;

import com.library.config.CacheConfig;
import com.library.dto.BookDto;
import com.library.dto.BookSearchRequest;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.FileMetadata;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.BookSpecifications;
import com.library.repository.FileMetadataRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class BookService {

	private static final String COVER_JPEG = "image/jpeg";
	private static final String COVER_PNG = "image/png";
	private static final String PREFACE_PDF = "application/pdf";

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;
	private final FileMetadataRepository fileMetadataRepository;
	private final FileService fileService;
	private final BookMapper bookMapper;

	public BookService(
			BookRepository bookRepository,
			AuthorRepository authorRepository,
			FileMetadataRepository fileMetadataRepository,
			FileService fileService,
			BookMapper bookMapper) {
		this.bookRepository = bookRepository;
		this.authorRepository = authorRepository;
		this.fileMetadataRepository = fileMetadataRepository;
		this.fileService = fileService;
		this.bookMapper = bookMapper;
	}

	public Page<BookDto> findAll(Pageable pageable) {
		return bookRepository.findAll(pageable).map(bookMapper::toDto);
	}

	public Page<BookDto> search(BookSearchRequest request, Pageable pageable) {
		Specification<Book> specification = Specification
				.allOf(
						BookSpecifications.titleContains(request.getTitle()),
						BookSpecifications.authorNameContains(request.getAuthor()),
						BookSpecifications.publishedAfter(request.getPublishedAfter()),
						BookSpecifications.publishedYearFrom(request.getYearFrom()),
						BookSpecifications.publishedYearTo(request.getYearTo()),
						BookSpecifications.availability(request.getAvailable())
				);

		return bookRepository.findAll(specification, pageable).map(bookMapper::toDto);
	}

	@Cacheable(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
	public BookDto findById(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		return bookMapper.toDto(book);
	}

	@Transactional
	public BookDto create(BookDto bookDto) {
		Author author = findAuthor(bookDto.getAuthorId());
		Book book = bookMapper.toEntity(bookDto, author);
		book.setId(null);
		Book saved = bookRepository.save(book);
		return bookMapper.toDto(saved);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id", beforeInvocation = false)
	public BookDto update(Long id, BookDto bookDto) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		Author author = findAuthor(bookDto.getAuthorId());
		book.setTitle(bookDto.getTitle());
		book.setIsbn(bookDto.getIsbn());
		book.setPublishedYear(bookDto.getPublishedYear());
		book.setAuthor(author);
		Book saved = bookRepository.save(book);
		return bookMapper.toDto(saved);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id", beforeInvocation = false)
	public void delete(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		FileMetadata cover = book.getCoverFile();
		FileMetadata preface = book.getPrefaceFile();
		book.setCoverFile(null);
		book.setPrefaceFile(null);
		bookRepository.save(book);
		bookRepository.delete(book);
		if (cover != null) {
			fileService.delete(cover.getId());
		}
		if (preface != null) {
			fileService.delete(preface.getId());
		}
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#bookId", beforeInvocation = false)
	public BookDto attachCover(Long bookId, MultipartFile file) {
		return replaceAttachment(bookId, file, true);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#bookId", beforeInvocation = false)
	public BookDto attachPreface(Long bookId, MultipartFile file) {
		return replaceAttachment(bookId, file, false);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#bookId", beforeInvocation = false)
	public BookDto removeCover(Long bookId) {
		Book book = requireBook(bookId);
		FileMetadata previous = book.getCoverFile();
		book.setCoverFile(null);
		bookRepository.save(book);
		if (previous != null) {
			fileService.delete(previous.getId());
		}
		return bookMapper.toDto(book);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#bookId", beforeInvocation = false)
	public BookDto removePreface(Long bookId) {
		Book book = requireBook(bookId);
		FileMetadata previous = book.getPrefaceFile();
		book.setPrefaceFile(null);
		bookRepository.save(book);
		if (previous != null) {
			fileService.delete(previous.getId());
		}
		return bookMapper.toDto(book);
	}

	private BookDto replaceAttachment(Long bookId, MultipartFile file, boolean cover) {
		Book book = requireBook(bookId);
		FileMetadata previous = cover ? book.getCoverFile() : book.getPrefaceFile();
		var uploaded = fileService.upload(file);
		FileMetadata next = fileMetadataRepository.findById(uploaded.getId()).orElseThrow();
		try {
			if (cover) {
				assertCoverType(next);
				book.setCoverFile(next);
			} else {
				assertPrefaceType(next);
				book.setPrefaceFile(next);
			}
		} catch (BadRequestException ex) {
			fileService.delete(next.getId());
			throw ex;
		}
		bookRepository.save(book);
		if (previous != null && !previous.getId().equals(next.getId())) {
			fileService.delete(previous.getId());
		}
		return bookMapper.toDto(book);
	}

	private void assertCoverType(FileMetadata metadata) {
		String type = metadata.getContentType();
		if (!COVER_JPEG.equals(type) && !COVER_PNG.equals(type)) {
			throw new BadRequestException("Cover must be a JPEG or PNG image");
		}
	}

	private void assertPrefaceType(FileMetadata metadata) {
		if (!PREFACE_PDF.equals(metadata.getContentType())) {
			throw new BadRequestException("Preface must be a PDF document");
		}
	}

	private Book requireBook(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
	}

	private Author findAuthor(Long authorId) {
		return authorRepository.findById(authorId)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
	}
}
