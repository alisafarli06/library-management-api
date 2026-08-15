package com.library.mapper;

import com.library.dto.LoanDto;
import com.library.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

	public LoanDto toDto(Loan loan) {
		LoanDto dto = new LoanDto();
		dto.setId(loan.getId());
		dto.setMemberId(loan.getMember().getId());
		dto.setMemberName(loan.getMember().getName());
		dto.setBookId(loan.getBook().getId());
		dto.setBookTitle(loan.getBook().getTitle());
		dto.setBorrowedAt(loan.getBorrowedAt());
		dto.setReturnedAt(loan.getReturnedAt());
		return dto;
	}
}
