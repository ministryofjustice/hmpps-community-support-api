-- Seed reference outcomes for the existing need catalogue.

INSERT INTO outcome (id, need_id, text, order_number, setting)
VALUES
    ('f2a3c4d5-e6f7-4801-9001-000000000001', '3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01', 'I want to secure and maintain settled and suitable accommodation.', 1, 'COMMUNITY'),
    ('f2a3c4d5-e6f7-4801-9001-000000000002', '3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01', 'I want to manage my tenancy and prevent rent arrears or other debts while I am in custody.', 2, 'CUSTODY'),
    ('f2a3c4d5-e6f7-4801-9001-000000000003', '3b1f7e2a-1a02-4c2d-8e3f-1a2b3c4d5e02', 'I want to find and keep suitable employment, or take steps towards employment through education, training, or other opportunities.', 1, 'ALL'),
    ('f2a3c4d5-e6f7-4801-9001-000000000004', '3b1f7e2a-1a03-4c2d-8e3f-1a2b3c4d5e03', 'I want to improve my financial situation by reducing debt, managing my money better and accessing the benefits I am entitled to.', 1, 'ALL'),
    ('f2a3c4d5-e6f7-4801-9001-000000000005', '3b1f7e2a-1a04-4c2d-8e3f-1a2b3c4d5e04', 'I want to make progress in my recovery from drug or alcohol dependency or gambling.', 1, 'ALL'),
    ('f2a3c4d5-e6f7-4801-9001-000000000006', '3b1f7e2a-1a05-4c2d-8e3f-1a2b3c4d5e05', 'I want to make progress in my recovery from drug or alcohol dependency or gambling.', 1, 'ALL'),
    ('f2a3c4d5-e6f7-4801-9001-000000000007', '3b1f7e2a-1a06-4c2d-8e3f-1a2b3c4d5e06', 'I want to find positive ways to spend my time and to build supportive social networks.', 1, 'ALL'),
    ('f2a3c4d5-e6f7-4801-9001-000000000008', '3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07', 'I want to improve my relationships with my family or other people who support me.', 1, 'COMMUNITY'),
    ('f2a3c4d5-e6f7-4801-9001-000000000009', '3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07', 'I want to access support to help with my family relationships and circumstances, and to help me prepare for any changes that may affect me when I leave custody.', 2, 'CUSTODY')
ON CONFLICT (id) DO UPDATE
SET need_id = EXCLUDED.need_id,
    text = EXCLUDED.text,
    order_number = EXCLUDED.order_number,
    setting = EXCLUDED.setting;
