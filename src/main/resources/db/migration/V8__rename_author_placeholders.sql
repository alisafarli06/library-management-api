-- Replace leftover integration-test author placeholders with demo-friendly names.
WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM authors
    WHERE name LIKE 'Attach Author %'
       OR name LIKE 'Borrow Author %'
       OR name = 'Borrow Author'
),
demo_names AS (
    SELECT *
    FROM (
        VALUES
            (1, 'Jane Austen'),
            (2, 'Leo Tolstoy'),
            (3, 'Chinua Achebe'),
            (4, 'Gabriel Garcia Marquez'),
            (5, 'Haruki Murakami'),
            (6, 'Toni Morrison'),
            (7, 'Kazuo Ishiguro'),
            (8, 'Margaret Atwood'),
            (9, 'Isabel Allende'),
            (10, 'Chimamanda Ngozi Adichie'),
            (11, 'Neil Gaiman'),
            (12, 'Octavia E. Butler'),
            (13, 'Ursula K. Le Guin'),
            (14, 'Salman Rushdie'),
            (15, 'Zadie Smith'),
            (16, 'Colson Whitehead'),
            (17, 'Elena Ferrante'),
            (18, 'Ngugi wa Thiong''o'),
            (19, 'Arundhati Roy'),
            (20, 'Jhumpa Lahiri')
    ) AS t(rn, name)
)
UPDATE authors a
SET name = d.name
FROM ranked r
JOIN demo_names d ON d.rn = r.rn
WHERE a.id = r.id;
