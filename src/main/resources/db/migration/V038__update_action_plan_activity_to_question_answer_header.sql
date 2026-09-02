-- Activities are keyed to the old step question model which is incompatible with the new
-- answer header model; there is no safe way to migrate existing rows so we clear the table.
TRUNCATE TABLE action_plan_activity;

ALTER TABLE action_plan_activity
    DROP CONSTRAINT IF EXISTS fk_action_plan_activity_question;

ALTER TABLE action_plan_activity
    DROP CONSTRAINT IF EXISTS chk_action_plan_activity_question_type_outcome;

-- Remove the composite-FK support index created in V034 on action_plan_step_question
DROP INDEX IF EXISTS uk_action_plan_step_question_id_question_type;

DROP INDEX IF EXISTS idx_action_plan_activity_question_id;

ALTER TABLE action_plan_activity
    DROP COLUMN IF EXISTS action_plan_step_question_id;

ALTER TABLE action_plan_activity
    DROP COLUMN IF EXISTS action_plan_step_question_type;

ALTER TABLE action_plan_activity
    ADD COLUMN action_plan_step_question_answer_header_id UUID NOT NULL;

ALTER TABLE action_plan_activity
    ADD CONSTRAINT fk_action_plan_activity_question_answer_header
        FOREIGN KEY (action_plan_step_question_answer_header_id)
            REFERENCES action_plan_step_question_answer_header (id)
            ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_action_plan_activity_question_answer_header_id
    ON action_plan_activity (action_plan_step_question_answer_header_id);

COMMENT ON TABLE action_plan_activity IS 'Activities associated with action plan question answer headers';
COMMENT ON COLUMN action_plan_activity.action_plan_step_question_answer_header_id IS 'Foreign key reference to action_plan_step_question_answer_header.id';
