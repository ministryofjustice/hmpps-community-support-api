-- V07: Add session-not-happen and no-attendance fields to appointment_ics_feedback

ALTER TABLE appointment_ics_feedback
    ADD COLUMN IF NOT EXISTS additional_support_needs_identified            BOOLEAN,
    ADD COLUMN IF NOT EXISTS additional_support_needs_details               TEXT;

COMMENT ON COLUMN appointment_ics_feedback.additional_support_needs_identified IS 'Whether any additional support needs were identified for the person during the session';
COMMENT ON COLUMN appointment_ics_feedback.additional_support_needs_details IS 'Details of any additional support needs identified for the person during the session';