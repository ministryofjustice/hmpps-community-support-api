-- V17: Seed initial global Action Plan template placeholder data.
-- This placeholder template can be replaced later using the fixed UUIDs below.

-- Seed template row.
INSERT INTO action_plan_template (id)
VALUES ('c191398c-9661-4983-bafb-be649d877183')
ON CONFLICT (id) DO NOTHING;

-- Seed one step for the template.
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name)
VALUES (
    '7dc752c5-020a-4287-a347-9b1aa8412416',
    'c191398c-9661-4983-bafb-be649d877183',
    1,
    'Basic Information'
)
ON CONFLICT (id) DO UPDATE
SET action_plan_template_id = EXCLUDED.action_plan_template_id,
    order_number = EXCLUDED.order_number,
    name = EXCLUDED.name;

-- Seed one freetext-style question (stored as textarea) for the step.
INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type)
VALUES (
    '73e9acfe-86ce-44ff-9be5-eb6151ed0f4c',
    '7dc752c5-020a-4287-a347-9b1aa8412416',
    1,
    'What is the Action Plan?',
    'textarea'
)
ON CONFLICT (id) DO UPDATE
SET action_plan_step_id = EXCLUDED.action_plan_step_id,
    order_number = EXCLUDED.order_number,
    title = EXCLUDED.title,
    answer_type = EXCLUDED.answer_type;
