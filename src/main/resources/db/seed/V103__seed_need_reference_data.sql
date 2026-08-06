-- V103: Seed static reference data for needs

INSERT INTO need (id, label, order_number)
VALUES
    ('3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01', 'Accommodation', 1),
    ('3b1f7e2a-1a02-4c2d-8e3f-1a2b3c4d5e02', 'Employment and education', 2),
    ('3b1f7e2a-1a03-4c2d-8e3f-1a2b3c4d5e03', 'Finances', 3),
    ('3b1f7e2a-1a04-4c2d-8e3f-1a2b3c4d5e04', 'Drug use', 4),
    ('3b1f7e2a-1a05-4c2d-8e3f-1a2b3c4d5e05', 'Alcohol use', 5),
    ('3b1f7e2a-1a06-4c2d-8e3f-1a2b3c4d5e06', 'Health and wellbeing', 6),
    ('3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07', 'Personal relationships and community', 7),
    ('3b1f7e2a-1a08-4c2d-8e3f-1a2b3c4d5e08', 'Thinking, behaviours and attitudes', 8)
ON CONFLICT (id) DO UPDATE
SET label = EXCLUDED.label,
    order_number = EXCLUDED.order_number;
