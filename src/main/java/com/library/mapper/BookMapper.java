package com.library.mapper;

import com.library.dto.BookDto;
import com.library.entity.Author;
import com.library.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

	public BookDto toDto(Book book) {
		BookDto dto = new BookDto();
		dto.setId(book.getId());
		dto.setTitle(book.getTitle());
		dto.setIsbn(book.getIsbn());
		dto.setPublishedYear(book.getPublishedYear());
		dto.setAuthorId(book.getAuthor().getId());
		dto.setAvailable(book.isAvailable());
		if (book.getCoverFile() != null) {
			dto.setCoverFileId(book.getCoverFile().getId());
			dto.setCoverFileName(book.getCoverFile().getOriginalFilename());
		}
		if (book.getPrefaceFile() != null) {
			dto.setPrefaceFileId(book.getPrefaceFile().getId());
			dto.setPrefaceFileName(book.getPrefaceFile().getOriginalFilename());
		}
		return dto;
	}

	public Book toEntity(BookDto dto, Author author) {
		Book book = new Book();
		book.setId(dto.getId());
		book.setTitle(dto.getTitle());
		book.setIsbn(dto.getIsbn());
		book.setPublishedYear(dto.getPublishedYear());
		book.setAuthor(author);
		return book;
	}
}
