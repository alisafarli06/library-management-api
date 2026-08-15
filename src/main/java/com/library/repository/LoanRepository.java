package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	boolean existsByMember_IdAndBook_IdAndReturnedAtIsNull(Long memberId, Long bookId);

	boolean existsByBook_IdAndReturnedAtIsNull(Long bookId);

	Optional<Loan> findByMember_IdAndBook_IdAndReturnedAtIsNull(Long memberId, Long bookId);

	@Override
	@EntityGraph(attributePaths = { "member", "book" })
	Page<Loan> findAll(Pageable pageable);

	@EntityGraph(attributePaths = { "member", "book" })
	Page<Loan> findByMember_Id(Long memberId, Pageable pageable);
}
