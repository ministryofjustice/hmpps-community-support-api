-- V10: patch records of appointment status history with value of the status field from RESCHEDULED to CHANGED

-- update comment of the status field
COMMENT ON COLUMN appointment_status_history.status IS 'Status of the appointment at the given time (e.g. SCHEDULED, CHANGED, COMPLETED)';

-- patch existing records
UPDATE appointment_status_history SET status = 'CHANGED' WHERE status = 'RESCHEDULED';

