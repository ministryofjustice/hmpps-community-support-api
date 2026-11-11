ALTER TABLE appointment_delivery ADD COLUMN prison_code VARCHAR(255);

COMMENT ON COLUMN appointment_delivery.prison_code IS 'Prison agency code for IN_PERSON_PRISON deliveries';
COMMENT ON COLUMN appointment_delivery.method_details IS 'Free-text details or the selected prison name for appointment deliveries';
COMMENT ON COLUMN appointment_delivery.method IS 'Delivery method: PHONE_CALL, VIDEO_CALL, IN_PERSON, IN_PERSON_PROBATION_OFFICE, IN_PERSON_OTHER_LOCATION, IN_PERSON_PRISON';
