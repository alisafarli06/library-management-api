-- Account status for admin block/unblock. Existing users remain ACTIVE.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';
