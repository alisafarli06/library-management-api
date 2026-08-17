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

	public static Specification<Author> hasBooks(Boolean hasBooks) {
		return (root, query, builder) -> {
			if (hasBooks == null) {
				return null;
			}
			if (hasBooks) {
				return builder.greaterThan(root.get("bookCount"), 0L);
			}
			return builder.or(
					builder.equal(root.get("bookCount"), 0L),
					builder.isNull(root.get("bookCount"))
			);
		};
	}
}
