ALTER TABLE referral
  ADD COLUMN IF NOT EXISTS service_days INTEGER;

COMMENT ON COLUMN referral.service_days IS 'The number of service days allocated for the referral';
