-- Demo books were incorrectly attributed after placeholder renames (e.g. Harry Potter and
-- The Pragmatic Programmer both pointing at Leo Tolstoy), which made author analytics look wrong
-- even though the loan→book→author aggregation query is correct.
INSERT INTO authors (name)
SELECT 'J. K. Rowling'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE name = 'J. K. Rowling');

INSERT INTO authors (name)
SELECT 'Andrew Hunt'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE name = 'Andrew Hunt');

UPDATE books
SET author_id = (SELECT id FROM authors WHERE name = 'J. K. Rowling' ORDER BY id LIMIT 1)
WHERE title = 'Harry Potter';

UPDATE books
SET author_id = (SELECT id FROM authors WHERE name = 'Andrew Hunt' ORDER BY id LIMIT 1)
WHERE title = 'The Pragmatic Programmer';
