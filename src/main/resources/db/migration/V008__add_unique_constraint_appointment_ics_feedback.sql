-- V08: Enforce one-to-one relationship between appointment_ics and appointment_ics_feedback.
-- A given ICS appointment can only have a single feedback record.

ALTER TABLE appointment_ics_feedback
    ADD CONSTRAINT uq_appointment_ics_feedback_ics_id UNIQUE (appointment_ics_id);

COMMENT ON CONSTRAINT uq_appointment_ics_feedback_ics_id ON appointment_ics_feedback
    IS 'Ensures at most one feedback record exists per ICS appointment';

