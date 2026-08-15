ALTER TABLE members
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE members
    DROP CONSTRAINT IF EXISTS uk_members_user_id;

ALTER TABLE members
    ADD CONSTRAINT uk_members_user_id UNIQUE (user_id);

ALTER TABLE members
    DROP CONSTRAINT IF EXISTS fk_members_user;

ALTER TABLE members
    ADD CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id);
