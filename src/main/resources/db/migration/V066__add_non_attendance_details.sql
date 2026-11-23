ALTER TABLE appointment_ics_feedback
    ADD COLUMN IF NOT EXISTS record_session_how_they_tried_to_contact_the_person TEXT,
    ADD COLUMN IF NOT EXISTS record_session_reasons_they_did_not_attend TEXT,
    DROP COLUMN IF EXISTS record_session_no_attendance_information;

COMMENT ON COLUMN appointment_ics_feedback.record_session_how_they_tried_to_contact_the_person IS 'How and when the practitioner tried to contact the person and how many times';
COMMENT ON COLUMN appointment_ics_feedback.record_session_reasons_they_did_not_attend IS 'Reasons the person did not attend the session';
