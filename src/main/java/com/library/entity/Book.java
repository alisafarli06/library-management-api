package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, unique = true)
	private String isbn;

	@Column(name = "published_year")
	private Integer publishedYear;

	/**
	 * Fast availability flag kept in sync with {@code member_books} and {@code loans}
	 * on the borrow/return path. Search still treats a book as available when
	 * {@code members} is empty. Borrow and return update the join row, the Loan record,
	 * and {@code available} in one transaction.
	 */
	@Column(nullable = false)
	@ColumnDefault("true")
	private boolean available = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Author author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cover_file_id")
	private FileMetadata coverFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "preface_file_id")
	private FileMetadata prefaceFile;

	@ManyToMany(mappedBy = "books", fetch = FetchType.LAZY)
	private Set<Member> members = new HashSet<>();

	public Book() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getPublishedYear() {
		return publishedYear;
	}

	public void setPublishedYear(Integer publishedYear) {
		this.publishedYear = publishedYear;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public FileMetadata getCoverFile() {
		return coverFile;
	}

	public void setCoverFile(FileMetadata coverFile) {
		this.coverFile = coverFile;
	}

	public FileMetadata getPrefaceFile() {
		return prefaceFile;
	}

	public void setPrefaceFile(FileMetadata prefaceFile) {
		this.prefaceFile = prefaceFile;
	}

	public Set<Member> getMembers() {
		return members;
	}

	public void setMembers(Set<Member> members) {
		this.members = members;
	}
}
