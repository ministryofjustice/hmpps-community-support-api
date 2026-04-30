-- Local seed data for appointment-related tables
-- Appointments (one per referral, type = ICS)
INSERT INTO appointment (id, referral_id, type)
VALUES
    ('4a88fd16-76a9-4ded-9f87-f60a9748f641', '3f9d6a0e-1a2b-4c3d-8e9f-0123456789ab', 'ICS'),
    ('a7b108a8-1ce0-4a86-98d9-faa4847e6f1d', '8a1b2c3d-4e5f-6789-abcd-abcdef012345', 'ICS'),
    ('53272579-0983-42a9-88ce-820c79471bec', '11111111-2222-3333-4444-555555555555', 'ICS'),
    ('f2dbd949-2e39-48ef-a483-aaafda4a5608', 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'ICS'),
    ('61707704-3ea5-4707-b71a-22c276e7c4db', '123e4567-e89b-12d3-a456-426614174000', 'ICS'),
    ('b1e8a0cf-6b7f-4d3a-bef6-eb6822c59f2c', '6f1e2d3c-4b5a-6978-8c9d-0a1b2c3d4e5f', 'ICS'),
    ('5838a444-2ed5-4e70-850e-aada3f9eaaf3', '5bfb6628-8d7e-4eab-8f70-b27e166ea73c', 'ICS'),
    ('24b90534-830a-43f6-8f5a-e3eee268505a', '0827e8d7-0a38-4ac0-9c43-fe72922b18f5', 'ICS'),
    ('856b6c0f-7948-4352-8265-f7b59f6f7c68', 'c9f5b3ed-5bd9-487b-86c4-3acc64a4b557', 'ICS'),
    ('d18f169c-e215-46a7-848e-e2244c1faa41', 'aacd0c6f-d9fc-44e5-9204-bb78010ffc2e', 'ICS');

-- Appointment delivery records
-- c0000000-* UUIDs are used for appointment_delivery rows
INSERT INTO appointment_delivery (id, method, method_details, address_line1, address_line2, town_or_city, county, postcode)
VALUES
    -- Alice: Phone call
    (
        'c0000000-0000-4000-8000-000000000001',
        'PHONE_CALL',
        'Welfare check call with Alice',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Bob: Video call
    (
        'c0000000-0000-4000-8000-000000000002',
        'VIDEO_CALL',
        'Microsoft Teams session with Bob Jones',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Carlos: In person – probation office
    (
        'c0000000-0000-4000-8000-000000000003',
        'IN_PERSON_PROBATION_OFFICE',
        'Office visit at probation office, Room 3',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Dana: Phone call
    (
        'c0000000-0000-4000-8000-000000000004',
        'PHONE_CALL',
        'Inbound call from Dana Lee',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Evan: Video call
    (
        'c0000000-0000-4000-8000-000000000005',
        'VIDEO_CALL',
        'Zoom session with Evan Brown',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Fiona: In person – other location (address)
    (
        'c0000000-0000-4000-8000-000000000006',
        'IN_PERSON_OTHER_LOCATION',
        NULL,
        '56 Carlisle Road', NULL, 'London', NULL, 'N1 6XE'
    ),
    -- Luka: Phone call
    (
        'c0000000-0000-4000-8000-000000000007',
        'PHONE_CALL',
        'Scheduled welfare check call with Luka Cross',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Gloria: Phone call
    (
        'c0000000-0000-4000-8000-000000000008',
        'PHONE_CALL',
        'Inbound call from Gloria Green',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Harry: Video call
    (
        'c0000000-0000-4000-8000-000000000009',
        'VIDEO_CALL',
        'Zoom session with Harry Helton',
        NULL, NULL, NULL, NULL, NULL
    ),
    -- Imran: Phone call
    (
        'c0000000-0000-4000-8000-000000000010',
        'PHONE_CALL',
        'Inbound call from Imran Indigo',
        NULL, NULL, NULL, NULL, NULL
    );

-- Appointment ICS records
INSERT INTO appointment_ics (id, appointment_id, appointment_delivery_id, created_at, start_date, created_by, session_communication)
VALUES
    (
        'b0000000-0000-4000-8000-000000000001',
        '4a88fd16-76a9-4ded-9f87-f60a9748f641',
        'c0000000-0000-4000-8000-000000000001',
        '2026-01-10 09:00:00',
        '2026-02-01 10:00:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000002',
        'a7b108a8-1ce0-4a86-98d9-faa4847e6f1d',
        'c0000000-0000-4000-8000-000000000002',
        '2026-01-11 11:00:00',
        '2026-02-05 14:00:00',
        'a0000000-0000-4000-8000-000000000002',
        ARRAY['Email']
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        '53272579-0983-42a9-88ce-820c79471bec',
        'c0000000-0000-4000-8000-000000000003',
        '2026-01-12 08:30:00',
        '2026-02-10 09:30:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'f2dbd949-2e39-48ef-a483-aaafda4a5608',
        'c0000000-0000-4000-8000-000000000004',
        '2026-01-13 14:00:00',
        '2026-02-12 11:00:00',
        'a0000000-0000-4000-8000-000000000002',
        ARRAY['Email', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        '61707704-3ea5-4707-b71a-22c276e7c4db',
        'c0000000-0000-4000-8000-000000000005',
        '2026-01-14 10:00:00',
        '2026-02-14 15:00:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text', 'Letter']
    ),
    (
        'b0000000-0000-4000-8000-000000000006',
        'b1e8a0cf-6b7f-4d3a-bef6-eb6822c59f2c',
        'c0000000-0000-4000-8000-000000000006',
        '2026-01-15 09:00:00',
        '2026-02-18 10:30:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000007',
        '5838a444-2ed5-4e70-850e-aada3f9eaaf3',
        'c0000000-0000-4000-8000-000000000007',
        '2026-01-16 13:00:00',
        '2026-02-20 13:00:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Email', 'SMS']
    ),
    (
        'b0000000-0000-4000-8000-000000000008',
        '24b90534-830a-43f6-8f5a-e3eee268505a',
        'c0000000-0000-4000-8000-000000000008',
        '2026-01-14 09:30:00',
        '2026-02-12 10:30:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000009',
        '856b6c0f-7948-4352-8265-f7b59f6f7c68',
        'c0000000-0000-4000-8000-000000000009',
        '2026-01-15 10:30:00',
        '2026-02-12 11:30:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text', 'Letter']
    ),
    (
        'b0000000-0000-4000-8000-000000000010',
        'd18f169c-e215-46a7-848e-e2244c1faa41',
        'c0000000-0000-4000-8000-000000000010',
        '2026-01-16 11:30:00',
        '2026-02-13 12:30:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Email']
    );

-- Appointment status history
INSERT INTO appointment_status_history (appointment_id, status, created_at)
VALUES
    -- Alice: scheduled, then completed
    ('4a88fd16-76a9-4ded-9f87-f60a9748f641', 'SCHEDULED',  '2026-01-10 09:00:00'),
    ('4a88fd16-76a9-4ded-9f87-f60a9748f641', 'NEEDS_FEEDBACK',  '2026-01-11 11:00:00'),
    ('4a88fd16-76a9-4ded-9f87-f60a9748f641', 'COMPLETED',  '2026-02-01 11:00:00'),

    -- Bob: scheduled, rescheduled, then completed
    ('a7b108a8-1ce0-4a86-98d9-faa4847e6f1d', 'SCHEDULED',  '2026-01-11 11:00:00'),
    ('a7b108a8-1ce0-4a86-98d9-faa4847e6f1d', 'NEEDS_FEEDBACK',  '2026-01-12 12:00:00'),
    ('a7b108a8-1ce0-4a86-98d9-faa4847e6f1d', 'RESCHEDULED','2026-01-20 10:00:00'),
    ('a7b108a8-1ce0-4a86-98d9-faa4847e6f1d', 'COMPLETED',  '2026-02-05 15:00:00'),

    -- Carlos: scheduled only
    ('53272579-0983-42a9-88ce-820c79471bec', 'SCHEDULED',  '2026-01-12 08:30:00'),
    ('53272579-0983-42a9-88ce-820c79471bec', 'NEEDS_FEEDBACK',  '2026-01-13 13:00:00'),
    ('53272579-0983-42a9-88ce-820c79471bec', 'COMPLETED',  '2026-02-10 10:30:00'),

    -- Dana: scheduled, then did not attend
    ('f2dbd949-2e39-48ef-a483-aaafda4a5608', 'SCHEDULED',  '2026-01-13 14:00:00'),
    ('f2dbd949-2e39-48ef-a483-aaafda4a5608', 'NEEDS_FEEDBACK',  '2026-01-14 14:00:00'),
    ('f2dbd949-2e39-48ef-a483-aaafda4a5608', 'DID_NOT_ATTEND', '2026-02-12 11:30:00'),

    -- Evan: scheduled, then completed
    ('61707704-3ea5-4707-b71a-22c276e7c4db', 'SCHEDULED',  '2026-01-14 10:00:00'),
    ('61707704-3ea5-4707-b71a-22c276e7c4db', 'NEEDS_FEEDBACK',  '2026-01-15 15:00:00'),
    ('61707704-3ea5-4707-b71a-22c276e7c4db', 'COMPLETED',  '2026-02-14 16:00:00'),

    -- Fiona: scheduled, then completed (session did not happen – service provider issue)
    ('b1e8a0cf-6b7f-4d3a-bef6-eb6822c59f2c', 'SCHEDULED',  '2026-01-15 09:00:00'),
    ('b1e8a0cf-6b7f-4d3a-bef6-eb6822c59f2c', 'NEEDS_FEEDBACK',  '2026-01-16 16:00:00'),
    ('b1e8a0cf-6b7f-4d3a-bef6-eb6822c59f2c', 'COMPLETED',  '2026-02-18 11:00:00'),

    -- Luka: scheduled, then completed (session did not happen – person did not comply)
    ('5838a444-2ed5-4e70-850e-aada3f9eaaf3', 'SCHEDULED',  '2026-01-16 13:00:00'),
    ('5838a444-2ed5-4e70-850e-aada3f9eaaf3', 'NEEDS_FEEDBACK',  '2026-01-17 17:00:00'),
    ('5838a444-2ed5-4e70-850e-aada3f9eaaf3', 'COMPLETED',  '2026-02-20 13:45:00'),

    -- Gloria: scheduled, needs feedback then did not happen
    ('24b90534-830a-43f6-8f5a-e3eee268505a', 'SCHEDULED',  '2026-01-17 15:00:00'),
    ('24b90534-830a-43f6-8f5a-e3eee268505a', 'NEEDS_FEEDBACK',  '2026-01-18 18:00:00'),
    ('24b90534-830a-43f6-8f5a-e3eee268505a', 'DID_NOT_HAPPEN', '2026-02-12 13:30:00'),

    -- Harry: scheduled, then needs feedback
    ('856b6c0f-7948-4352-8265-f7b59f6f7c68', 'SCHEDULED',  '2026-01-18 15:00:00'),
    ('856b6c0f-7948-4352-8265-f7b59f6f7c68', 'NEEDS_FEEDBACK',  '2026-01-19 19:00:00'),

    -- Imran: scheduled
    ('d18f169c-e215-46a7-848e-e2244c1faa41', 'SCHEDULED',  '2026-01-19 16:00:00');

-- Appointment ICS feedback records
-- d0000000-* UUIDs are used for appointment_ics_feedback rows
-- Only sessions that have concluded (COMPLETED, DID_NOT_HAPPEN or DID_NOT_ATTEND) have feedback
INSERT INTO appointment_ics_feedback (
    id,
    appointment_ics_id,
    record_session_did_session_happen,
    record_session_how_session_took_place,
    record_session_not_in_person_reason,
    record_session_pdu,
    record_session_address_line1,
    record_session_address_line2,
    record_session_town_or_city,
    record_session_county,
    record_session_postcode,
    -- New non-attendance / session-not-happen fields
    record_session_did_person_attend,
    record_session_not_happen_reason,
    record_session_not_happen_reason_details,
    record_session_no_attendance_information,
    session_details_was_person_late,
    session_details_late_reason,
    session_details_duration,
    session_feedback_what_happened,
    session_feedback_behaviour,
    session_feedback_strengths_identified,
    issues_concerns_identified,
    issues_concerns_notify_probation_practitioner,
    next_steps_planned_for_next_session,
    next_steps_actions_before_next_session,
    created_at,
    created_by
)
VALUES
    -- Alice: session happened via phone call; no attendance-related fields
    (
        'd0000000-0000-4000-8000-000000000001',
        'b0000000-0000-4000-8000-000000000001',
        true,
        'Phone call',
        NULL,
        NULL,  -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        NULL, NULL, NULL, NULL,        -- non-attendance / session-not-happen fields
        false,
        NULL,
        '1 hour and 0 minutes',
        'Alice engaged well with the session. Discussed progress on housing application and employment goals.',
        'Positive and cooperative throughout. Showed motivation to engage with support.',
        'Strong communication skills and clear awareness of personal goals.',
        NULL,
        false,
        'Review housing application outcome and set next employment milestone.',
        'Alice to submit housing application by 10 February 2026.',
        '2026-02-01 11:15:00',
        'a0000000-0000-4000-8000-000000000001'
    ),
    -- Bob: session happened via video call; person arrived late; concern raised with probation practitioner
    (
        'd0000000-0000-4000-8000-000000000002',
        'b0000000-0000-4000-8000-000000000002',
        true,
        'Video call',
        NULL,
        NULL,  -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        NULL, NULL, NULL, NULL,        -- non-attendance / session-not-happen fields
        true,
        'Technical issues connecting to the video call platform.',
        '45 minutes',
        'Bob joined late due to technical difficulties. Discussed coping strategies for anxiety and upcoming court date.',
        'Initially anxious but settled as the session progressed. Engaged constructively with suggested coping techniques.',
        'Demonstrated resilience and willingness to engage despite initial technical barriers.',
        'Bob expressed concern about the upcoming court date and its potential impact on his accommodation.',
        true,
        'Follow up on court outcome and reassess accommodation support needs.',
        'Practitioner to liaise with probation officer regarding court date concerns. Bob to practise breathing exercises daily.',
        '2026-02-05 15:30:00',
        'a0000000-0000-4000-8000-000000000002'
    ),
    -- Carlos: session happened at probation office; PDU recorded; no issues
    (
        'd0000000-0000-4000-8000-000000000003',
        'b0000000-0000-4000-8000-000000000003',
        true,
        'In person (probation office)',
        NULL,
        'PDU-South-East',  -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields (not applicable for probation office)
        NULL, NULL, NULL, NULL,        -- non-attendance / session-not-happen fields
        false,
        NULL,
        '1 hour and 30 minutes',
        'Carlos attended the probation office appointment on time. Session focused on reviewing licence conditions and reintegration support.',
        'Cooperative and engaged. Demonstrated understanding of licence obligations and willingness to comply.',
        'Strong sense of personal accountability and clear progress on agreed action points.',
        NULL,
        false,
        'Review progress against licence conditions and plan next in-person appointment.',
        'Carlos to attend community service placement as agreed before next session.',
        '2026-02-10 10:45:00',
        'a0000000-0000-4000-8000-000000000001'
    ),
    -- Dana: session did not happen; person did NOT attend (did not answer phone)
    --       record_session_did_person_attend = false → noAttendanceInformation populated
    (
        'd0000000-0000-4000-8000-000000000004',
        'b0000000-0000-4000-8000-000000000004',
        false,
        NULL,
        NULL,          -- record_session_not_in_person_reason (n/a – session did not happen)
        NULL,          -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        false,         -- record_session_did_person_attend
        NULL,          -- record_session_not_happen_reason (n/a – person did not attend)
        NULL,          -- record_session_not_happen_reason_details
        'Called twice on the day of the appointment – no answer either time. Left a voicemail after each attempt. Sent a follow-up text message the following morning but received no response.',
        NULL, NULL, NULL,  -- session details (n/a)
        NULL, NULL, NULL,  -- session feedback (n/a)
        'Unable to make contact with Dana. Pattern of non-attendance may indicate additional needs or barriers to engagement.',
        true,
        'Attempt re-engagement via alternative contact method and reschedule session.',
        'Practitioner to send letter and attempt contact via text before rescheduling.',
        '2026-02-12 11:45:00',
        'a0000000-0000-4000-8000-000000000002'
    ),
    -- Evan: session happened via video call; minor financial concern noted, no probation notification needed
    (
        'd0000000-0000-4000-8000-000000000005',
        'b0000000-0000-4000-8000-000000000005',
        true,
        'Video call',
        NULL,
        NULL,  -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        NULL, NULL, NULL, NULL,        -- non-attendance / session-not-happen fields
        false,
        NULL,
        '1 hour and 15 minutes',
        'Productive session focused on Evan''s reintegration plan. Discussed volunteer work opportunities and budgeting skills.',
        'Engaged and enthusiastic. Came prepared with questions about the volunteer programme.',
        'Proactive attitude and strong motivation to build a stable routine.',
        'Evan mentioned difficulties managing benefit payments. Referred to financial support service.',
        false,
        'Review progress with volunteer coordinator and assess budgeting skill development.',
        'Evan to attend initial meeting with financial support advisor before next session.',
        '2026-02-14 16:20:00',
        'a0000000-0000-4000-8000-000000000003'
    ),
    -- Fiona: session did not happen; person DID attend but session could not proceed – SERVICE_PROVIDER_ISSUE
    --        (room booking cancelled due to fire alarm drill)
    --        Add a COMPLETED status for Fiona so this feedback row is valid
    (
        'd0000000-0000-4000-8000-000000000006',
        'b0000000-0000-4000-8000-000000000006',
        false,
        NULL,
        NULL,          -- record_session_not_in_person_reason
        NULL,          -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        true,          -- record_session_did_person_attend
        'SERVICE_PROVIDER_ISSUE',
        'The room booking had been cancelled without notice due to a fire alarm drill that ran over time. A replacement room was not available at short notice.',
        NULL,          -- record_session_no_attendance_information (n/a – person attended)
        NULL, NULL, NULL,  -- session details (n/a)
        NULL, NULL, NULL,  -- session feedback (n/a)
        NULL,
        false,
        'Reschedule and confirm room availability in advance.',
        'Practitioner to arrange a new appointment date and confirm the venue with Fiona.',
        '2026-02-18 11:00:00',
        'a0000000-0000-4000-8000-000000000003'
    ),
    -- Luka: session did not happen; person DID attend but did not comply (disruptive behaviour)
    --       record_session_did_person_attend = true, reason = PERSON_DID_NOT_COMPLY
    (
        'd0000000-0000-4000-8000-000000000007',
        'b0000000-0000-4000-8000-000000000007',
        false,
        NULL,
        NULL,          -- record_session_not_in_person_reason
        NULL,          -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        true,          -- record_session_did_person_attend
        'PERSON_DID_NOT_COMPLY',
        'Luka attended the appointment but became disruptive during the introduction, refusing to engage with the session structure and using disrespectful language. The session was ended early for safeguarding reasons.',
        NULL,          -- record_session_no_attendance_information (n/a – person attended)
        NULL, NULL, NULL,  -- session details (n/a)
        NULL, NULL, NULL,  -- session feedback (n/a)
        'Disruptive behaviour resulted in early termination of the session. Safeguarding considerations noted.',
        true,
        'Review engagement approach and discuss incident with probation officer before rescheduling.',
        'Practitioner to complete incident report and share with probation officer.',
        '2026-02-20 13:45:00',
        'a0000000-0000-4000-8000-000000000001'
    ),
    -- Gloria: session did not happen; person DID attend but did not comply (aggressive behaviour)
    --       record_session_did_person_attend = true, reason = PERSON_DID_NOT_COMPLY
    (
        'd0000000-0000-4000-8000-000000000008',
        'b0000000-0000-4000-8000-000000000008',
        false,
        NULL,
        NULL,          -- record_session_not_in_person_reason (n/a – session did not happen)
        NULL,          -- record_session_pdu
        NULL, NULL, NULL, NULL, NULL,  -- address fields
        true,         -- record_session_did_person_attend
        'PERSON_DID_NOT_COMPLY',
        'Gloria attended the appointment but became aggressive and using disrespectful language when introduced. Due to safeguarding reasons, the session finished early',
        NULL,          -- record_session_no_attendance_information (n/a – person attended)
        NULL, NULL, NULL,  -- session details (n/a)
        NULL, NULL, NULL,  -- session feedback (n/a)
        'Disruptive behaviour resulted in early termination of the session. Safeguarding considerations noted.',
        true,
        'Review engagement approach and discuss incident with probation officer before rescheduling.',
        'Practitioner to complete incident report and share with probation officer.',
        '2026-02-27 14:50:00',
        'a0000000-0000-4000-8000-000000000003'
    );

