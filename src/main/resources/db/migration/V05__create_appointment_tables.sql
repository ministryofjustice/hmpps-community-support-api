-- V05: Create appointment-related tables

-- Create appointment table
CREATE TABLE IF NOT EXISTS appointment (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL REFERENCES referral(id) ON DELETE CASCADE,
    type VARCHAR(255) NOT NULL
);
COMMENT ON COLUMN appointment.id IS 'Unique identifier for the appointment';
COMMENT ON COLUMN appointment.referral_id IS 'Foreign key to referral.id';
COMMENT ON COLUMN appointment.type IS 'Appointment type/category to indicate if it is ICS or Delivery Session';

-- Create appointment_ics table
CREATE TABLE IF NOT EXISTS appointment_ics (
    id UUID NOT NULL PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointment(id) ON DELETE CASCADE,
    method VARCHAR(255) NOT NULL,
    method_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date TIMESTAMP NOT NULL,
    created_by UUID REFERENCES referral_user(id),
    session_communication TEXT[]
);
COMMENT ON COLUMN appointment_ics.id IS 'Unique identifier for the ICS record';
COMMENT ON COLUMN appointment_ics.appointment_id IS 'Foreign key to appointment.id';
COMMENT ON COLUMN appointment_ics.method IS 'How the ICS was delivered (e.g. Phone call, video call)';
COMMENT ON COLUMN appointment_ics.method_details IS 'Based on the method, additional details about the delivery';
COMMENT ON COLUMN appointment_ics.created_at IS 'Timestamp when ICS was created';
COMMENT ON COLUMN appointment_ics.start_date IS 'Appointment start datetime';
COMMENT ON COLUMN appointment_ics.created_by IS 'User who created the ICS (referral_user.id)';
COMMENT ON COLUMN appointment_ics.session_communication IS 'List of communication methods used during the session (e.g. Email, SMS, Letter)';

-- Create appointment_status_history table
CREATE TABLE IF NOT EXISTS appointment_status_history (
    appointment_id UUID NOT NULL REFERENCES appointment(id) ON DELETE CASCADE,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (appointment_id, created_at)
);
COMMENT ON COLUMN appointment_status_history.appointment_id IS 'Foreign key to appointment.id';
COMMENT ON COLUMN appointment_status_history.status IS 'Status of the appointment at the given time (e.g. SCHEDULED, RESCHEDULED, COMPLETED)';
COMMENT ON COLUMN appointment_status_history.created_at IS 'When the status was recorded';
