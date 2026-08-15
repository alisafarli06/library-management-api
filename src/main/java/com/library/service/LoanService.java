package com.library.service;

import com.library.dto.LoanDto;
import com.library.entity.Member;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.LoanMapper;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanService {

	private final LoanRepository loanRepository;
	private final UserRepository userRepository;
	private final MemberRepository memberRepository;
	private final LoanMapper loanMapper;

	public LoanService(
			LoanRepository loanRepository,
			UserRepository userRepository,
			MemberRepository memberRepository,
			LoanMapper loanMapper) {
		this.loanRepository = loanRepository;
		this.userRepository = userRepository;
		this.memberRepository = memberRepository;
		this.loanMapper = loanMapper;
	}

	public Page<LoanDto> findAll(Pageable pageable) {
		return loanRepository.findAll(pageable).map(loanMapper::toDto);
	}

	public Page<LoanDto> findForAuthenticatedUser(String email, Pageable pageable) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		Member member = memberRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Member not found for authenticated user"));
		return loanRepository.findByMember_Id(member.getId(), pageable).map(loanMapper::toDto);
	}
}
