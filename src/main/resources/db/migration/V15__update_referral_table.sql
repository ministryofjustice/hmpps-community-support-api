-- V14__update_referral_table.sql
-- 1. Add target service completion date columns


-- 1. Add new columns
ALTER TABLE referral
    ADD COLUMN IF NOT EXISTS target_service_completion_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS target_service_completion_date_reason TEXT;

COMMENT ON COLUMN referral.target_service_completion_date IS 'Date when the referral is expected to be completed by the service';
COMMENT ON COLUMN referral.target_service_completion_date_reason IS 'Reason for the target service completion date';