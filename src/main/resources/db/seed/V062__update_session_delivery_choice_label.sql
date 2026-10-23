-- V62: Update label of a question choice for the service delivery check service end date step

-- check service end date step
UPDATE reference_data_action_plan_step_question_choice SET label = 'No'
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3216' AND action_plan_step_question_id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3107';
