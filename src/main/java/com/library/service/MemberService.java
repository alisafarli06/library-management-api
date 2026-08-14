package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.MemberMapper;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;
	private final MemberMapper memberMapper;

	public MemberService(
			MemberRepository memberRepository,
			BookRepository bookRepository,
			MemberMapper memberMapper) {
		this.memberRepository = memberRepository;
		this.bookRepository = bookRepository;
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
	 * Inserts the member–book relationship (member_books) and marks the book unavailable.
	 */
	@Transactional
	public void borrowBook(Long memberId, Long bookId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
		Book book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

		if (member.getBooks().contains(book)) {
			throw new ConflictException("Member already borrowed this book");
		}
		if (!book.isAvailable() || !book.getMembers().isEmpty()) {
			throw new ConflictException("Book is not available");
		}

		member.borrowBook(book);
		book.setAvailable(false);
		memberRepository.save(member);
		bookRepository.save(book);
	}
}
