package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.entity.User;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.MemberMapper;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private static final Logger log = LoggerFactory.getLogger(MemberService.class);

	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;
	private final UserRepository userRepository;
	private final LoanRepository loanRepository;
	private final MemberMapper memberMapper;

	public MemberService(
			MemberRepository memberRepository,
			BookRepository bookRepository,
			UserRepository userRepository,
			LoanRepository loanRepository,
			MemberMapper memberMapper) {
		this.memberRepository = memberRepository;
		this.bookRepository = bookRepository;
		this.userRepository = userRepository;
		this.loanRepository = loanRepository;
		this.memberMapper = memberMapper;
	}

	public Page<MemberDto> findAll(Pageable pageable) {
		return memberRepository.findAll(pageable).map(memberMapper::toDto);
	}

	public MemberDto findById(Long id) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
		return memberMapper.toDto(member);
	}

	@Transactional
	public MemberDto create(MemberDto memberDto) {
		Member member = memberMapper.toEntity(memberDto);
		member.setId(null);
		Member saved = memberRepository.save(member);
		return memberMapper.toDto(saved);
	}

	@Transactional
	public MemberDto update(Long id, MemberDto memberDto) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
		member.setName(memberDto.getName());
		member.setEmail(memberDto.getEmail());
		Member saved = memberRepository.save(member);
		return memberMapper.toDto(saved);
	}

	@Transactional
	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new ResourceNotFoundException("Member not found with id: " + id);
		}
		memberRepository.deleteById(id);
	}

	/**
	 * Borrows a book for a member in a single transaction.
	 * Creates a Loan history row, inserts the member–book relationship (member_books),
	 * and marks the book unavailable.
	 */
	@Transactional
	public void borrowBook(Long memberId, Long bookId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
		Book book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

		if (loanRepository.existsByMember_IdAndBook_IdAndReturnedAtIsNull(memberId, bookId)
				|| memberRepository.existsByIdAndBooks_Id(memberId, bookId)) {
			throw new ConflictException("Member already borrowed this book");
		}
		if (!book.isAvailable()
				|| !book.getMembers().isEmpty()
				|| loanRepository.existsByBook_IdAndReturnedAtIsNull(bookId)) {
			throw new ConflictException("Book is not available");
		}

		member.borrowBook(book);
		book.setAvailable(false);
		Loan loan = new Loan();
		loan.setMember(member);
		loan.setBook(book);
		loan.setBorrowedAt(Instant.now());
		memberRepository.save(member);
		bookRepository.save(book);
		try {
			loanRepository.saveAndFlush(loan);
		} catch (DataIntegrityViolationException ex) {
			throw new ConflictException("Member already borrowed this book");
		}
		log.info("Book {} borrowed by member {}", bookId, memberId);
	}

	@Transactional
	public void returnBook(Long memberId, Long bookId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
		Book book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
		Loan loan = loanRepository.findByMember_IdAndBook_IdAndReturnedAtIsNull(memberId, bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Active loan not found"));

		loan.setReturnedAt(Instant.now());
		member.returnBook(book);
		book.setAvailable(true);
		loanRepository.save(loan);
		memberRepository.save(member);
		bookRepository.save(book);
		log.info("Book {} returned by member {}", bookId, memberId);
	}

	@Transactional
	public void borrowBookForAuthenticatedUser(String email, Long bookId) {
		borrowBook(requireMemberIdForEmail(email), bookId);
	}

	@Transactional
	public void returnBookForAuthenticatedUser(String email, Long bookId) {
		returnBook(requireMemberIdForEmail(email), bookId);
	}

	@Transactional
	public Member ensureMemberForUser(User user) {
		return memberRepository.findByUser_Id(user.getId())
				.orElseGet(() -> linkOrCreateMember(user));
	}

	private Long requireMemberIdForEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		Member member = memberRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Member not found for authenticated user"));
		return member.getId();
	}

	private Member linkOrCreateMember(User user) {
		return memberRepository.findByEmail(user.getEmail())
				.map(existing -> {
					if (existing.getUser() != null && !user.getId().equals(existing.getUser().getId())) {
						throw new ConflictException("Email already registered: " + user.getEmail());
					}
					existing.setUser(user);
					return memberRepository.save(existing);
				})
				.orElseGet(() -> {
					Member member = new Member();
					member.setName(user.getFullName());
					member.setEmail(user.getEmail());
					member.setUser(user);
					return memberRepository.save(member);
				});
	}
}
