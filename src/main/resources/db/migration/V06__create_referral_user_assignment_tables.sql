CREATE TABLE IF NOT EXISTS app_user (
    id UUID NOT NULL PRIMARY KEY,
    hmpps_auth_id TEXT,
    hmpps_auth_username TEXT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email_address TEXT NOT NULL,
    user_type TEXT NOT NULL,
    last_synchronised_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_user_email_address UNIQUE (email_address)
);

CREATE INDEX idx_user_email_address ON app_user (email_address);

CREATE TABLE IF NOT EXISTS referral_user_assignment (
    referral_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    created_by TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_by TEXT,
    deleted_at TIMESTAMP,

    PRIMARY KEY (referral_id, user_id)
);

