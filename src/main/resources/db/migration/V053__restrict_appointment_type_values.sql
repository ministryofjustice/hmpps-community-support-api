-- V053: Restrict appointment.type to supported appointment categories

ALTER TABLE appointment
  ADD CONSTRAINT appointment_type_check
  CHECK (type IN (
    'ICS',
    'CONTACT_SESSION',
    'POST_RELEASE_SESSION',
    'PRE_RELEASE_SESSION',
    'HANDOVER_SESSION'
  ));

COMMENT ON COLUMN appointment.type IS 'Appointment type/category: ICS, CONTACT_SESSION, POST_RELEASE_SESSION, PRE_RELEASE_SESSION, HANDOVER_SESSION';
