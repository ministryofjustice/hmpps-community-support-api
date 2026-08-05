CREATE TABLE referral_criminogenic_needs (
    id UUID PRIMARY KEY,
    referral_id UUID NOT NULL,
    has_accommodation_needs BOOLEAN,
    accommodation_details TEXT,
    has_employment_education_needs BOOLEAN,
    employment_education_details TEXT,
    has_financial_needs BOOLEAN,
    financial_details TEXT,
    has_personal_relationships_community_needs BOOLEAN,
    personal_relationships_community_details TEXT,
    has_drug_use_needs BOOLEAN,
    drug_use_details TEXT,
    has_alcohol_use_needs BOOLEAN,
    alcohol_use_details TEXT,
    has_health_wellbeing_needs BOOLEAN,
    health_wellbeing_details TEXT,
    has_thinking_behaviours_attitude_needs BOOLEAN,
    thinking_behaviours_attitude_details TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID NOT NULL,

    CONSTRAINT fk_referral_criminogenic_needs_referral
     FOREIGN KEY (referral_id)
         REFERENCES referral(id)
         ON DELETE CASCADE,

    CONSTRAINT fk_referral_criminogenic_needs_updated_by
     FOREIGN KEY (updated_by)
         REFERENCES referral_user(id),

    CONSTRAINT uk_referral_criminogenic_needs_referral UNIQUE (referral_id)
);

COMMENT ON TABLE referral_criminogenic_needs IS 'Criminogenic needs captured for the referee of a referral';
COMMENT ON COLUMN referral_criminogenic_needs.id IS 'Unique identifier for the criminogenic needs record';
COMMENT ON COLUMN referral_criminogenic_needs.referral_id IS 'Foreign key reference to the referral table';
COMMENT ON COLUMN referral_criminogenic_needs.has_accommodation_needs IS 'Indicator for accommodation needs';
COMMENT ON COLUMN referral_criminogenic_needs.accommodation_details IS 'Details for accommodation needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_employment_education_needs IS 'Indicator for employment and education needs';
COMMENT ON COLUMN referral_criminogenic_needs.employment_education_details IS 'Details for employment and education needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_financial_needs IS 'Indicator for financial needs';
COMMENT ON COLUMN referral_criminogenic_needs.financial_details IS 'Details for financial needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_personal_relationships_community_needs IS 'Indicator for personal relationships and community needs';
COMMENT ON COLUMN referral_criminogenic_needs.personal_relationships_community_details IS 'Details for personal relationships and community needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_drug_use_needs IS 'Indicator for drug use needs';
COMMENT ON COLUMN referral_criminogenic_needs.drug_use_details IS 'Details for drug use needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_alcohol_use_needs IS 'Indicator for alcohol use needs';
COMMENT ON COLUMN referral_criminogenic_needs.alcohol_use_details IS 'Details for alcohol use needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_health_wellbeing_needs IS 'Indicator for health and wellbeing needs';
COMMENT ON COLUMN referral_criminogenic_needs.health_wellbeing_details IS 'Details for health and wellbeing needs';
COMMENT ON COLUMN referral_criminogenic_needs.has_thinking_behaviours_attitude_needs IS 'Indicator for thinking, behaviours and attitude needs';
COMMENT ON COLUMN referral_criminogenic_needs.thinking_behaviours_attitude_details IS 'Details for thinking, behaviours and attitude needs';
COMMENT ON COLUMN referral_criminogenic_needs.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN referral_criminogenic_needs.updated_by IS 'Foreign key reference to the user who last updated the record';
