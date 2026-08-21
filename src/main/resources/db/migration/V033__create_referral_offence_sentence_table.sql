CREATE TABLE IF NOT EXISTS referral_offence_sentence (
	id UUID PRIMARY KEY,
	referral_id UUID NOT NULL,
	person_id UUID NOT NULL,
	offence TEXT,
	offence_sub_category TEXT,
	outcome TEXT,
	sentence_end_date DATE,
	expected_release_date DATE,
	has_licence_conditions_or_exclusion_zones BOOLEAN,
	licence_conditions_or_exclusion_zones_details TEXT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITH TIME ZONE,
	created_by UUID NOT NULL,
	updated_by UUID,

	CONSTRAINT fk_referral_offence_sentence_referral
	  FOREIGN KEY (referral_id)
		REFERENCES referral(id)
		ON DELETE CASCADE,

	CONSTRAINT fk_referral_offence_sentence_person
	  FOREIGN KEY (person_id)
		REFERENCES person(id)
		ON DELETE CASCADE,

	CONSTRAINT fk_referral_offence_sentence_created_by
	  FOREIGN KEY (created_by)
		REFERENCES referral_user(id),

	CONSTRAINT fk_referral_offence_sentence_updated_by
	  FOREIGN KEY (updated_by)
		REFERENCES referral_user(id),

	CONSTRAINT uk_referral_offence_sentence_referral UNIQUE (referral_id)
);

CREATE INDEX idx_referral_offence_sentence_referral_id ON referral_offence_sentence(referral_id);

COMMENT ON TABLE referral_offence_sentence IS 'Offence and sentence details captured against a referral';
COMMENT ON COLUMN referral_offence_sentence.referral_id IS 'Foreign key reference to the referral';
COMMENT ON COLUMN referral_offence_sentence.person_id IS 'Foreign key reference to the person linked to the referral';
COMMENT ON COLUMN referral_offence_sentence.offence IS 'Offence details confirmed during referral flow';
COMMENT ON COLUMN referral_offence_sentence.offence_sub_category IS 'Offence sub-category confirmed during referral flow';
COMMENT ON COLUMN referral_offence_sentence.outcome IS 'Outcome details confirmed during referral flow';
COMMENT ON COLUMN referral_offence_sentence.sentence_end_date IS 'Sentence end date if available';
COMMENT ON COLUMN referral_offence_sentence.expected_release_date IS 'Expected release date if available';
COMMENT ON COLUMN referral_offence_sentence.has_licence_conditions_or_exclusion_zones IS 'Whether licence conditions or exclusion zones are in place';
COMMENT ON COLUMN referral_offence_sentence.licence_conditions_or_exclusion_zones_details IS 'Details of licence conditions or exclusion zones when present';
