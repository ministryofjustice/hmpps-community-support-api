-- V07: Add session-not-happen and no-attendance fields to appointment_ics_feedback

ALTER TABLE appointment_ics_feedback
    ADD COLUMN IF NOT EXISTS record_session_did_person_attend            BOOLEAN,
    ADD COLUMN IF NOT EXISTS record_session_not_happen_reason            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS record_session_not_happen_reason_details    TEXT,
    ADD COLUMN IF NOT EXISTS record_session_no_attendance_information    TEXT;

COMMENT ON COLUMN appointment_ics_feedback.record_session_did_person_attend IS 'Whether the person came to the appointment (only relevant when didSessionHappen = false)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_not_happen_reason IS 'Reason the session did not happen when the person attended: SERVICE_PROVIDER_ISSUE, REFERRAL_COULD_NOT_TAKE_PART, or REFERRAL_DID_NOT_COMPLY';
COMMENT ON COLUMN appointment_ics_feedback.record_session_not_happen_reason_details IS 'Free-text details for why the session did not happen when the person attended';
COMMENT ON COLUMN appointment_ics_feedback.record_session_no_attendance_information IS 'How the practitioner tried to contact the person and what they know about why the person did not attend';

