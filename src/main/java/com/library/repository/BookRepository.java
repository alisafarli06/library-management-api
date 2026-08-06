package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

	List<Book> findByTitleContainingIgnoreCase(String title);

	List<Book> findByAuthor_NameContainingIgnoreCase(String authorName);

	List<Book> findByPublishedYearGreaterThan(Integer year);

	List<Book> findByPublishedYearBetween(Integer yearFrom, Integer yearTo);

	List<Book> findByMembersIsEmpty();

	boolean existsByIdAndMembers_Id(Long bookId, Long memberId);

	/**
	 * Available books published after a given year (Checkpoint 2 JPQL example).
	 */
	@Query("""
			SELECT b FROM Book b
			WHERE b.members IS EMPTY
			  AND b.publishedYear > :year
			""")
	List<Book> findAvailablePublishedAfter(@Param("year") Integer year);
}
