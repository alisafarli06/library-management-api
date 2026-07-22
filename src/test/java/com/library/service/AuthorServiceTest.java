package com.library.service;

import com.library.dto.AuthorDto;
import com.library.entity.Author;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.AuthorMapper;
import com.library.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

	@Mock
	private AuthorRepository authorRepository;

	@Spy
	private AuthorMapper authorMapper = new AuthorMapper();

	@InjectMocks
	private AuthorService authorService;

	@Test
	void findAll_returnsMappedAuthorDtos() {
		// Arrange
		Author author = createAuthor(1L, "Ali Safarli");
		Pageable pageable = PageRequest.of(0, 10);
		when(authorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(author)));

		// Act
		Page<AuthorDto> result = authorService.findAll(pageable);

		// Assert
		assertEquals(1, result.getTotalElements());
		assertEquals(1L, result.getContent().getFirst().getId());
		assertEquals("Ali Safarli", result.getContent().getFirst().getName());
		verify(authorRepository).findAll(pageable);
	}

	@Test
	void findById_whenAuthorExists_returnsAuthorDto() {
		// Arrange
		when(authorRepository.findById(1L)).thenReturn(Optional.of(createAuthor(1L, "Ali Safarli")));

		// Act
		AuthorDto result = authorService.findById(1L);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Ali Safarli", result.getName());
	}

	@Test
	void findById_whenAuthorDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(authorRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> authorService.findById(99L)
		);
		assertEquals("Author not found with id: 99", exception.getMessage());
	}

	@Test
	void create_savesAuthorAndReturnsDto() {
		// Arrange
		AuthorDto request = new AuthorDto();
		request.setName("Omar Ismayilov");

		when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
			Author input = invocation.getArgument(0);
			Author saved = new Author();
			saved.setId(2L);
			saved.setName(input.getName());
			return saved;
		});

		// Act
		AuthorDto result = authorService.create(request);

		// Assert
		assertEquals(2L, result.getId());
		assertEquals("Omar Ismayilov", result.getName());

		ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
		verify(authorRepository).save(captor.capture());
		assertNull(captor.getValue().getId());
		assertEquals("Omar Ismayilov", captor.getValue().getName());
	}

	@Test
	void update_whenAuthorExists_updatesAndReturnsDto() {
		// Arrange
		Author existing = createAuthor(1L, "Ali Safarli");
		AuthorDto request = new AuthorDto();
		request.setName("Omar Ismayilov");

		when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		AuthorDto result = authorService.update(1L, request);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Omar Ismayilov", result.getName());
		verify(authorRepository).save(existing);
	}

	@Test
	void update_whenAuthorDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		AuthorDto request = new AuthorDto();
		request.setName("Omar Ismayilov");
		when(authorRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> authorService.update(99L, request));
		verify(authorRepository, never()).save(any(Author.class));
	}

	@Test
	void delete_whenAuthorExists_deletesAuthor() {
		// Arrange
		when(authorRepository.existsById(1L)).thenReturn(true);

		// Act
		authorService.delete(1L);

		// Assert
		verify(authorRepository).deleteById(1L);
	}

	@Test
	void delete_whenAuthorDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(authorRepository.existsById(99L)).thenReturn(false);

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> authorService.delete(99L)
		);
		assertEquals("Author not found with id: 99", exception.getMessage());
		verify(authorRepository, never()).deleteById(any());
	}

	private Author createAuthor(Long id, String name) {
		Author author = new Author();
		author.setId(id);
		author.setName(name);
		return author;
	}
}
