CREATE TABLE IF NOT EXISTS referral_event (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    event_type TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_type TEXT NOT NULL,
    actor_id TEXT
);

ALTER TABLE referral ADD COLUMN IF NOT EXISTS person_id UUID;
ALTER TABLE referral ADD COLUMN IF NOT EXISTS community_service_provider_id UUID;
ALTER TABLE referral ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE referral ADD COLUMN IF NOT EXISTS urgency BOOLEAN;

UPDATE referral
SET person_id = id
WHERE person_id IS NULL;

ALTER TABLE referral
    ADD CONSTRAINT fk_referral_person FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_referral_community_service_provider FOREIGN KEY (community_service_provider_id) REFERENCES community_service_provider (id) ON DELETE CASCADE;

CREATE INDEX ON referral (reference_number);

ALTER TABLE referral
    DROP COLUMN IF EXISTS first_name,
    DROP COLUMN IF EXISTS last_name,
    DROP COLUMN IF EXISTS sex,
    DROP COLUMN IF EXISTS date_of_birth,
    DROP COLUMN IF EXISTS ethnicity;