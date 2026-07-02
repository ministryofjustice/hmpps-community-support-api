-- V12: Create referral additional support needs tables

-- create person additional support needs table
CREATE TABLE IF NOT EXISTS person_additional_support_needs (
    id UUID NOT NULL PRIMARY KEY,

    referral_id UUID NOT NULL,
    person_id UUID NOT NULL,

    -- Individual support need fields
    physical_health_details TEXT,
    mental_emotional_health_details TEXT,
    neurodiversity_details TEXT,
    location_travel_details TEXT,
    caring_responsibilities_details TEXT,
    employment_responsibilities_details TEXT,
    diversity_details TEXT,
    anything_else_details TEXT,
    -- flag to indicate referee does not need any additional support
    no_additional_support_needed BOOLEAN NOT NULL DEFAULT FALSE,

    interpreter_language TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL REFERENCES referral_user(id),
    updated_by UUID REFERENCES referral_user(id),

    CONSTRAINT fk_person_additional_support_needs_referral FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE,
    CONSTRAINT fk_person_additional_support_needs_person FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    CONSTRAINT uk_pasn_referral UNIQUE (referral_id)
);

-- Indexes
CREATE INDEX idx_person_support_needs_referral_id ON person_additional_support_needs(referral_id);
CREATE INDEX idx_person_support_needs_person_id ON person_additional_support_needs(person_id);

-- Comments
COMMENT ON TABLE person_additional_support_needs IS 'Additional support needs for the referee of a referral';
COMMENT ON COLUMN person_additional_support_needs.physical_health_details IS 'Details for Physical health support need';
COMMENT ON COLUMN person_additional_support_needs.mental_emotional_health_details IS 'Details for Mental or emotional health';
COMMENT ON COLUMN person_additional_support_needs.neurodiversity_details IS 'Details for Neurodiversity';
COMMENT ON COLUMN person_additional_support_needs.location_travel_details IS 'Details for Location and travel';
COMMENT ON COLUMN person_additional_support_needs.caring_responsibilities_details IS 'Details for Caring responsibilities';
COMMENT ON COLUMN person_additional_support_needs.employment_responsibilities_details IS 'Details for Employment responsibilities';
COMMENT ON COLUMN person_additional_support_needs.diversity_details IS 'Details for Diversity';
COMMENT ON COLUMN person_additional_support_needs.anything_else_details IS 'Free text for Anything else';
COMMENT ON COLUMN person_additional_support_needs.no_additional_support_needed IS 'User explicitly indicated no additional support needed';
COMMENT ON COLUMN person_additional_support_needs.interpreter_language IS 'Specific language for interpreter';

-- create referral and person support needs relationship table
-- CREATE TABLE IF NOT EXISTS referral_support_needs (
--     id UUID NOT NULL PRIMARY KEY,,
--     referral_id UUID NOT NULL,
--     person_support_needs_id UUID NOT NULL,
--
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     created_by UUID REFERENCES referral_user(id),
--
--     CONSTRAINT fk_rsn_referral FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE,
--     CONSTRAINT fk_rsn_support_needs FOREIGN KEY (person_support_needs_id) REFERENCES person_additional_support_needs(id) ON DELETE CASCADE,
--     CONSTRAINT uk_rsn_referral UNIQUE (referral_id)
-- );
--
-- -- Indexes
-- CREATE INDEX idx_rsn_referral_id ON referral_person_support_needs(referral_id);
-- CREATE INDEX idx_rsn_support_needs_id ON referral_person_support_needs(person_support_needs_id);