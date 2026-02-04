-- V03: Create all referral-related tables

-- Create referral_user table (needed for foreign key references)
CREATE TABLE IF NOT EXISTS referral_user (
    id UUID NOT NULL PRIMARY KEY,
    hmpps_auth_id VARCHAR(255) NOT NULL,
    hmpps_auth_username TEXT NOT NULL,
    auth_source TEXT NOT NULL,
    last_synced_at TIMESTAMP
);

-- Comments for referral_user table columns
COMMENT ON COLUMN referral_user.id IS 'Unique identifier for the referral user';
COMMENT ON COLUMN referral_user.hmpps_auth_id IS 'HMPPS authentication identifier';
COMMENT ON COLUMN referral_user.hmpps_auth_username IS 'HMPPS authentication username';
COMMENT ON COLUMN referral_user.auth_source IS 'Source of authentication (e.g., auth, delius)';
COMMENT ON COLUMN referral_user.last_synced_at IS 'Timestamp when the user was last synced';

-- Create referral table
CREATE TABLE IF NOT EXISTS referral (
    id UUID NOT NULL PRIMARY KEY,
    person_id UUID NOT NULL,
    person_identifier TEXT NOT NULL,
    reference_number TEXT,
    urgency BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_referral_person FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE
);

-- Comments for referral table columns
COMMENT ON COLUMN referral.id IS 'Unique identifier for the referral';
COMMENT ON COLUMN referral.person_id IS 'Foreign key reference to the person table';
COMMENT ON COLUMN referral.person_identifier IS 'External identifier for the person (CRN or Prisoner Number)';
COMMENT ON COLUMN referral.reference_number IS 'Reference number for the referral';
COMMENT ON COLUMN referral.urgency IS 'Flag indicating if the referral is urgent';
COMMENT ON COLUMN referral.created_at IS 'Timestamp when the referral was created';
COMMENT ON COLUMN referral.updated_at IS 'Timestamp when the referral was last updated';

CREATE INDEX idx_referral_reference_number ON referral (reference_number);

-- Create referral_event table
CREATE TABLE IF NOT EXISTS referral_event (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    event_type TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_type TEXT NOT NULL,
    actor_id TEXT
);

-- Comments for referral_event table columns
COMMENT ON COLUMN referral_event.id IS 'Unique identifier for the referral event';
COMMENT ON COLUMN referral_event.referral_id IS 'Foreign key reference to the referral table';
COMMENT ON COLUMN referral_event.event_type IS 'Type of the event (e.g., SUBMITTED, ASSIGNED)';
COMMENT ON COLUMN referral_event.created_at IS 'Timestamp when the event was created';
COMMENT ON COLUMN referral_event.actor_type IS 'Type of the actor who triggered the event';
COMMENT ON COLUMN referral_event.actor_id IS 'Identifier of the actor who triggered the event';

-- Create referral_user_assignment table
CREATE TABLE IF NOT EXISTS referral_user_assignment (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    user_id UUID NOT NULL REFERENCES referral_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES referral_user(id),
    deleted_at TIMESTAMP,
    deleted_by UUID REFERENCES referral_user(id)
);

-- Comments for referral_user_assignment table columns
COMMENT ON COLUMN referral_user_assignment.id IS 'Unique identifier for the referral user assignment';
COMMENT ON COLUMN referral_user_assignment.referral_id IS 'Foreign key reference to the referral table';
COMMENT ON COLUMN referral_user_assignment.user_id IS 'Foreign key reference to the referral_user table (assigned user)';
COMMENT ON COLUMN referral_user_assignment.created_at IS 'Timestamp when the assignment was created';
COMMENT ON COLUMN referral_user_assignment.created_by IS 'Foreign key reference to the user who created the assignment';
COMMENT ON COLUMN referral_user_assignment.deleted_at IS 'Timestamp when the assignment was deleted (soft delete)';
COMMENT ON COLUMN referral_user_assignment.deleted_by IS 'Foreign key reference to the user who deleted the assignment';

-- Create referral_provider_assignment table
CREATE TABLE IF NOT EXISTS referral_provider_assignment (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id),
    community_service_provider_id UUID NOT NULL REFERENCES community_service_provider(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES referral_user(id)
);

-- Comments for referral_provider_assignment table columns
COMMENT ON COLUMN referral_provider_assignment.id IS 'Unique identifier for the referral provider assignment';
COMMENT ON COLUMN referral_provider_assignment.referral_id IS 'Foreign key reference to the referral table';
COMMENT ON COLUMN referral_provider_assignment.community_service_provider_id IS 'Foreign key reference to the community_service_provider table';
COMMENT ON COLUMN referral_provider_assignment.created_at IS 'Timestamp when the assignment was created';
COMMENT ON COLUMN referral_provider_assignment.created_by IS 'Foreign key reference to the user who created the assignment';

