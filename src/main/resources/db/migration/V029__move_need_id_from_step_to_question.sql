-- V29: Move need_id from action_plan_step to action_plan_step_question
-- The need link is now held at the question level so a single step can contain
-- one question per need, rather than requiring one step per need.

-- 1. Remove the per-step need constraints
DROP INDEX IF EXISTS uk_action_plan_step_template_need;

ALTER TABLE action_plan_step
    DROP CONSTRAINT IF EXISTS fk_action_plan_step_need;

ALTER TABLE action_plan_step
    DROP COLUMN IF EXISTS need_id;

-- 2. Add need_id to action_plan_step_question
ALTER TABLE action_plan_step_question
    ADD COLUMN IF NOT EXISTS need_id UUID;

ALTER TABLE action_plan_step_question
    ADD CONSTRAINT fk_action_plan_step_question_need
        FOREIGN KEY (need_id) REFERENCES need(id);

COMMENT ON COLUMN action_plan_step_question.need_id IS 'Optional foreign key reference to need.id; set when the question captures outcomes for a specific need';

