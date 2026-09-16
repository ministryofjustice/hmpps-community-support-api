UPDATE action_plan_step_question
SET
  title = CASE id
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101' THEN 'How often will sessions take place?'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102' THEN 'How will the sessions take place?'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103' THEN 'What format will you use for the sessions?'
  END,
  answer_type = CASE id
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101' THEN 'TEXTAREA'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102' THEN 'RADIO'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103' THEN 'CHECKBOX'
  END,
  max_number_responses = CASE id
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101' THEN 1
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102' THEN 1
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103' THEN 2
  END,
  hint = CASE id
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101' THEN 'For example, every week, every 2 weeks, every month.'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102' THEN 'Select one option.'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103' THEN 'Select all that apply.'
  END
WHERE id IN (
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103'
);

DELETE FROM action_plan_step_question_choice
WHERE action_plan_step_question_id IN (
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
  'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3104'
);

DELETE FROM action_plan_step_question
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3104';

INSERT INTO action_plan_step_question_choice (id, action_plan_step_question_id, order_number, label, value, has_free_text, free_text_label, free_text_hint)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3201',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        1,
        'In person',
        'IN_PERSON',
        true,
        'Reason for not being in person',
        'Why are the sessions not in person?'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3202',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        2,
        'Video call',
        'VIDEO_CALL',
        true,
        'Reason for not being in person',
        'Why are the sessions not in person?'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3203',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
        3,
        'Phone call',
        'PHONE_CALL',
        true,
        'Reason for not being in person',
        'Why are the sessions not in person?'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3204',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        1,
        'One-to-one session',
        'ONE_TO_ONE_SESSION',
        false,
        NULL,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3207',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
        2,
        'Group session',
        'GROUP_SESSION',
        false,
        NULL,
        NULL
    )
ON CONFLICT (id) DO UPDATE
SET action_plan_step_question_id = EXCLUDED.action_plan_step_question_id,
    order_number = EXCLUDED.order_number,
    label = EXCLUDED.label,
    value = EXCLUDED.value,
    has_free_text = EXCLUDED.has_free_text,
    free_text_label = EXCLUDED.free_text_label,
    free_text_hint = EXCLUDED.free_text_hint;
