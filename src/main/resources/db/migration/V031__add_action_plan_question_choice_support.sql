-- V31: Add radio answer support and question choices for action plan question data model

ALTER TABLE action_plan_step_question
    DROP CONSTRAINT IF EXISTS chk_action_plan_step_question_answer_type;

ALTER TABLE action_plan_step_question
    ADD CONSTRAINT chk_action_plan_step_question_answer_type
        CHECK (answer_type IN ('textarea', 'radio', 'checkbox'));

CREATE TABLE IF NOT EXISTS action_plan_step_question_choice (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_step_question_id UUID NOT NULL,
    order_number INTEGER NOT NULL,
    label TEXT NOT NULL,
    value TEXT NOT NULL,
    has_free_text BOOLEAN NOT NULL DEFAULT false,
    free_text_label TEXT,
    CONSTRAINT fk_action_plan_step_question_choice_question
        FOREIGN KEY (action_plan_step_question_id) REFERENCES action_plan_step_question(id) ON DELETE CASCADE,
    CONSTRAINT chk_action_plan_step_question_choice_order_number_min
        CHECK (order_number >= 1),
    CONSTRAINT uk_action_plan_step_question_choice_question_order
        UNIQUE (action_plan_step_question_id, order_number),
    CONSTRAINT uk_action_plan_step_question_choice_question_value
        UNIQUE (action_plan_step_question_id, value)
);

COMMENT ON TABLE action_plan_step_question_choice IS 'Predefined answer choices for action plan step questions';
COMMENT ON COLUMN action_plan_step_question_choice.id IS 'Unique identifier for a question choice';
COMMENT ON COLUMN action_plan_step_question_choice.action_plan_step_question_id IS 'Foreign key reference to action_plan_step_question.id';
COMMENT ON COLUMN action_plan_step_question_choice.order_number IS 'Display order of the choice within a question; starts at 1';
COMMENT ON COLUMN action_plan_step_question_choice.label IS 'Display label shown to the user';
COMMENT ON COLUMN action_plan_step_question_choice.value IS 'Stored value for the selected choice';
COMMENT ON COLUMN action_plan_step_question_choice.has_free_text IS 'Indicates whether this choice reveals a free-text input';
COMMENT ON COLUMN action_plan_step_question_choice.free_text_label IS 'Label shown alongside the free-text input when has_free_text is true';
