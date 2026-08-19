-- V32: Seed session delivery details step and choices

INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3001',
    'c191398c-9661-4983-bafb-be649d877183',
    10,
    'Session Delivery Details',
    'SESSION_DELIVERY'
)
ON CONFLICT (id) DO UPDATE
SET action_plan_template_id = EXCLUDED.action_plan_template_id,
    order_number = EXCLUDED.order_number,
    name = EXCLUDED.name,
    step_type = EXCLUDED.step_type;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3001',
        1,
        'How will the session be delivered?',
        'radio',
        'GENERAL',
        1
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3001',
        2,
        'How many people will be in the group?',
        'radio',
        'GENERAL',
        1
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3001',
        3,
        'How frequently will sessions be delivered?',
        'radio',
        'GENERAL',
        1
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3104',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3001',
        4,
        'How many sessions are required?',
        'textarea',
        'GENERAL',
        1
    )
ON CONFLICT (id) DO UPDATE
SET action_plan_step_id = EXCLUDED.action_plan_step_id,
    order_number = EXCLUDED.order_number,
    title = EXCLUDED.title,
    answer_type = EXCLUDED.answer_type,
    question_type = EXCLUDED.question_type,
    max_number_responses = EXCLUDED.max_number_responses;

INSERT INTO action_plan_step_question_choice (id, action_plan_step_question_id, order_number, label, value, has_free_text, free_text_label)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3201',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
        1,
        'Face-to-face',
        'FACE_TO_FACE',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3202',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
        2,
        'Other',
        'OTHER',
        true,
        'Reason for not meeting face-to-face'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3203',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        1,
        'One-to-one',
        'ONE_TO_ONE',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3204',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        2,
        'Group',
        'GROUP',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3207',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        1,
        'Weekly',
        'WEEKLY',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3208',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        2,
        'Biweekly',
        'BIWEEKLY',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3209',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        3,
        'Monthly',
        'MONTHLY',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3210',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        4,
        'Other',
        'OTHER',
        true,
        'Please specify frequency'
    )
ON CONFLICT (id) DO UPDATE
SET action_plan_step_question_id = EXCLUDED.action_plan_step_question_id,
    order_number = EXCLUDED.order_number,
    label = EXCLUDED.label,
    value = EXCLUDED.value,
    has_free_text = EXCLUDED.has_free_text,
    free_text_label = EXCLUDED.free_text_label;
