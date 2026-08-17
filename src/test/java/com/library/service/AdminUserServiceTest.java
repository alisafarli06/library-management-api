package com.library.service;

import com.library.dto.AdminUserDto;
import com.library.entity.AccountStatus;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BadRequestException;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
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
class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private LoanRepository loanRepository;

	@InjectMocks
	private AdminUserService adminUserService;

	@Test
	void searchMapsUsersWithoutPassword() {
		User user = user(2L, "Ada Lovelace", "ada@library.com", Role.USER);
		when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<User>>any(), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(List.of(user)));

		Page<AdminUserDto> page = adminUserService.search("ada", null, null, PageRequest.of(0, 20));

		assertEquals(1, page.getContent().size());
		AdminUserDto dto = page.getContent().getFirst();
		assertEquals(2L, dto.getId());
		assertEquals("Ada Lovelace", dto.getFullName());
		assertEquals("ada@library.com", dto.getEmail());
		assertEquals(Role.USER, dto.getRole());
		assertEquals(AccountStatus.ACTIVE, dto.getStatus());
		assertNull(readPasswordIfPresent(dto));
	}

	@Test
	void updateRolePromotesUserToAdmin() {
		User user = user(2L, "Ada Lovelace", "ada@library.com", Role.USER);
		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		AdminUserDto dto = adminUserService.updateRole(2L, Role.ADMIN, "admin@library.com");

		assertEquals(Role.ADMIN, user.getRole());
		assertEquals(Role.ADMIN, dto.getRole());
	}

	@Test
	void updateRoleRejectsSelfDemotion() {
		User user = user(1L, "Ali Safarli", "alisafarli@gmail.com", Role.ADMIN);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> adminUserService.updateRole(1L, Role.USER, "alisafarli@gmail.com")
		);
		assertEquals("You cannot change your own role", ex.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateRoleDemotesAdminWhenAnotherAdminExists() {
		User user = user(3L, "Second Admin", "second@library.com", Role.ADMIN);
		when(userRepository.findById(3L)).thenReturn(Optional.of(user));
		when(userRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(2L);
		when(userRepository.save(user)).thenReturn(user);

		AdminUserDto dto = adminUserService.updateRole(3L, Role.USER, "admin@library.com");

		assertEquals(Role.USER, dto.getRole());
	}

	@Test
	void updateRoleRejectsDowngradingTheLastAdmin() {
		User user = user(1L, "Ali Safarli", "alisafarli@gmail.com", Role.ADMIN);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

		ConflictException ex = assertThrows(
				ConflictException.class,
				() -> adminUserService.updateRole(1L, Role.USER, "other-admin@library.com")
		);
		assertEquals(AdminUserService.LAST_ADMIN_MESSAGE, ex.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateStatusBlocksUser() {
		User user = user(2L, "Ada Lovelace", "ada@library.com", Role.USER);
		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		AdminUserDto dto = adminUserService.updateStatus(2L, AccountStatus.BLOCKED, "admin@library.com");

		assertEquals(AccountStatus.BLOCKED, dto.getStatus());
	}

	@Test
	void updateStatusRejectsSelfBlock() {
		User user = user(1L, "Ali Safarli", "alisafarli@gmail.com", Role.ADMIN);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> adminUserService.updateStatus(1L, AccountStatus.BLOCKED, "alisafarli@gmail.com")
		);
		assertEquals("You cannot block or unblock your own account", ex.getMessage());
	}

	@Test
	void updateStatusRejectsBlockingLastAdmin() {
		User user = user(1L, "Ali Safarli", "alisafarli@gmail.com", Role.ADMIN);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

		ConflictException ex = assertThrows(
				ConflictException.class,
				() -> adminUserService.updateStatus(1L, AccountStatus.BLOCKED, "other-admin@library.com")
		);
		assertEquals(AdminUserService.LAST_ADMIN_MESSAGE, ex.getMessage());
	}

	@Test
	void deleteRejectsOwnAccount() {
		User user = user(4L, "Self", "self@library.com", Role.USER);
		when(userRepository.findById(4L)).thenReturn(Optional.of(user));

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> adminUserService.delete(4L, "self@library.com")
		);
		assertEquals("You cannot delete your own account", ex.getMessage());
		verify(userRepository, never()).delete(any(User.class));
	}

	@Test
	void deleteRejectsLastAdmin() {
		User user = user(1L, "Ali Safarli", "alisafarli@gmail.com", Role.ADMIN);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

		assertThrows(ConflictException.class, () -> adminUserService.delete(1L, "other-admin@library.com"));
		verify(userRepository, never()).delete(any(User.class));
	}

	@Test
	void deleteRejectsUserWithLoanHistory() {
		User user = user(5L, "Borrower", "borrower@library.com", Role.USER);
		Member member = new Member();
		member.setId(9L);
		when(userRepository.findById(5L)).thenReturn(Optional.of(user));
		when(memberRepository.findByUser_Id(5L)).thenReturn(Optional.of(member));
		when(loanRepository.existsByMember_Id(9L)).thenReturn(true);

		ConflictException ex = assertThrows(
				ConflictException.class,
				() -> adminUserService.delete(5L, "admin@library.com")
		);
		assertEquals("User cannot be deleted because the linked member has borrow records", ex.getMessage());
		verify(userRepository, never()).delete(any(User.class));
	}

	@Test
	void findByIdThrowsWhenMissing() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> adminUserService.findById(99L));
	}

	private static User user(Long id, String name, String email, Role role) {
		User user = new User();
		user.setId(id);
		user.setFullName(name);
		user.setEmail(email);
		user.setPassword("secret-hash");
		user.setRole(role);
		user.setStatus(AccountStatus.ACTIVE);
		user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
		return user;
	}

	private static Object readPasswordIfPresent(AdminUserDto dto) {
		try {
			var field = dto.getClass().getDeclaredField("password");
			field.setAccessible(true);
			return field.get(dto);
		} catch (NoSuchFieldException ex) {
			return null;
		} catch (IllegalAccessException ex) {
			throw new AssertionError(ex);
		}
	}
}
