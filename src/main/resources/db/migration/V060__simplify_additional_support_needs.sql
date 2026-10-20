-- V060: Simplify additional support needs - drop identified flag and rename details to additional_support_needs

ALTER TABLE appointment_ics_feedback
    DROP COLUMN IF EXISTS additional_support_needs_identified;

ALTER TABLE appointment_ics_feedback
    RENAME COLUMN additional_support_needs_details TO additional_support_needs;

COMMENT ON COLUMN appointment_ics_feedback.additional_support_needs IS 'Additional support needs identified for the person during the session';
