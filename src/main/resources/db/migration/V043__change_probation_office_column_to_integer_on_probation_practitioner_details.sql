-- Change the probation_practitioner_details.probation_office column from a free-text
-- office name to an INTEGER reference to the probation_office_id from the
-- probation offices reference data (loaded from CSV, not a database table).
ALTER TABLE probation_practitioner_details
	ALTER COLUMN probation_office TYPE INTEGER USING NULL;

COMMENT ON COLUMN probation_practitioner_details.probation_office IS 'Probation office id of the Probation Practitioner, referencing the probation offices reference data';
