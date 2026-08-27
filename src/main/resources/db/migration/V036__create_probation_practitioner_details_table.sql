CREATE TABLE IF NOT EXISTS probation_practitioner_details (
	id UUID PRIMARY KEY,
	referral_id UUID NOT NULL,
	name TEXT NOT NULL,
	job_role TEXT,
	email_address TEXT,
	pdu TEXT,
	probation_office TEXT,
	team_phone_number TEXT,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_by UUID NOT NULL,

	CONSTRAINT fk_probation_practitioner_details_referral
	  FOREIGN KEY (referral_id)
		REFERENCES referral(id)
		ON DELETE CASCADE,

	CONSTRAINT fk_probation_practitioner_details_updated_by
	  FOREIGN KEY (updated_by)
		REFERENCES referral_user(id),

	CONSTRAINT uk_probation_practitioner_details_referral UNIQUE (referral_id)
);

CREATE INDEX idx_probation_practitioner_details_referral_id ON probation_practitioner_details(referral_id);

COMMENT ON TABLE probation_practitioner_details IS 'Probation Practitioner details saved against a referral';
COMMENT ON COLUMN probation_practitioner_details.referral_id IS 'Foreign key reference to the referral';
COMMENT ON COLUMN probation_practitioner_details.name IS 'Full name of the Probation Practitioner';
COMMENT ON COLUMN probation_practitioner_details.job_role IS 'Job role of the Probation Practitioner';
COMMENT ON COLUMN probation_practitioner_details.email_address IS 'Email address of the Probation Practitioner';
COMMENT ON COLUMN probation_practitioner_details.pdu IS 'Probation Delivery Unit of the Probation Practitioner';
COMMENT ON COLUMN probation_practitioner_details.probation_office IS 'Probation office of the Probation Practitioner';
COMMENT ON COLUMN probation_practitioner_details.team_phone_number IS 'Team phone number for the Probation Practitioner';
