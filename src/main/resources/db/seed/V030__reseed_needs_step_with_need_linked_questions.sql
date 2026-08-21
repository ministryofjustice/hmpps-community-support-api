-- V30: Reseed action plan steps and questions with need-linked question model
-- Replaces the per-need-step structure (V026) with a single "Needs" step
-- where each question carries its own need_id link.

-- Delete previously-created steps from V026 (questions/answers/revisions cascade).
DELETE FROM action_plan_step
WHERE id IN (
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67001',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67002',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67003',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67004',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67005',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67006',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67007',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67008',
    'aa7f37f1-2e35-4425-b0f4-d58ef4d67009'
);

-- Single "Needs" step that contains all need-linked outcome questions.
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
    'cc3f48e2-3f46-5536-c1f5-e69f05e8f101',
    'c191398c-9661-4983-bafb-be649d877183',
    1,
    'Needs',
    'NEED'
)
ON CONFLICT (id) DO UPDATE
SET action_plan_template_id = EXCLUDED.action_plan_template_id,
    order_number             = EXCLUDED.order_number,
    name                     = EXCLUDED.name,
    step_type                = EXCLUDED.step_type;

-- One outcome question per need (ordered to match need order_number).
INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses, need_id)
VALUES
    ('dd44c3d4-5168-7758-e3b7-a81b27aab301', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab302', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 2, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a02-4c2d-8e3f-1a2b3c4d5e02'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab303', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 3, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a03-4c2d-8e3f-1a2b3c4d5e03'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab304', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 4, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a04-4c2d-8e3f-1a2b3c4d5e04'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab305', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 5, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a05-4c2d-8e3f-1a2b3c4d5e05'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab306', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 6, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a06-4c2d-8e3f-1a2b3c4d5e06'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab307', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 7, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07'),
    ('dd44c3d4-5168-7758-e3b7-a81b27aab308', 'cc3f48e2-3f46-5536-c1f5-e69f05e8f101', 8, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10, '3b1f7e2a-1a08-4c2d-8e3f-1a2b3c4d5e08')
ON CONFLICT (id) DO UPDATE
SET action_plan_step_id    = EXCLUDED.action_plan_step_id,
    order_number           = EXCLUDED.order_number,
    title                  = EXCLUDED.title,
    answer_type            = EXCLUDED.answer_type,
    question_type          = EXCLUDED.question_type,
    max_number_responses   = EXCLUDED.max_number_responses,
    need_id                = EXCLUDED.need_id;

