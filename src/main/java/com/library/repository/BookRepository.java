package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

	List<Book> findByTitleContainingIgnoreCase(String title);

	List<Book> findByAuthor_NameContainingIgnoreCase(String authorName);

	List<Book> findByPublishedYearGreaterThan(Integer year);

	List<Book> findByPublishedYearBetween(Integer yearFrom, Integer yearTo);

	List<Book> findByMembersIsEmpty();

	/**
	 * Available books published after a given year.
	 * JPQL keeps collection emptiness + year filter explicit and readable.
	 */
	@Query("""
			SELECT b FROM Book b
			WHERE b.members IS EMPTY
			  AND b.publishedYear > :year
			""")
	List<Book> findAvailablePublishedAfter(@Param("year") Integer year);

	/**
	 * Optional combined filters for GET /api/books/search.
	 * Null parameters are ignored (caller should normalize blank strings to null).
	 */
	@Query("""
			SELECT b FROM Book b
			JOIN b.author a
			WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
			  AND (:author IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :author, '%')))
			  AND (:publishedAfter IS NULL OR b.publishedYear > :publishedAfter)
			  AND (:yearFrom IS NULL OR b.publishedYear >= :yearFrom)
			  AND (:yearTo IS NULL OR b.publishedYear <= :yearTo)
			  AND (
			        :available IS NULL
			     OR (:available = TRUE AND SIZE(b.members) = 0)
			     OR (:available = FALSE AND SIZE(b.members) > 0)
			  )
			""")
	Page<Book> search(
			@Param("title") String title,
			@Param("author") String author,
			@Param("publishedAfter") Integer publishedAfter,
			@Param("yearFrom") Integer yearFrom,
			@Param("yearTo") Integer yearTo,
			@Param("available") Boolean available,
			Pageable pageable
	);
}
