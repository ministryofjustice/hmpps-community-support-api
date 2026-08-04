-- V16: Create action plan template and runtime tables

CREATE TABLE IF NOT EXISTS action_plan_template (
    id UUID NOT NULL PRIMARY KEY
);

COMMENT ON COLUMN action_plan_template.id IS 'Unique identifier for the action plan template';

CREATE TABLE IF NOT EXISTS action_plan_step (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_template_id UUID NOT NULL,
    order_number INTEGER NOT NULL,
    name TEXT NOT NULL,
    CONSTRAINT fk_action_plan_step_template
        FOREIGN KEY (action_plan_template_id) REFERENCES action_plan_template(id) ON DELETE CASCADE,
    CONSTRAINT chk_action_plan_step_order_number_min
        CHECK (order_number >= 1),
    CONSTRAINT uk_action_plan_step_template_order
        UNIQUE (action_plan_template_id, order_number)
);

COMMENT ON COLUMN action_plan_step.id IS 'Unique identifier for the action plan step';
COMMENT ON COLUMN action_plan_step.action_plan_template_id IS 'Foreign key reference to action_plan_template.id';
COMMENT ON COLUMN action_plan_step.order_number IS 'Display order of the step within a template; starts at 1';
COMMENT ON COLUMN action_plan_step.name IS 'Human-readable name of the step';

CREATE TABLE IF NOT EXISTS action_plan_step_question (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_step_id UUID NOT NULL,
    order_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    answer_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_action_plan_step_question_step
        FOREIGN KEY (action_plan_step_id) REFERENCES action_plan_step(id) ON DELETE CASCADE,
    CONSTRAINT chk_action_plan_step_question_order_number_min
        CHECK (order_number >= 1),
    CONSTRAINT chk_action_plan_step_question_answer_type
        CHECK (answer_type = 'textarea'),
    CONSTRAINT uk_action_plan_step_question_step_order
        UNIQUE (action_plan_step_id, order_number)
);

COMMENT ON COLUMN action_plan_step_question.id IS 'Unique identifier for the action plan step question';
COMMENT ON COLUMN action_plan_step_question.action_plan_step_id IS 'Foreign key reference to action_plan_step.id';
COMMENT ON COLUMN action_plan_step_question.order_number IS 'Display order of the question within a step; starts at 1';
COMMENT ON COLUMN action_plan_step_question.title IS 'Question prompt shown to the user';
COMMENT ON COLUMN action_plan_step_question.answer_type IS 'Supported answer type for the question';

CREATE TABLE IF NOT EXISTS action_plan (
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_action_plan_referral
        FOREIGN KEY (referral_id) REFERENCES referral(id) ON DELETE CASCADE,
    CONSTRAINT uk_action_plan_referral
        UNIQUE (referral_id)
);

COMMENT ON COLUMN action_plan.id IS 'Unique identifier for an action plan';
COMMENT ON COLUMN action_plan.referral_id IS 'Foreign key reference to referral.id';
COMMENT ON COLUMN action_plan.created_at IS 'Timestamp when the action plan was created';
COMMENT ON COLUMN action_plan.updated_at IS 'Timestamp when the action plan was last updated';

CREATE TABLE IF NOT EXISTS action_plan_step_question_response (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_id UUID NOT NULL,
    action_plan_step_question_id UUID NOT NULL,
    response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT fk_action_plan_step_question_response_action_plan
        FOREIGN KEY (action_plan_id) REFERENCES action_plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_plan_step_question_response_question
        FOREIGN KEY (action_plan_step_question_id) REFERENCES action_plan_step_question(id) ON DELETE CASCADE,
    CONSTRAINT uk_action_plan_step_question_response_plan_question
        UNIQUE (action_plan_id, action_plan_step_question_id)
);

COMMENT ON COLUMN action_plan_step_question_response.id IS 'Unique identifier for a recorded question response';
COMMENT ON COLUMN action_plan_step_question_response.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_step_question_response.action_plan_step_question_id IS 'Foreign key reference to action_plan_step_question.id';
COMMENT ON COLUMN action_plan_step_question_response.response IS 'String representation of the response';
COMMENT ON COLUMN action_plan_step_question_response.created_at IS 'Timestamp when the response was recorded';
COMMENT ON COLUMN action_plan_step_question_response.created_by IS 'Actor identifier that created the response (UUID or SYSTEM)';

CREATE TABLE IF NOT EXISTS action_plan_event (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT fk_action_plan_event_action_plan
        FOREIGN KEY (action_plan_id) REFERENCES action_plan(id) ON DELETE CASCADE
);

COMMENT ON COLUMN action_plan_event.id IS 'Unique identifier for the action plan event';
COMMENT ON COLUMN action_plan_event.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_event.event_type IS 'Type of event raised against an action plan';
COMMENT ON COLUMN action_plan_event.created_at IS 'Timestamp when the event occurred';
COMMENT ON COLUMN action_plan_event.created_by IS 'Actor identifier that created the event (UUID or SYSTEM)';
