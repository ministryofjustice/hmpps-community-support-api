ALTER TABLE appointment_history
    ADD COLUMN meet_at_the_gate BOOLEAN;

COMMENT ON COLUMN appointment_history.meet_at_the_gate IS 'Whether this is a Meet at the Gate session – only applicable to POST_RELEASE_SESSION appointments';
COMMENT ON COLUMN appointment_delivery.method IS 'Delivery method: PHONE_CALL, VIDEO_CALL, IN_PERSON, IN_PERSON_PROBATION_OFFICE, IN_PERSON_OTHER_LOCATION';
