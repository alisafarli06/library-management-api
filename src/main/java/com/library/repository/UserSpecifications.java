package com.library.repository;

import com.library.entity.Role;
import com.library.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {

	private UserSpecifications() {
	}

	public static Specification<User> matchesQuery(String q) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(q)) {
				return null;
			}
			String pattern = "%" + q.trim().toLowerCase() + "%";
			return builder.or(
					builder.like(builder.lower(root.get("fullName")), pattern),
					builder.like(builder.lower(root.get("email")), pattern)
			);
		};
	}

	public static Specification<User> hasRole(Role role) {
		return (root, query, builder) -> {
			if (role == null) {
				return null;
			}
			return builder.equal(root.get("role"), role);
		};
	}
}
