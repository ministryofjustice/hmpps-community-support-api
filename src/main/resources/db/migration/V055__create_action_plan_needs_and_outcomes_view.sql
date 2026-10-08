-- V55: Create action plan needs and outcomes view

CREATE VIEW action_plan_needs_and_outcomes_view AS
SELECT
    ap.referral_id AS referral_id,
    ap.id AS action_plan_id,
    apsqah.id AS action_plan_step_question_answer_header_id,
    apsqad.id AS action_plan_step_question_answer_details_id,
    rdn.id AS need_id,
    rdn.label AS need_name,
    apsqad.content AS outcome_answer_text,
    apsqah.order_number AS display_order
FROM action_plan_step_question_answer_header apsqah
INNER JOIN action_plan ap ON apsqah.action_plan_id = ap.id
INNER JOIN action_plan_step_question_answer_details apsqad ON apsqah.id = apsqad.action_plan_step_question_answer_header_id
INNER JOIN reference_data_action_plan_step_question rdapsq ON apsqah.action_plan_step_question_id = rdapsq.id
INNER JOIN reference_data_need rdn ON rdapsq.need_id = rdn.id;

COMMENT ON VIEW action_plan_needs_and_outcomes_view IS 'view for action plans showing details about the needs and outcomes';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.referral_id IS 'Unique identifier for the referral';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.action_plan_id IS 'Unique identifier for the action plan';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.action_plan_step_question_answer_header_id IS 'Unique identifier for the action plan step question answer header';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.action_plan_step_question_answer_details_id IS 'Unique identifier for the action plan step question answer details';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.need_id IS 'Unique identifier for the need';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.need_name IS 'Name of the need';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.outcome_answer_text IS 'The answer text for the outcome';
COMMENT ON COLUMN action_plan_needs_and_outcomes_view.display_order IS 'Display order of the need in this persons action plan; starts at 1';
