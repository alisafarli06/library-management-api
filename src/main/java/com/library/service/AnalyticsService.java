package com.library.service;

import com.library.dto.AuthorBorrowAnalyticsDto;
import com.library.dto.BookBorrowAnalyticsDto;
import com.library.dto.LoanAnalyticsSummaryDto;
import com.library.dto.MemberBorrowAnalyticsDto;
import com.library.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

	private final LoanRepository loanRepository;

	public AnalyticsService(LoanRepository loanRepository) {
		this.loanRepository = loanRepository;
	}

	public LoanAnalyticsSummaryDto getSummary() {
		return new LoanAnalyticsSummaryDto(
				loanRepository.count(),
				loanRepository.countByReturnedAtIsNull(),
				loanRepository.countByReturnedAtIsNotNull(),
				loanRepository.countDistinctBooks(),
				loanRepository.countDistinctMembers());
	}

	public Page<BookBorrowAnalyticsDto> getMostBorrowedBooks(Pageable pageable) {
		return loanRepository.findMostBorrowedBooks(unsortedPage(pageable));
	}

	public Page<AuthorBorrowAnalyticsDto> getMostBorrowedAuthors(Pageable pageable) {
		return loanRepository.findMostBorrowedAuthors(unsortedPage(pageable));
	}

	public Page<MemberBorrowAnalyticsDto> getMostActiveMembers(Pageable pageable) {
		return loanRepository.findMostActiveMembers(unsortedPage(pageable));
	}

	private Pageable unsortedPage(Pageable pageable) {
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
	}
}
