-- V51: Seed session delivery additional steps and choices - risk and adjustments, service end date, user involvement steps

-- patch existing free_text_label values to update the label for session delivery details step
UPDATE action_plan_step_question_choice
SET has_free_text = false,
    free_text_label = NULL,
    free_text_hint = NULL
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3201';

UPDATE action_plan_step_question_choice
SET free_text_label = 'Why are the sessions not in person?',
    free_text_hint = NULL
WHERE id IN (
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3202',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3203'
);

-- risk and adjustments step
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
           'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3002',
           'c191398c-9661-4983-bafb-be649d877183',
           11,
           'Risks and adjustments',
           'RISK_AND_ADJUSTMENTS'
       )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_template_id = EXCLUDED.action_plan_template_id,
                            order_number = EXCLUDED.order_number,
                            name = EXCLUDED.name,
                            step_type = EXCLUDED.step_type;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3002',
        1,
        'Are there any risks associated with the planned activities?',
        'RADIO',
        'GENERAL',
        1
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3002',
        2,
        'Will you put any reasonable adjustments in place to help {{ firstName }} take part in the planned activities?',
        'RADIO',
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
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3211',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105',
        1,
        'Yes',
        'YES',
        true,
        'Give details about the risks and what you will put in place to reduce them'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3212',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105',
        2,
        'No',
        'NO',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3213',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106',
        1,
        'Yes',
        'YES',
        true,
        'Give details about what reasonable adjustments you will make and how this will support {{ firstName }}'
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3214',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106',
        2,
        'No',
        'NO',
        false,
        NULL
    )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_step_question_id = EXCLUDED.action_plan_step_question_id,
                            order_number = EXCLUDED.order_number,
                            label = EXCLUDED.label,
                            value = EXCLUDED.value,
                            has_free_text = EXCLUDED.has_free_text,
                            free_text_label = EXCLUDED.free_text_label;

-- is service end date changed step
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
           'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3003',
           'c191398c-9661-4983-bafb-be649d877183',
           12,
           'Is service end date changed',
           'SRV_END_DATE_CHECK'
       )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_template_id = EXCLUDED.action_plan_template_id,
                            order_number = EXCLUDED.order_number,
                            name = EXCLUDED.name,
                            step_type = EXCLUDED.step_type;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3003',
        1,
        'Is the service end date still {{ service_end_date }}?',
        'RADIO',
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
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3215',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107',
        1,
        'Yes',
        'YES',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3216',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107',
        2,
        'No, I need to change the date',
        'NO',
        false,
        NULL
    )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_step_question_id = EXCLUDED.action_plan_step_question_id,
                            order_number = EXCLUDED.order_number,
                            label = EXCLUDED.label,
                            value = EXCLUDED.value,
                            has_free_text = EXCLUDED.has_free_text,
                            free_text_label = EXCLUDED.free_text_label;

-- change service end date step
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
           'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3004',
           'c191398c-9661-4983-bafb-be649d877183',
           13,
           'Change service end date',
           'CHANGE_SRV_END_DATE'
       )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_template_id = EXCLUDED.action_plan_template_id,
                            order_number = EXCLUDED.order_number,
                            name = EXCLUDED.name,
                            step_type = EXCLUDED.step_type;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, hint, answer_type, question_type, max_number_responses)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3108',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3004',
        1,
        'What is the new service end date?',
        NULL,
        'DATE',
        'GENERAL',
        1
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3109',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3004',
        2,
        'Why are you changing the service end date?',
        NULL,
        'TEXTAREA',
        'GENERAL',
        1
    )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_step_id = EXCLUDED.action_plan_step_id,
                            order_number = EXCLUDED.order_number,
                            title = EXCLUDED.title,
                            hint = EXCLUDED.hint,
                            answer_type = EXCLUDED.answer_type,
                            question_type = EXCLUDED.question_type,
                            max_number_responses = EXCLUDED.max_number_responses;

-- user involvement step
INSERT INTO action_plan_step (id, action_plan_template_id, order_number, name, step_type)
VALUES (
           'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3005',
           'c191398c-9661-4983-bafb-be649d877183',
           14,
           'User involvement',
           'USER_INVOLVEMENT'
       )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_template_id = EXCLUDED.action_plan_template_id,
                            order_number = EXCLUDED.order_number,
                            name = EXCLUDED.name,
                            step_type = EXCLUDED.step_type;

INSERT INTO action_plan_step_question (id, action_plan_step_id, order_number, title, answer_type, question_type, max_number_responses)
VALUES
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3005',
        1,
        'Was {{ firstName }} involved in creating the action plan?',
        'RADIO',
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
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3217',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110',
        1,
        'Yes',
        'YES',
        false,
        NULL
    ),
    (
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3218',
        'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110',
        2,
        'No',
        'NO',
        true,
        'Give details about why {{ firstName }} was not involved in creating the action plan'
    )
    ON CONFLICT (id) DO UPDATE
                            SET action_plan_step_question_id = EXCLUDED.action_plan_step_question_id,
                            order_number = EXCLUDED.order_number,
                            label = EXCLUDED.label,
                            value = EXCLUDED.value,
                            has_free_text = EXCLUDED.has_free_text,
                            free_text_label = EXCLUDED.free_text_label;
