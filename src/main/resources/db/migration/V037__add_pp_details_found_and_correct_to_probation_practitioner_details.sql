ALTER TABLE probation_practitioner_details
  ADD COLUMN pp_details_found_and_correct BOOLEAN;

COMMENT ON COLUMN probation_practitioner_details.pp_details_found_and_correct IS 'Whether the Probation Practitioner details found in nDelius were confirmed as correct';
