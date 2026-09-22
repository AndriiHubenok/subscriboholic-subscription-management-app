ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS email_verification_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(64) NOT NULL,
    user_id    BIGINT      NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_verification_token UNIQUE (token),
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_verification_token_user_id
    ON email_verification_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_verification_token_expires_at
    ON email_verification_tokens (expires_at);