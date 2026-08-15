UPDATE members m
SET user_id = u.id
FROM users u
WHERE m.user_id IS NULL
  AND lower(m.email) = lower(u.email);

INSERT INTO members (name, email, user_id)
SELECT u.full_name, u.email, u.id
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM members m WHERE m.user_id = u.id)
  AND NOT EXISTS (SELECT 1 FROM members m WHERE lower(m.email) = lower(u.email));
