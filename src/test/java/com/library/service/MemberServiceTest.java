package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Test
	void findAll_returnsMappedMemberDtos() {
		// Arrange
		Member member = createMember(1L, "Ali Safarli", "ali.safarli@gmail.com");
		Pageable pageable = PageRequest.of(0, 10);
		when(memberRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(member)));

		// Act
		Page<MemberDto> result = memberService.findAll(pageable);

		// Assert
		assertEquals(1, result.getTotalElements());
		assertEquals(1L, result.getContent().getFirst().getId());
		assertEquals("Ali Safarli", result.getContent().getFirst().getName());
		assertEquals("ali.safarli@gmail.com", result.getContent().getFirst().getEmail());
		verify(memberRepository).findAll(pageable);
	}

	@Test
	void findById_whenMemberExists_returnsMemberDto() {
		// Arrange
		when(memberRepository.findById(1L))
				.thenReturn(Optional.of(createMember(1L, "Ali Safarli", "ali.safarli@gmail.com")));

		// Act
		MemberDto result = memberService.findById(1L);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Ali Safarli", result.getName());
		assertEquals("ali.safarli@gmail.com", result.getEmail());
	}

	@Test
	void findById_whenMemberDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(memberRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> memberService.findById(99L)
		);
		assertEquals("Member not found with id: 99", exception.getMessage());
	}

	@Test
	void create_savesMemberAndReturnsDto() {
		// Arrange
		MemberDto request = new MemberDto();
		request.setName("Omar Ismayilov");
		request.setEmail("omar.ismayilov@gmail.com");

		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member input = invocation.getArgument(0);
			Member saved = new Member();
			saved.setId(3L);
			saved.setName(input.getName());
			saved.setEmail(input.getEmail());
			return saved;
		});

		// Act
		MemberDto result = memberService.create(request);

		// Assert
		assertEquals(3L, result.getId());
		assertEquals("Omar Ismayilov", result.getName());
		assertEquals("omar.ismayilov@gmail.com", result.getEmail());

		ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(captor.capture());
		assertNull(captor.getValue().getId());
		assertEquals("Omar Ismayilov", captor.getValue().getName());
		assertEquals("omar.ismayilov@gmail.com", captor.getValue().getEmail());
	}

	@Test
	void update_whenMemberExists_updatesAndReturnsDto() {
		// Arrange
		Member existing = createMember(1L, "Ali Safarli", "ali.safarli@gmail.com");
		MemberDto request = new MemberDto();
		request.setName("Omar Ismayilov");
		request.setEmail("omar.ismayilov@gmail.com");

		when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		MemberDto result = memberService.update(1L, request);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Omar Ismayilov", result.getName());
		assertEquals("omar.ismayilov@gmail.com", result.getEmail());
		verify(memberRepository).save(existing);
	}

	@Test
	void update_whenMemberDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		MemberDto request = new MemberDto();
		request.setName("Omar Ismayilov");
		request.setEmail("omar.ismayilov@gmail.com");
		when(memberRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> memberService.update(99L, request));
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	void delete_whenMemberExists_deletesMember() {
		// Arrange
		when(memberRepository.existsById(1L)).thenReturn(true);

		// Act
		memberService.delete(1L);

		// Assert
		verify(memberRepository).deleteById(1L);
	}

	@Test
	void delete_whenMemberDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(memberRepository.existsById(99L)).thenReturn(false);

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> memberService.delete(99L)
		);
		assertEquals("Member not found with id: 99", exception.getMessage());
		verify(memberRepository, never()).deleteById(any());
	}

	private Member createMember(Long id, String name, String email) {
		Member member = new Member();
		member.setId(id);
		member.setName(name);
		member.setEmail(email);
		return member;
	}
}
