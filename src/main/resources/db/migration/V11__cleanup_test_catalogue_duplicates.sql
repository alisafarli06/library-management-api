-- Remove catalogue/member rows left behind by integration tests against a persistent database.
-- Legitimate demo rows are kept (earliest book per duplicated title; core demo accounts).

DELETE FROM loans
WHERE book_id IN (
    SELECT id
    FROM books
    WHERE title = 'Head First Design Patterns'
      AND id <> (
        SELECT MIN(id)
        FROM books
        WHERE title = 'Head First Design Patterns'
    )
)
   OR member_id IN (
    SELECT id
    FROM members
    WHERE name = 'Register Test User'
       OR email LIKE 'register-%@library.com'
);

DELETE FROM member_books
WHERE book_id IN (
    SELECT id
    FROM books
    WHERE title = 'Head First Design Patterns'
      AND id <> (
        SELECT MIN(id)
        FROM books
        WHERE title = 'Head First Design Patterns'
    )
)
   OR member_id IN (
    SELECT id
    FROM members
    WHERE name = 'Register Test User'
       OR email LIKE 'register-%@library.com'
);

DELETE FROM books
WHERE title = 'Head First Design Patterns'
  AND id <> (
    SELECT MIN(id)
    FROM books
    WHERE title = 'Head First Design Patterns'
);

DELETE FROM members
WHERE name = 'Register Test User'
   OR email LIKE 'register-%@library.com';

DELETE FROM users
WHERE email LIKE 'register-%@library.com';