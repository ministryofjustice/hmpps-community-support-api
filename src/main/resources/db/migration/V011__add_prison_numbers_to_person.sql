-- V11: Add prison_numbers column to person table

ALTER TABLE person ADD COLUMN IF NOT EXISTS prison_numbers VARCHAR(500);

COMMENT ON COLUMN person.prison_numbers IS 'Comma-separated list of prison numbers associated with the person';
