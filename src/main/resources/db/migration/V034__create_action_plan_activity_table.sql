CREATE UNIQUE INDEX IF NOT EXISTS uk_action_plan_step_question_id_question_type
    ON action_plan_step_question (id, question_type);

CREATE TABLE IF NOT EXISTS action_plan_activity (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_step_question_id UUID NOT NULL,
    action_plan_step_question_type VARCHAR(30) NOT NULL DEFAULT 'OUTCOME',
    who TEXT NOT NULL,
    activity_details TEXT NOT NULL,
    status TEXT NOT NULL,
    CONSTRAINT chk_action_plan_activity_question_type_outcome
        CHECK (action_plan_step_question_type = 'OUTCOME'),
    CONSTRAINT fk_action_plan_activity_question
        FOREIGN KEY (action_plan_step_question_id, action_plan_step_question_type)
            REFERENCES action_plan_step_question (id, question_type)
            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_action_plan_activity_question_id
    ON action_plan_activity (action_plan_step_question_id);

COMMENT ON TABLE action_plan_activity IS 'Activities associated with outcome action plan step questions';
COMMENT ON COLUMN action_plan_activity.id IS 'Unique identifier for an action plan activity';
COMMENT ON COLUMN action_plan_activity.action_plan_step_question_id IS 'Foreign key reference to action_plan_step_question.id';
COMMENT ON COLUMN action_plan_activity.action_plan_step_question_type IS 'Constraint column fixed to OUTCOME to enforce question type';
COMMENT ON COLUMN action_plan_activity.who IS 'Who is responsible for the activity';
COMMENT ON COLUMN action_plan_activity.activity_details IS 'Details describing the activity';
COMMENT ON COLUMN action_plan_activity.status IS 'Current status of the activity';
