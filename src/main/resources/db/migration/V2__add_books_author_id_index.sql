-- Index on books.author_id: every book query joins authors via this FK
-- (EntityGraph, list/search). PostgreSQL does not create indexes for FKs automatically.
CREATE INDEX IF NOT EXISTS idx_books_author_id ON books (author_id);
