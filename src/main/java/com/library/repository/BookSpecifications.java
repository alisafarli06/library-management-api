package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class BookSpecifications {

	private BookSpecifications() {
	}

	public static Specification<Book> titleContains(String title) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(title)) {
				return null;
			}
			return builder.like(builder.lower(root.get("title")), "%" + title.trim().toLowerCase() + "%");
		};
	}

	public static Specification<Book> authorNameContains(String authorName) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(authorName)) {
				return null;
			}
			return builder.like(
					builder.lower(root.get("author").get("name")),
					"%" + authorName.trim().toLowerCase() + "%"
			);
		};
	}

	public static Specification<Book> publishedAfter(Integer year) {
		return (root, query, builder) -> {
			if (year == null) {
				return null;
			}
			return builder.greaterThan(root.get("publishedYear"), year);
		};
	}

	public static Specification<Book> publishedYearFrom(Integer yearFrom) {
		return (root, query, builder) -> {
			if (yearFrom == null) {
				return null;
			}
			return builder.greaterThanOrEqualTo(root.get("publishedYear"), yearFrom);
		};
	}

	public static Specification<Book> publishedYearTo(Integer yearTo) {
		return (root, query, builder) -> {
			if (yearTo == null) {
				return null;
			}
			return builder.lessThanOrEqualTo(root.get("publishedYear"), yearTo);
		};
	}

	public static Specification<Book> availability(Boolean available) {
		return (root, query, builder) -> {
			if (available == null) {
				return null;
			}
			if (Boolean.TRUE.equals(available)) {
				return builder.isEmpty(root.get("members"));
			}
			return builder.isNotEmpty(root.get("members"));
		};
	}
}
