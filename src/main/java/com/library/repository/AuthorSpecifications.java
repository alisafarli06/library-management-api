package com.library.repository;

import com.library.entity.Author;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AuthorSpecifications {

	private AuthorSpecifications() {
	}

	/**
	 * Case-insensitive match against author name.
	 */
	public static Specification<Author> nameContains(String q) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(q)) {
				return null;
			}
			String pattern = "%" + q.trim().toLowerCase() + "%";
			return builder.like(builder.lower(root.get("name")), pattern);
		};
	}
}
