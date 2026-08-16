-- Optional cover image and preface document per book.
-- Existing books remain valid with both columns null.

ALTER TABLE books
    ADD COLUMN IF NOT EXISTS cover_file_id BIGINT;

ALTER TABLE books
    ADD COLUMN IF NOT EXISTS preface_file_id BIGINT;

ALTER TABLE books
    DROP CONSTRAINT IF EXISTS fk_books_cover_file;

ALTER TABLE books
    ADD CONSTRAINT fk_books_cover_file
        FOREIGN KEY (cover_file_id) REFERENCES file_metadata (id) ON DELETE SET NULL;

ALTER TABLE books
    DROP CONSTRAINT IF EXISTS fk_books_preface_file;

ALTER TABLE books
    ADD CONSTRAINT fk_books_preface_file
        FOREIGN KEY (preface_file_id) REFERENCES file_metadata (id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_books_cover_file
    ON books (cover_file_id)
    WHERE cover_file_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_books_preface_file
    ON books (preface_file_id)
    WHERE preface_file_id IS NOT NULL;
