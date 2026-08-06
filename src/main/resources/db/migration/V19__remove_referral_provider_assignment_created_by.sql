-- Remove redundant created_by column from referral_provider_assignment table
ALTER TABLE referral_provider_assignment DROP COLUMN IF EXISTS created_by;
