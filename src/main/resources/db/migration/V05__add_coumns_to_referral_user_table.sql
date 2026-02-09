-- V05 add full_name and email_address to referral_user table

ALTER TABLE referral_user
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(400),
    ADD COLUMN IF NOT EXISTS email_address VARCHAR(255);

CREATE INDEX idx_referral_user_email ON referral_user(email_address);