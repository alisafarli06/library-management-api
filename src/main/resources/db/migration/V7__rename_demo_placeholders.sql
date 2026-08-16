-- Replace leftover integration-test placeholder titles/names with demo-friendly values.
UPDATE books
SET title = 'The Pragmatic Programmer'
WHERE title = 'Borrowable Book';

UPDATE books
SET title = 'Head First Design Patterns'
WHERE title = 'Attach Book';

UPDATE members
SET name = 'Casey Rivera'
WHERE name IN ('Borrower', 'Borrower User');
