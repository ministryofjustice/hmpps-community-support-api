-- V104: Seed need-based action plan steps and default questions

DELETE FROM action_plan_step_question
WHERE id = '73e9acfe-86ce-44ff-9be5-eb6151ed0f4c';

DELETE FROM action_plan_step
WHERE id = '7dc752c5-020a-4287-a347-9b1aa8412416';

INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type, need_id)
VALUES
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67001', 'c191398c-9661-4983-bafb-be649d877183', 1, 'Accommodation', 'NEED', '3b1f7e2a-1a01-4c2d-8e3f-1a2b3c4d5e01'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67002', 'c191398c-9661-4983-bafb-be649d877183', 2, 'Employment and education', 'NEED', '3b1f7e2a-1a02-4c2d-8e3f-1a2b3c4d5e02'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67003', 'c191398c-9661-4983-bafb-be649d877183', 3, 'Finances', 'NEED', '3b1f7e2a-1a03-4c2d-8e3f-1a2b3c4d5e03'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67004', 'c191398c-9661-4983-bafb-be649d877183', 4, 'Drug use', 'NEED', '3b1f7e2a-1a04-4c2d-8e3f-1a2b3c4d5e04'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67005', 'c191398c-9661-4983-bafb-be649d877183', 5, 'Alcohol use', 'NEED', '3b1f7e2a-1a05-4c2d-8e3f-1a2b3c4d5e05'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67006', 'c191398c-9661-4983-bafb-be649d877183', 6, 'Health and wellbeing', 'NEED', '3b1f7e2a-1a06-4c2d-8e3f-1a2b3c4d5e06'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67007', 'c191398c-9661-4983-bafb-be649d877183', 7, 'Personal relationships and community', 'NEED', '3b1f7e2a-1a07-4c2d-8e3f-1a2b3c4d5e07'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67008', 'c191398c-9661-4983-bafb-be649d877183', 8, 'Thinking, behaviours and attitudes', 'NEED', '3b1f7e2a-1a08-4c2d-8e3f-1a2b3c4d5e08'),
    ('aa7f37f1-2e35-4425-b0f4-d58ef4d67009', 'c191398c-9661-4983-bafb-be649d877183', 9, 'Anything else we should know?', 'CATCH_ALL', NULL)
ON CONFLICT (id) DO UPDATE
SET action_plan_template_id = EXCLUDED.action_plan_template_id,
    order_number = EXCLUDED.order_number,
    name = EXCLUDED.name,
    step_type = EXCLUDED.step_type,
    need_id = EXCLUDED.need_id;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses)
VALUES
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec001', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67001', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec002', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67002', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec003', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67003', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec004', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67004', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec005', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67005', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec006', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67006', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec007', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67007', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec008', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67008', 1, 'What is the desired outcome?', 'textarea', 'OUTCOME', 10),
    ('bb22b1a0-6656-49f5-a0dc-a95d3a6ec009', 'aa7f37f1-2e35-4425-b0f4-d58ef4d67009', 1, 'Anything else we should know?', 'textarea', 'GENERAL', 1)
ON CONFLICT (id) DO UPDATE
SET action_plan_step_id = EXCLUDED.action_plan_step_id,
    order_number = EXCLUDED.order_number,
    title = EXCLUDED.title,
    answer_type = EXCLUDED.answer_type,
    question_type = EXCLUDED.question_type,
    max_number_responses = EXCLUDED.max_number_responses;
