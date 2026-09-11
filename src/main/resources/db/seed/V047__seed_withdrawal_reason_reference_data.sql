-- V047: Seed static reference data for withdrawal reasons

INSERT INTO withdrawal_reason (id, name, group_name)
VALUES
    ('7c1a1e10-1a01-4c2d-8e3f-1a2b3c4d5f01', 'Ineligible referral', 'Problem with referral'),
    ('7c1a1e10-1a02-4c2d-8e3f-1a2b3c4d5f02', 'Mistaken or duplicate referral', 'Problem with referral'),
    ('7c1a1e10-1a03-4c2d-8e3f-1a2b3c4d5f03', 'Acquitted on appeal', 'Sentence or custody related'),
    ('7c1a1e10-1a04-4c2d-8e3f-1a2b3c4d5f04', 'Returned to custody', 'Sentence or custody related'),
    ('7c1a1e10-1a05-4c2d-8e3f-1a2b3c4d5f05', 'Sentence expired', 'Sentence or custody related'),
    ('7c1a1e10-1a06-4c2d-8e3f-1a2b3c4d5f06', 'Sentence revoked', 'Sentence or custody related'),
    ('7c1a1e10-1a07-4c2d-8e3f-1a2b3c4d5f07', 'Died', 'User related'),
    ('7c1a1e10-1a08-4c2d-8e3f-1a2b3c4d5f08', 'Moved out of service area', 'User related'),
    ('7c1a1e10-1a09-4c2d-8e3f-1a2b3c4d5f09', 'Needs met through another route', 'User related'),
    ('7c1a1e10-1a10-4c2d-8e3f-1a2b3c4d5f10', 'Not engaged', 'User related'),
    ('7c1a1e10-1a11-4c2d-8e3f-1a2b3c4d5f11', 'Work, caring commitments or sickness', 'User related'),
    ('7c1a1e10-1a12-4c2d-8e3f-1a2b3c4d5f12', 'Another reason', 'User related')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    group_name = EXCLUDED.group_name;
