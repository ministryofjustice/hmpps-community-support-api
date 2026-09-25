-- add question_key column to action_plan_step_question table and populate it with values based on the id of the question
ALTER TABLE action_plan_step_question
    ADD COLUMN question_key VARCHAR(100);

UPDATE action_plan_step_question
SET question_key = CASE id
    WHEN '73e9acfe-86ce-44ff-9be5-eb6151ed0f4c' THEN 'ACTION_PLAN_DESCRIPTION'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab301' THEN 'ACCOMMODATION_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab302' THEN 'EMPLOYMENT_AND_EDUCATION_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab303' THEN 'FINANCES_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab304' THEN 'DRUG_USE_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab305' THEN 'ALCOHOL_USE_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab306' THEN 'HEALTH_AND_WELLBEING_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab307' THEN 'PERSONAL_RELATIONSHIPS_AND_COMMUNITY_OUTCOME'
    WHEN 'dd44c3d4-5168-7758-e3b7-a81b27aab308' THEN 'THINKING_BEHAVIOURS_AND_ATTITUDES_OUTCOME'
    WHEN 'bb22b1a0-6656-49f5-a0dc-a95d3a6ec009' THEN 'ANYTHING_ELSE_WE_SHOULD_KNOW'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101' THEN 'SESSION_FREQUENCY'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102' THEN 'SESSION_DELIVERY_METHOD'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103' THEN 'SESSION_FORMAT'
    -- WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3104' THEN 'SESSION_COUNT'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105' THEN 'RISK_ASSOCIATED_WITH_PLANNED_ACTIVITIES'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106' THEN 'REASONABLE_ADJUSTMENTS_FOR_PLANNED_ACTIVITIES'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107' THEN 'SERVICE_END_DATE_CHECK'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3108' THEN 'NEW_SERVICE_END_DATE'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3109' THEN 'SERVICE_END_DATE_CHANGE_REASON'
    WHEN 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110' THEN 'USER_INVOLVEMENT_IN_ACTION_PLAN'
  END
WHERE id IN (
    '73e9acfe-86ce-44ff-9be5-eb6151ed0f4c',
    'dd44c3d4-5168-7758-e3b7-a81b27aab301',
    'dd44c3d4-5168-7758-e3b7-a81b27aab302',
    'dd44c3d4-5168-7758-e3b7-a81b27aab303',
    'dd44c3d4-5168-7758-e3b7-a81b27aab304',
    'dd44c3d4-5168-7758-e3b7-a81b27aab305',
    'dd44c3d4-5168-7758-e3b7-a81b27aab306',
    'dd44c3d4-5168-7758-e3b7-a81b27aab307',
    'dd44c3d4-5168-7758-e3b7-a81b27aab308',
    'bb22b1a0-6656-49f5-a0dc-a95d3a6ec009',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3101',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3102',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3103',
    -- 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3104',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3108',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3109',
    'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110'
);

ALTER TABLE action_plan_step_question
    ALTER COLUMN question_key SET NOT NULL;

CREATE UNIQUE INDEX uk_action_plan_step_question_step_key
    ON action_plan_step_question (action_plan_step_id, question_key);

COMMENT ON COLUMN action_plan_step_question.question_key IS 'Key for identifying a question in the UI';

-- patch Session delivery details IN_PERSON choice to remove free text fields, as the free text is now captured in a separate question
UPDATE action_plan_step_question_choice
SET
    has_free_text = false,
    free_text_label = NULL,
    free_text_hint = NULL
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3201';