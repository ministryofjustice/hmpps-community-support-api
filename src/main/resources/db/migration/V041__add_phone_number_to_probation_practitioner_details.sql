ALTER TABLE probation_practitioner_details
  ADD COLUMN phone_number TEXT;

COMMENT ON COLUMN probation_practitioner_details.phone_number IS 'Phone number for the main point of contact for the Probation Practitioner';
