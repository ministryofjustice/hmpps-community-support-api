-- V064: Seed reference data referral pathway tables

INSERT INTO reference_data_referral_pathway (id, title, display_order)
VALUES
    ('ca4d147f-cbc3-4c2d-9e33-627c9974d801', 'Community links', 1),
    ('ca4d147f-cbc3-4c2d-9e33-627c9974d802', 'Housing support', 2),
    ('ca4d147f-cbc3-4c2d-9e33-627c9974d803', 'Jobs and skills support', 3),
    ('ca4d147f-cbc3-4c2d-9e33-627c9974d804', 'Money management', 4)
ON CONFLICT (id) DO UPDATE
SET title = EXCLUDED.title,
    display_order = EXCLUDED.display_order;

INSERT INTO reference_data_referral_pathway_need (id, reference_data_referral_pathway_id, reference_data_need_id)
VALUES
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce01', 'ca4d147f-cbc3-4c2d-9e33-627c9974d801', '3b1f7e2a-1a04-4c2d-8e3f-1a2b3c4d5e04'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce02', 'ca4d147f-cbc3-4c2d-9e33-627c9974d801', '3b1f7e2a-1a05-4c2d-8e3f-1a2b3c4d5e05'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce03', 'ca4d147f-cbc3-4c2d-9e33-627c9974d801', '3b1f7e2a-1a06-4c2d-8e3f-1a2b3c4d5e06'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce04', 'ca4d147f-cbc3-4c2d-9e33-627c9974d801', '3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce05', 'ca4d147f-cbc3-4c2d-9e33-627c9974d802', '3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce06', 'ca4d147f-cbc3-4c2d-9e33-627c9974d803', '3b1f7e2a-1a02-4c2d-8e3f-1a2b3c4d5e02'),
    ('ed5018de-1bf4-46e5-b8be-2a6c944fce07', 'ca4d147f-cbc3-4c2d-9e33-627c9974d804', '3b1f7e2a-1a03-4c2d-8e3f-1a2b3c4d5e03')
ON CONFLICT (id) DO UPDATE
SET reference_data_referral_pathway_id = EXCLUDED.reference_data_referral_pathway_id,
    reference_data_need_id = EXCLUDED.reference_data_need_id;
