-- Rename table
ALTER TABLE appointment_ics RENAME TO appointment_ics_history;

-- Add new columns
ALTER TABLE appointment_ics_history
    ADD COLUMN change_requested_by VARCHAR(30),                 -- enum type
    ADD COLUMN change_reason TEXT;

CREATE INDEX idx_appointment_ics_history_appointment_created
    ON appointment_ics_history(appointment_id, created_at DESC);

-- Add constraints / comments
COMMENT ON COLUMN appointment_ics_history.change_requested_by IS 'Type of actor who requested the change';
COMMENT ON COLUMN appointment_ics_history.change_reason IS 'Reason for reschedule';