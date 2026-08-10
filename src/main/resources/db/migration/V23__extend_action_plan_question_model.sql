-- V23: Extend action plan step/question model for need-linked steps and answer revision history

ALTER TABLE action_plan_step
    ADD COLUMN step_type VARCHAR(20),
    ADD COLUMN need_id UUID;

UPDATE action_plan_step
SET step_type = 'CATCH_ALL'
WHERE step_type IS NULL;

ALTER TABLE action_plan_step
    ALTER COLUMN step_type SET NOT NULL,
    ALTER COLUMN step_type SET DEFAULT 'NEED';

ALTER TABLE action_plan_step
    ADD CONSTRAINT fk_action_plan_step_need
        FOREIGN KEY (need_id) REFERENCES need(id);

CREATE UNIQUE INDEX uk_action_plan_step_template_need
    ON action_plan_step (action_plan_template_id, need_id)
    WHERE need_id IS NOT NULL;

COMMENT ON COLUMN action_plan_step.step_type IS 'Defines whether this step links to a need or is a catch-all section';
COMMENT ON COLUMN action_plan_step.need_id IS 'Optional foreign key reference to need.id when step_type is need';

ALTER TABLE action_plan_step_question
    ADD COLUMN question_type VARCHAR(30),
    ADD COLUMN max_number_responses INTEGER;

UPDATE action_plan_step_question q
SET question_type = CASE
    WHEN EXISTS (
      SELECT 1
      FROM action_plan_step s
      WHERE s.id = q.action_plan_step_id
                AND s.step_type = 'CATCH_ALL'
        ) THEN 'GENERAL'
        ELSE 'OUTCOME'
  END,
  max_number_responses = 1
WHERE question_type IS NULL
   OR max_number_responses IS NULL;

ALTER TABLE action_plan_step_question
    ALTER COLUMN question_type SET NOT NULL,
    ALTER COLUMN question_type SET DEFAULT 'OUTCOME',
    ALTER COLUMN max_number_responses SET NOT NULL,
    ALTER COLUMN max_number_responses SET DEFAULT 1,
    ADD CONSTRAINT chk_action_plan_step_question_max_number_responses_min
      CHECK (max_number_responses >= 1);

COMMENT ON COLUMN action_plan_step_question.question_type IS 'Categorises question intent for downstream workflows';
COMMENT ON COLUMN action_plan_step_question.max_number_responses IS 'Maximum distinct active answers permitted for this question';

DROP TABLE IF EXISTS action_plan_step_question_response CASCADE;

CREATE TABLE IF NOT EXISTS action_plan_step_question_answer (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_id UUID NOT NULL,
    action_plan_step_question_id UUID NOT NULL,
    order_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMP,
    deleted_by TEXT,
    CONSTRAINT fk_action_plan_question_answer_action_plan
        FOREIGN KEY (action_plan_id) REFERENCES action_plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_plan_question_answer_question
        FOREIGN KEY (action_plan_step_question_id) REFERENCES action_plan_step_question(id) ON DELETE CASCADE,
    CONSTRAINT chk_action_plan_question_answer_order_number_min
        CHECK (order_number >= 1)
);

CREATE UNIQUE INDEX uk_action_plan_question_answer_active_order
    ON action_plan_step_question_answer (action_plan_id, action_plan_step_question_id, order_number)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE action_plan_step_question_answer IS 'Distinct answer slots for a question within one action plan';
COMMENT ON COLUMN action_plan_step_question_answer.id IS 'Unique identifier for a question answer slot';
COMMENT ON COLUMN action_plan_step_question_answer.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_step_question_answer.action_plan_step_question_id IS 'Foreign key reference to action_plan_step_question.id';
COMMENT ON COLUMN action_plan_step_question_answer.order_number IS 'Display order of answer slot within a question; starts at 1';
COMMENT ON COLUMN action_plan_step_question_answer.created_at IS 'Timestamp when the answer slot was first created';
COMMENT ON COLUMN action_plan_step_question_answer.created_by IS 'Actor identifier that created the answer slot (UUID or SYSTEM)';
COMMENT ON COLUMN action_plan_step_question_answer.deleted_at IS 'Timestamp when the answer slot was soft-deleted';
COMMENT ON COLUMN action_plan_step_question_answer.deleted_by IS 'Actor identifier that soft-deleted the answer slot';

CREATE TABLE IF NOT EXISTS action_plan_step_question_answer_revision (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_step_question_answer_id UUID NOT NULL,
    revision_number INTEGER NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT fk_action_plan_question_answer_revision_answer
        FOREIGN KEY (action_plan_step_question_answer_id) REFERENCES action_plan_step_question_answer(id) ON DELETE CASCADE,
    CONSTRAINT chk_action_plan_question_answer_revision_number_min
        CHECK (revision_number >= 1),
    CONSTRAINT uk_action_plan_question_answer_revision_number
        UNIQUE (action_plan_step_question_answer_id, revision_number)
);

COMMENT ON TABLE action_plan_step_question_answer_revision IS 'Immutable revisions for each question answer slot';
COMMENT ON COLUMN action_plan_step_question_answer_revision.id IS 'Unique identifier for an answer revision';
COMMENT ON COLUMN action_plan_step_question_answer_revision.action_plan_step_question_answer_id IS 'Foreign key reference to action_plan_step_question_answer.id';
COMMENT ON COLUMN action_plan_step_question_answer_revision.revision_number IS 'Monotonic revision number for an answer slot; starts at 1';
COMMENT ON COLUMN action_plan_step_question_answer_revision.content IS 'Recorded answer content for this revision';
COMMENT ON COLUMN action_plan_step_question_answer_revision.created_at IS 'Timestamp when this revision was recorded';
COMMENT ON COLUMN action_plan_step_question_answer_revision.created_by IS 'Actor identifier that created this revision (UUID or SYSTEM)';

CREATE TABLE IF NOT EXISTS action_plan_need (
    id UUID NOT NULL PRIMARY KEY,
    action_plan_id UUID NOT NULL,
    need_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMP,
    deleted_by TEXT,
    CONSTRAINT fk_action_plan_need_action_plan
        FOREIGN KEY (action_plan_id) REFERENCES action_plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_plan_need_need
        FOREIGN KEY (need_id) REFERENCES need(id)
);

CREATE UNIQUE INDEX uk_action_plan_need_active
    ON action_plan_need (action_plan_id, need_id)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE action_plan_need IS 'Tracks which needs are identified for a specific action plan';
COMMENT ON COLUMN action_plan_need.id IS 'Unique identifier for the action-plan-need link';
COMMENT ON COLUMN action_plan_need.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_need.need_id IS 'Foreign key reference to need.id';
COMMENT ON COLUMN action_plan_need.created_at IS 'Timestamp when the need was linked to the action plan';
COMMENT ON COLUMN action_plan_need.created_by IS 'Actor identifier that linked the need to the action plan (UUID or SYSTEM)';
COMMENT ON COLUMN action_plan_need.deleted_at IS 'Timestamp when the action-plan-need link was soft-deleted';
COMMENT ON COLUMN action_plan_need.deleted_by IS 'Actor identifier that soft-deleted the action-plan-need link';
