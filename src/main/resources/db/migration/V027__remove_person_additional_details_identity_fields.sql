-- Remove deprecated identity fields from person_additional_details.
-- Use IF EXISTS so this migration is safe across environments.
ALTER TABLE person_additional_details
    DROP COLUMN IF EXISTS transgender,
    DROP COLUMN IF EXISTS sexual_orientation;
