ALTER TABLE action_plan_step_question
    ADD COLUMN IF NOT EXISTS hint TEXT;

COMMENT ON COLUMN action_plan_step_question.hint IS 'Optional helper text shown alongside the question';

ALTER TABLE action_plan_step_question_choice
    ADD COLUMN IF NOT EXISTS free_text_hint TEXT;

COMMENT ON COLUMN action_plan_step_question_choice.free_text_hint IS 'Optional helper text shown alongside the free-text input for the choice';
