package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

	/**
	 * Loads books with authors in one query to avoid N+1 when mapping authorId.
	 */
	@Override
	@EntityGraph(attributePaths = "author")
	Page<Book> findAll(Pageable pageable);

	/**
	 * Same fetch plan for dynamic search results that also map authorId.
	 */
	@EntityGraph(attributePaths = "author")
	Page<Book> findAll(Specification<Book> spec, Pageable pageable);

	List<Book> findByTitleContainingIgnoreCase(String title);

	List<Book> findByAuthor_NameContainingIgnoreCase(String authorName);

	List<Book> findByPublishedYearGreaterThan(Integer year);

	List<Book> findByPublishedYearBetween(Integer yearFrom, Integer yearTo);

	List<Book> findByMembersIsEmpty();

	boolean existsByIdAndMembers_Id(Long bookId, Long memberId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
	@Query("SELECT b FROM Book b WHERE b.id = :id")
	Optional<Book> findByIdForUpdate(@Param("id") Long id);

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
