package com.library.repository;

import com.library.dto.AuthorBorrowAnalyticsDto;
import com.library.dto.BookBorrowAnalyticsDto;
import com.library.dto.MemberBorrowAnalyticsDto;
import com.library.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

	boolean existsByMember_IdAndBook_IdAndReturnedAtIsNull(Long memberId, Long bookId);

	boolean existsByBook_IdAndReturnedAtIsNull(Long bookId);

	long countByReturnedAtIsNull();

	long countByReturnedAtIsNotNull();

	Optional<Loan> findByMember_IdAndBook_IdAndReturnedAtIsNull(Long memberId, Long bookId);

	@Override
	@EntityGraph(attributePaths = { "member", "book" })
	Page<Loan> findAll(Pageable pageable);

	@Override
	@EntityGraph(attributePaths = { "member", "book" })
	Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "member", "book" })
	Page<Loan> findByMember_Id(Long memberId, Pageable pageable);

	@Query("SELECT COUNT(DISTINCT l.book.id) FROM Loan l")
	long countDistinctBooks();

	@Query("SELECT COUNT(DISTINCT l.member.id) FROM Loan l")
	long countDistinctMembers();

	@Query(
			value = """
					SELECT new com.library.dto.BookBorrowAnalyticsDto(b.id, b.title, COUNT(l.id))
					FROM Loan l
					JOIN l.book b
					GROUP BY b.id, b.title
					ORDER BY COUNT(l.id) DESC, b.id ASC
					""",
			countQuery = "SELECT COUNT(DISTINCT l.book.id) FROM Loan l")
	Page<BookBorrowAnalyticsDto> findMostBorrowedBooks(Pageable pageable);

	/**
	 * Aggregates loan rows through book → author (ManyToOne on {@code Book.author}).
	 * Counts are loan events, not distinct books.
	 */
	@Query(
			value = """
					SELECT new com.library.dto.AuthorBorrowAnalyticsDto(a.id, a.name, COUNT(l.id))
					FROM Loan l
					JOIN l.book b
					JOIN b.author a
					GROUP BY a.id, a.name
					ORDER BY COUNT(l.id) DESC, a.id ASC
					""",
			countQuery = "SELECT COUNT(DISTINCT b.author.id) FROM Loan l JOIN l.book b")
	Page<AuthorBorrowAnalyticsDto> findMostBorrowedAuthors(Pageable pageable);

	@Query(
			value = """
					SELECT new com.library.dto.MemberBorrowAnalyticsDto(m.id, m.name, COUNT(l.id))
					FROM Loan l
					JOIN l.member m
					GROUP BY m.id, m.name
					ORDER BY COUNT(l.id) DESC, m.id ASC
					""",
			countQuery = "SELECT COUNT(DISTINCT l.member.id) FROM Loan l")
	Page<MemberBorrowAnalyticsDto> findMostActiveMembers(Pageable pageable);
}
