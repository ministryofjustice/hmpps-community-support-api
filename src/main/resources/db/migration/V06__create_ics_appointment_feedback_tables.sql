-- V06: Create appointment_ics_feedback table

CREATE TABLE IF NOT EXISTS appointment_ics_feedback (
    id                                              UUID        NOT NULL PRIMARY KEY,
    appointment_ics_id                              UUID        NOT NULL REFERENCES appointment_ics(id) ON DELETE CASCADE,

    -- Section 1: Record session attendance
    record_session_did_session_happen               BOOLEAN     NOT NULL,
    record_session_how_session_took_place           VARCHAR(255),
    record_session_not_in_person_reason             TEXT,
    record_session_pdu                              VARCHAR(255),
    record_session_address_line1                    VARCHAR(255),
    record_session_address_line2                    VARCHAR(255),
    record_session_town_or_city                     VARCHAR(255),
    record_session_county                           VARCHAR(255),
    record_session_postcode                         VARCHAR(20),

    -- Section 2: Session details
    session_details_was_person_late                 BOOLEAN,
    session_details_late_reason                     TEXT,
    session_details_duration                        VARCHAR(100),

    -- Section 3: Session feedback
    session_feedback_what_happened                  TEXT,
    session_feedback_behaviour                      TEXT,
    session_feedback_strengths_identified           TEXT,

    -- Section 4: Issues or concerns
    issues_concerns_identified                      TEXT,
    issues_concerns_notify_probation_practitioner   BOOLEAN,

    -- Section 5: Next steps
    next_steps_planned_for_next_session             TEXT,
    next_steps_actions_before_next_session          TEXT,

    created_at                                      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                                      UUID        REFERENCES referral_user(id)
);

COMMENT ON COLUMN appointment_ics_feedback.id IS 'Unique identifier for the ICS feedback record';
COMMENT ON COLUMN appointment_ics_feedback.appointment_ics_id IS 'Foreign key to appointment_ics.id – the ICS session this feedback relates to';

-- Record session attendance
COMMENT ON COLUMN appointment_ics_feedback.record_session_did_session_happen IS 'Whether the session actually took place';
COMMENT ON COLUMN appointment_ics_feedback.record_session_how_session_took_place IS 'How the session was delivered (e.g. Phone call, Video call, In person)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_not_in_person_reason IS 'Reason the session was not held in person, if applicable';
COMMENT ON COLUMN appointment_ics_feedback.record_session_pdu IS 'Probation Delivery Unit (PDU) where the session took place (only relevant for IN_PERSON_PROBATION_OFFICE)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_address_line1  IS 'First line of the address where the session took place (only relevant for IN_PERSON_OTHER_LOCATION)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_address_line2  IS 'Second line of the address (only relevant for IN_PERSON_OTHER_LOCATION)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_town_or_city   IS 'Town or city where the session took place (only relevant for IN_PERSON_OTHER_LOCATION)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_county         IS 'County where the session took place (only relevant for IN_PERSON_OTHER_LOCATION)';
COMMENT ON COLUMN appointment_ics_feedback.record_session_postcode       IS 'Postcode where the session took place (only relevant for IN_PERSON_OTHER_LOCATION)';


-- Session details
COMMENT ON COLUMN appointment_ics_feedback.session_details_was_person_late IS 'Whether the person of interest arrived late to the session';
COMMENT ON COLUMN appointment_ics_feedback.session_details_late_reason IS 'Explanation of why the person was late';
COMMENT ON COLUMN appointment_ics_feedback.session_details_duration IS 'Formatted duration of the session (e.g. "1 hour and 45 minutes", "30 minutes")';

-- Session feedback
COMMENT ON COLUMN appointment_ics_feedback.session_feedback_what_happened IS 'Narrative description of what happened during the session';
COMMENT ON COLUMN appointment_ics_feedback.session_feedback_behaviour IS 'Description of the person''s behaviour during the session';
COMMENT ON COLUMN appointment_ics_feedback.session_feedback_strengths_identified IS 'Strengths identified in the person during the session';

-- Issues or concerns
COMMENT ON COLUMN appointment_ics_feedback.issues_concerns_identified IS 'Any issues or concerns that arose during the session';
COMMENT ON COLUMN appointment_ics_feedback.issues_concerns_notify_probation_practitioner IS 'Whether the probation practitioner needs to be notified about identified issues';

-- Next steps
COMMENT ON COLUMN appointment_ics_feedback.next_steps_planned_for_next_session IS 'What is planned to happen in the next session';
COMMENT ON COLUMN appointment_ics_feedback.next_steps_actions_before_next_session IS 'Actions the person or practitioner should take before the next session';

-- Audit
COMMENT ON COLUMN appointment_ics_feedback.created_at IS 'Timestamp when the feedback was submitted';
COMMENT ON COLUMN appointment_ics_feedback.created_by IS 'User who submitted the feedback (referral_user.id)';



