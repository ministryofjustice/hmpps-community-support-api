CREATE TABLE IF NOT EXISTS action_plan_question_response_event (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_id UUID NOT NULL,
    action_plan_step_question_answer_header_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    question_response_change_batch_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT fk_action_plan_question_response_event_action_plan
        FOREIGN KEY (action_plan_id) REFERENCES action_plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_plan_question_response_event_header
        FOREIGN KEY (action_plan_step_question_answer_header_id) REFERENCES action_plan_step_question_answer_header(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_action_plan_question_response_event_change_batch_id
    ON action_plan_question_response_event (question_response_change_batch_id);

COMMENT ON COLUMN action_plan_question_response_event.id IS 'Unique identifier for the action plan question response event';
COMMENT ON COLUMN action_plan_question_response_event.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_question_response_event.action_plan_step_question_answer_header_id IS 'Foreign key reference to action_plan_step_question_answer_header.id';
COMMENT ON COLUMN action_plan_question_response_event.event_type IS 'Type of event raised against an action plan question response';
COMMENT ON COLUMN action_plan_question_response_event.question_response_change_batch_id IS 'Groups all per-answer response events emitted by one change batch';
COMMENT ON COLUMN action_plan_question_response_event.created_at IS 'Timestamp when the event occurred';
COMMENT ON COLUMN action_plan_question_response_event.created_by IS 'Actor identifier that created the event (UUID or SYSTEM)';
