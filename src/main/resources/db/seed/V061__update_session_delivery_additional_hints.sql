-- V61: Update session delivery additional steps and choices - risk and adjustments

-- risk and adjustments step
UPDATE reference_data_action_plan_step_question_choice SET free_text_hint = 'Details about the risks and what you will put in place to reduce them'
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3211' AND action_plan_step_question_id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3105';

UPDATE reference_data_action_plan_step_question_choice SET free_text_hint = 'Reasonable adjustments you will make and how this will support {{ firstName }}'
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3213' AND action_plan_step_question_id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3106';

-- user involvement step
UPDATE reference_data_action_plan_step_question_choice SET free_text_hint = 'Why {{ firstName }} was not involved in creating the action plan?'
WHERE id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3218' AND action_plan_step_question_id = 'e8f3b4f9-8d84-4b3a-9f47-5f78f4cb3110';
