CREATE TABLE IF NOT EXISTS referral_event (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    event_type TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_type TEXT NOT NULL,
    actor_id TEXT
);

CREATE TABLE IF NOT EXISTS person (
                                      id UUID NOT NULL PRIMARY KEY,
                                      first_name TEXT,
                                      last_name TEXT,
                                      sex TEXT,
                                      date_of_birth DATE,
                                      ethnicity TEXT,
                                      preferred_language TEXT,
                                      disability TEXT,
                                      neurodiverse_conditions TEXT,
                                      religion_or_belief TEXT,
                                      gender_identity TEXT,
                                      transgender BOOLEAN,
                                      sexual_orientation TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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


ALTER TABLE referral
    DROP COLUMN IF EXISTS first_name,
    DROP COLUMN IF EXISTS last_name,
    DROP COLUMN IF EXISTS sex,
    DROP COLUMN IF EXISTS date_of_birth,
    DROP COLUMN IF EXISTS ethnicity;

CREATE TABLE IF NOT EXISTS person_address (
                                              id UUID NOT NULL PRIMARY KEY,
                                              person_id UUID NOT NULL,
                                              address TEXT,
                                              phone_number TEXT,
                                              email_address TEXT,
                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              CONSTRAINT fk_person_address_person FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE
);