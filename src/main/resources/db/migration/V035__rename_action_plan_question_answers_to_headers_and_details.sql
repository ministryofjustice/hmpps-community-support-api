-- V35: Make the answer header/details data model explicit.
-- This migration is intentionally non-idempotent because Flyway versioned migrations run once.

ALTER TABLE action_plan_step_question_answer
    RENAME TO action_plan_step_question_answer_header;

ALTER TABLE action_plan_step_question_answer_revision
    RENAME TO action_plan_step_question_answer_details;

ALTER TABLE action_plan_step_question_answer_details
    RENAME COLUMN action_plan_step_question_answer_id TO action_plan_step_question_answer_header_id;

ALTER TABLE action_plan_step_question_answer_header
    RENAME CONSTRAINT fk_action_plan_question_answer_action_plan TO fk_action_plan_question_answer_header_action_plan;

ALTER TABLE action_plan_step_question_answer_header
    RENAME CONSTRAINT fk_action_plan_question_answer_question TO fk_action_plan_question_answer_header_question;

ALTER TABLE action_plan_step_question_answer_header
    RENAME CONSTRAINT chk_action_plan_question_answer_order_number_min TO chk_action_plan_question_answer_header_order_number_min;

ALTER INDEX uk_action_plan_question_answer_active_order
    RENAME TO uk_action_plan_question_answer_header_active_order;

ALTER TABLE action_plan_step_question_answer_details
    RENAME CONSTRAINT fk_action_plan_question_answer_revision_answer TO fk_action_plan_question_answer_details_header;

ALTER TABLE action_plan_step_question_answer_details
    RENAME CONSTRAINT chk_action_plan_question_answer_revision_number_min TO chk_action_plan_question_answer_details_number_min;

ALTER TABLE action_plan_step_question_answer_details
    RENAME CONSTRAINT uk_action_plan_question_answer_revision_number TO uk_action_plan_question_answer_details_number;

COMMENT ON TABLE action_plan_step_question_answer_header IS 'Stable reference for an answer to one question in an action plan';
COMMENT ON COLUMN action_plan_step_question_answer_header.id IS 'Unique identifier for the answer header';
COMMENT ON COLUMN action_plan_step_question_answer_header.action_plan_id IS 'Foreign key reference to action_plan.id';
COMMENT ON COLUMN action_plan_step_question_answer_header.action_plan_step_question_id IS 'Foreign key reference to action_plan_step_question.id';
COMMENT ON COLUMN action_plan_step_question_answer_header.order_number IS 'Display order of the answer for its question; starts at 1';
COMMENT ON COLUMN action_plan_step_question_answer_header.created_at IS 'Timestamp when the answer header was created';
COMMENT ON COLUMN action_plan_step_question_answer_header.created_by IS 'Actor identifier that created the answer header (UUID or SYSTEM)';
COMMENT ON COLUMN action_plan_step_question_answer_header.deleted_at IS 'Timestamp when the answer header was soft-deleted';
COMMENT ON COLUMN action_plan_step_question_answer_header.deleted_by IS 'Actor identifier that soft-deleted the answer header';

COMMENT ON TABLE action_plan_step_question_answer_details IS 'Recorded versions of an answer header over time';
COMMENT ON COLUMN action_plan_step_question_answer_details.id IS 'Unique identifier for an answer details record';
COMMENT ON COLUMN action_plan_step_question_answer_details.action_plan_step_question_answer_header_id IS 'Foreign key reference to action_plan_step_question_answer_header.id';
COMMENT ON COLUMN action_plan_step_question_answer_details.revision_number IS 'Sequence number of this version within an answer header; starts at 1';
COMMENT ON COLUMN action_plan_step_question_answer_details.content IS 'Recorded answer content for this version';
COMMENT ON COLUMN action_plan_step_question_answer_details.free_text_value IS 'Optional free-text value captured for this version';
COMMENT ON COLUMN action_plan_step_question_answer_details.created_at IS 'Timestamp when this answer version was recorded';
COMMENT ON COLUMN action_plan_step_question_answer_details.created_by IS 'Actor identifier that created this answer version (UUID or SYSTEM)';
