-- Local seed data for appointment-related tables
-- Appointments (one per referral, type = ICS)
INSERT INTO appointment (id, referral_id, type)
VALUES
    ('a0000000-0000-4000-8000-000000000001', '3f9d6a0e-1a2b-4c3d-8e9f-0123456789ab', 'ICS'),
    ('a0000000-0000-4000-8000-000000000002', '8a1b2c3d-4e5f-6789-abcd-abcdef012345', 'ICS'),
    ('a0000000-0000-4000-8000-000000000003', '11111111-2222-3333-4444-555555555555', 'ICS'),
    ('a0000000-0000-4000-8000-000000000004', 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'ICS'),
    ('a0000000-0000-4000-8000-000000000005', '123e4567-e89b-12d3-a456-426614174000', 'ICS'),
    ('a0000000-0000-4000-8000-000000000006', '6f1e2d3c-4b5a-6978-8c9d-0a1b2c3d4e5f', 'ICS'),
    ('a0000000-0000-4000-8000-000000000007', '5bfb6628-8d7e-4eab-8f70-b27e166ea73c', 'ICS');

-- Appointment ICS records
INSERT INTO appointment_ics (id, appointment_id, method, method_details, created_at, start_date, created_by, session_communication)
VALUES
    (
        'b0000000-0000-4000-8000-000000000001',
        'a0000000-0000-4000-8000-000000000001',
        'Phone call',
        'session',
        '2026-01-10 09:00:00',
        '2026-02-01 10:00:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000002',
        'a0000000-0000-4000-8000-000000000002',
        'Video call',
        'Microsoft Teams session with Bob Jones',
        '2026-01-11 11:00:00',
        '2026-02-05 14:00:00',
        'a0000000-0000-4000-8000-000000000002',
        ARRAY['Email']
    ),
    (
        'b0000000-0000-4000-8000-000000000003',
        'a0000000-0000-4000-8000-000000000003',
        'In person - Probation Office',
        'Office visit at probation office, Room 3',
        '2026-01-12 08:30:00',
        '2026-02-10 09:30:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000004',
        'a0000000-0000-4000-8000-000000000004',
        'Phone call',
        'Inbound call from Dana Lee',
        '2026-01-13 14:00:00',
        '2026-02-12 11:00:00',
        'a0000000-0000-4000-8000-000000000002',
        ARRAY['Email', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000005',
        'a0000000-0000-4000-8000-000000000005',
        'Video call',
        'Zoom session with Evan Brown',
        '2026-01-14 10:00:00',
        '2026-02-14 15:00:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text', 'Letter']
    ),
    (
        'b0000000-0000-4000-8000-000000000006',
        'a0000000-0000-4000-8000-000000000006',
        'In person',
        'In person meeting - 56 Carlisle Road, London, N1 6XE',
        '2026-01-15 09:00:00',
        '2026-02-18 10:30:00',
        'a0000000-0000-4000-8000-000000000003',
        ARRAY['Phone', 'Text']
    ),
    (
        'b0000000-0000-4000-8000-000000000007',
        'a0000000-0000-4000-8000-000000000007',
        'Phone call',
        'Scheduled welfare check call with Luka Cross',
        '2026-01-16 13:00:00',
        '2026-02-20 13:00:00',
        'a0000000-0000-4000-8000-000000000001',
        ARRAY['Email', 'SMS']
    );

-- Appointment status history
INSERT INTO appointment_status_history (appointment_id, status, created_at)
VALUES
    -- Alice: scheduled, then completed
    ('a0000000-0000-4000-8000-000000000001', 'SCHEDULED',  '2026-01-10 09:00:00'),
    ('a0000000-0000-4000-8000-000000000001', 'COMPLETED',  '2026-02-01 11:00:00'),

    -- Bob: scheduled, rescheduled, then completed
    ('a0000000-0000-4000-8000-000000000002', 'SCHEDULED',  '2026-01-11 11:00:00'),
    ('a0000000-0000-4000-8000-000000000002', 'RESCHEDULED','2026-01-20 10:00:00'),
    ('a0000000-0000-4000-8000-000000000002', 'COMPLETED',  '2026-02-05 15:00:00'),

    -- Carlos: scheduled only
    ('a0000000-0000-4000-8000-000000000003', 'SCHEDULED',  '2026-01-12 08:30:00'),

    -- Dana: scheduled, then did not attend
    ('a0000000-0000-4000-8000-000000000004', 'SCHEDULED',  '2026-01-13 14:00:00'),
    ('a0000000-0000-4000-8000-000000000004', 'DID_NOT_ATTEND', '2026-02-12 11:30:00'),

    -- Evan: scheduled, then completed
    ('a0000000-0000-4000-8000-000000000005', 'SCHEDULED',  '2026-01-14 10:00:00'),
    ('a0000000-0000-4000-8000-000000000005', 'COMPLETED',  '2026-02-14 16:00:00'),

    -- Fiona: scheduled only (upcoming)
    ('a0000000-0000-4000-8000-000000000006', 'SCHEDULED',  '2026-01-15 09:00:00'),

    -- Luka: scheduled only (upcoming)
    ('a0000000-0000-4000-8000-000000000007', 'SCHEDULED',  '2026-01-16 13:00:00');

