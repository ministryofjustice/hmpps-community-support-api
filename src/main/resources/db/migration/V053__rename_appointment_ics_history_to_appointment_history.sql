ALTER TABLE IF EXISTS appointment_ics_history RENAME TO appointment_history;

ALTER INDEX IF EXISTS idx_appointment_ics_history_appointment_created
    RENAME TO idx_appointment_history_appointment_created;

COMMENT ON TABLE appointment IS 'Appointment header linking a referral to an appointment type; scheduling details are stored in appointment_history';
COMMENT ON TABLE appointment_history IS 'Scheduling details and change history for all appointment types; each record belongs to one appointment';
COMMENT ON COLUMN appointment_history.id IS 'Unique identifier for an appointment history record';
COMMENT ON COLUMN appointment_history.appointment_id IS 'Foreign key to the appointment header; multiple history records may belong to the same appointment';
COMMENT ON COLUMN appointment_history.appointment_delivery_id IS 'Foreign key to the delivery details for this appointment history record';
COMMENT ON COLUMN appointment_history.created_at IS 'Timestamp when this appointment history record was created';
COMMENT ON COLUMN appointment_history.start_date IS 'Scheduled appointment start datetime for this history record';
COMMENT ON COLUMN appointment_history.created_by IS 'User who created this appointment history record (referral_user.id)';
COMMENT ON COLUMN appointment_history.session_communication IS 'Communication methods recorded for this appointment history record (e.g. Email, SMS, Letter)';
COMMENT ON COLUMN appointment_history.change_requested_by IS 'Type of actor who requested the appointment change';
COMMENT ON COLUMN appointment_history.change_reason IS 'Reason for the appointment change';
COMMENT ON COLUMN appointment_ics_feedback.appointment_ics_id IS 'Foreign key to appointment_history.id for the ICS scheduling record receiving feedback';