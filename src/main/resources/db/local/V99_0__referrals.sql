-- sql
-- Persons (use the referral ids from V99_0__referrals as person ids)
INSERT INTO person (id, first_name, last_name, sex, date_of_birth, ethnicity, preferred_language, disability, neurodiverse_conditions, religion_or_belief, gender_identity, transgender, sexual_orientation, created_at)
VALUES
    ('46abce04-e137-41e5-b18f-606a35375b33', 'Alice',  'Smith',  'F', '1985-07-21', 'White',  'English', NULL, NULL, 'None', NULL, FALSE, 'Heterosexual', '2024-01-15 09:30:00'),
    ('27c313cc-7200-4d00-842c-21ab46e06c50', 'Bob',    'Jones',  'M', '1990-01-15', 'Black',  'English', 'None', 'ADHD', 'Christian', NULL, FALSE, 'Gay',        '2024-02-20 14:45:00'),
    ('a9e31863-2369-4593-b6fd-280bb4514a5e', 'Carlos', 'Garcia', 'M', '1978-11-30', 'Hispanic','Spanish', NULL, NULL, 'Catholic', NULL, FALSE, 'Heterosexual','2024-03-05 08:00:00'),
    ('1c35e4ab-de5f-4b6b-9a1f-31859584fce0', 'Dana',   'Lee',    'F', '2002-05-12', 'Asian',  'English', NULL, NULL, 'None', NULL, FALSE, 'Bisexual',   '2024-04-10 16:20:00'),
    ('9383489a-bc44-4a02-b4e4-0bab703b3d80', 'Evan',   'Brown',  'M', '1989-09-09', 'Mixed',  'English', NULL, 'Autism', 'None', NULL, FALSE, 'Heterosexual','2024-05-01 11:10:00'),
    ('bad19757-7a57-4e8a-b88a-e808a1e167b4', 'Fiona',  'Ng',     'F', '1994-02-28', 'Asian',  'English', NULL, NULL, 'Buddhism', NULL, FALSE, 'Heterosexual','2024-06-18 13:55:00');

-- Referrals (reference person_id instead of embedding person fields)
INSERT INTO referral (id, person_id, community_service_provider_id, crn, reference_number, created_at, updated_at, urgency)
VALUES
    ('3f9d6a0e-1a2b-4c3d-8e9f-0123456789ab', '46abce04-e137-41e5-b18f-606a35375b33','bc852b9d-1997-4ce4-ba7f-cd1759e15d2b', 'CRN0001', 'QD0878DE', '2026-01-01 09:30:00',  '2026-01-12 09:30:00', false),
    ('8a1b2c3d-4e5f-6789-abcd-abcdef012345', '27c313cc-7200-4d00-842c-21ab46e06c50','5f4c3e2d-9c6b-4f1a-8e2d-4b5c6d7e8f90', 'CRN0002', 'MA9178AC', '2026-01-02 14:45:00','2026-01-12 14:45:00', false),
    ('11111111-2222-3333-4444-555555555555', 'a9e31863-2369-4593-b6fd-280bb4514a5e','27ff2cfe-8eeb-4ebf-8909-3c9e0d4fe6a5',  'CRN0003', 'FW7833ED', '2026-01-03 08:00:00' ,'2026-01-10 08:00:00', false),
    ('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', '1c35e4ab-de5f-4b6b-9a1f-31859584fce0', '4a1fca07-aa93-46ab-8145-4017811d4749',  'CRN0004', 'ZW4199AC', '2026-01-04 16:20:00', '2026-01-09 16:20:00', false),
    ('123e4567-e89b-12d3-a456-426614174000', '9383489a-bc44-4a02-b4e4-0bab703b3d80', '0ca2070b-fbe6-4baf-a89c-6be88a0c3b10', 'CRN0005', 'CC3019ED', '2026-01-05 11:10:00', '2026-01-11 11:10:00', false),
    ('6f1e2d3c-4b5a-6978-8c9d-0a1b2c3d4e5f', 'bad19757-7a57-4e8a-b88a-e808a1e167b4', 'd5e0c774-8e95-4fe4-b9f3-8ecc2b62c242', 'CRN0006', 'KY2594AC', '2026-01-06 13:55:00',  '2024-06-18 13:55:00', true);

-- Person addresses (sample addresses linked by person_id)
INSERT INTO person_address (id, person_id, address, phone_number, email_address, created_at)
VALUES
    ('b1e2c3d4-0000-4000-8000-000000000001', '46abce04-e137-41e5-b18f-606a35375b33', '12 High Street, Townsville', '0207 000 0001', 'alice.smith@example.com', '2024-01-15 09:30:00'),
    ('b1e2c3d4-0000-4000-8000-000000000002', '27c313cc-7200-4d00-842c-21ab46e06c50', 'Flat 2, 34 Market Road',        '0207 000 0002', 'bob.jones@example.com',   '2024-02-20 14:45:00'),
    ('b1e2c3d4-0000-4000-8000-000000000003', 'a9e31863-2369-4593-b6fd-280bb4514a5e', '23 Calle Ocho, Madrid',      '+34 600 000 003',          'carlos.garcia@example.com','2024-03-05 08:00:00'),
    ('b1e2c3d4-0000-4000-8000-000000000004', '1c35e4ab-de5f-4b6b-9a1f-31859584fce0', 'Flat 5, 1 River Lane',    '0207 000 0004', 'dana.lee@example.com',     '2024-04-10 16:20:00'),
    ('b1e2c3d4-0000-4000-8000-000000000005', '9383489a-bc44-4a02-b4e4-0bab703b3d80', '9 Oak Avenue',            '0207 000 0005', 'evan.brown@example.com',   '2024-05-01 11:10:00'),
    ('b1e2c3d4-0000-4000-8000-000000000006', 'bad19757-7a57-4e8a-b88a-e808a1e167b4', '3 Cherry Close',          '0207 000 0006', 'fiona.ng@example.com',     '2024-06-18 13:55:00');

-- Referral events (simple sample events referencing the referrals)
INSERT INTO referral_event (id, referral_id, event_type, created_at, actor_type, actor_id)
VALUES
    ('e0000000-0000-4000-8000-000000000001', '3f9d6a0e-1a2b-4c3d-8e9f-0123456789ab', 'CREATED', '2024-01-15 09:30:00', 'SYSTEM', NULL),
    ('e0000000-0000-4000-8000-000000000002', '8a1b2c3d-4e5f-6789-abcd-abcdef012345', 'CREATED', '2024-02-20 14:45:00', 'SYSTEM', NULL),
    ('e0000000-0000-4000-8000-000000000003', '11111111-2222-3333-4444-555555555555', 'CREATED', '2024-03-05 08:00:00', 'SYSTEM', NULL),
    ('e0000000-0000-4000-8000-000000000004', 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'CREATED', '2024-04-10 16:20:00', 'SYSTEM', NULL),
    ('e0000000-0000-4000-8000-000000000005', '123e4567-e89b-12d3-a456-426614174000', 'CREATED', '2024-05-01 11:10:00', 'SYSTEM', NULL),
    ('e0000000-0000-4000-8000-000000000006', '6f1e2d3c-4b5a-6978-8c9d-0a1b2c3d4e5f', 'CREATED', '2024-06-18 13:55:00', 'SYSTEM', NULL);
