-- V13__update_person_additional_support_needs.sql
-- 1. Add interpreter_needed boolean (nullable)
-- 2. Change no_additional_support_needed to be nullable with new semantics
-- 3. Rename no_additional_support_needed to additional_support_needed
-- 4. Patch existing data

-- 1. Add new column: interpreter_needed
ALTER TABLE person_additional_support_needs
    ADD COLUMN IF NOT EXISTS interpreter_needed BOOLEAN;

COMMENT ON COLUMN person_additional_support_needs.interpreter_needed IS
    'null = not answered, true = interpreter needed, false = no interpreter needed';

-- 2. Make no_additional_support_needed nullable and update default
ALTER TABLE person_additional_support_needs
    ALTER COLUMN no_additional_support_needed DROP NOT NULL,
    ALTER COLUMN no_additional_support_needed SET DEFAULT NULL;

-- 3. Rename the column
ALTER TABLE person_additional_support_needs
    RENAME COLUMN no_additional_support_needed TO additional_support_needed;

COMMENT ON COLUMN person_additional_support_needs.additional_support_needed IS
    'null = not answered, true = additional support needed, false = no additional support needed';

-- 4. Patch existing data
UPDATE person_additional_support_needs
SET interpreter_needed = CASE
    WHEN interpreter_language IS NOT NULL AND TRIM(interpreter_language) <> '' THEN TRUE
    ELSE FALSE
    END
WHERE interpreter_needed IS NULL;

UPDATE person_additional_support_needs
SET additional_support_needed = NOT additional_support_needed
WHERE additional_support_needed IS NOT NULL;